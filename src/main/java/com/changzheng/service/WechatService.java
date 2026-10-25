package com.changzheng.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.changzheng.common.BizException;
import com.changzheng.config.WechatProperties;
import com.changzheng.entity.*;
import com.changzheng.mapper.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.net.URI;

@Slf4j @Service @RequiredArgsConstructor
public class WechatService {
 private final WechatProperties properties; private final UserMapper userMapper; private final WechatSessionMapper sessionMapper; private final ObjectMapper objectMapper=new ObjectMapper(); private final RestClient restClient=RestClient.create();
 public User login(String code) {
   if(code==null||code.isBlank()) throw new BizException(40001,"code不能为空");
   WechatSession old=sessionMapper.selectOne(new QueryWrapper<WechatSession>().eq("code",code));
   if(old!=null && old.getUserId()!=null && (old.getExpiresAt()==null||old.getExpiresAt().isAfter(LocalDateTime.now()))) return userMapper.selectById(old.getUserId());
   String openid; String sessionKey; String unionid=null;
   if(properties.isMock() || properties.getAppId()==null || properties.getAppId().isBlank()) { openid="mock-openid-"+code; sessionKey=Base64.getEncoder().encodeToString("0123456789012345".getBytes(StandardCharsets.UTF_8)); }
   else {
     try { String body=restClient.get().uri(URI.create(properties.getSessionUrl()+"?appid="+properties.getAppId()+"&secret="+properties.getAppSecret()+"&js_code="+code+"&grant_type=authorization_code")).retrieve().body(String.class); JsonNode n=objectMapper.readTree(body); if(n.has("errcode")) throw new BizException(40003,"微信登录失败："+n.path("errmsg").asText()); openid=n.path("openid").asText(); sessionKey=n.path("session_key").asText(); unionid=n.path("unionid").asText(null); }
     catch(BizException e){throw e;} catch(Exception e){throw new BizException(40003,"微信登录失败");}
   }
   User user=userMapper.selectOne(new QueryWrapper<User>().eq("openid",openid));
   if(user==null){ user=new User(); user.setOpenid(openid); user.setUnionid(unionid); user.setStatus(1); user.setCreatedAt(LocalDateTime.now()); user.setUpdatedAt(LocalDateTime.now()); userMapper.insert(user); }
   else { user.setUpdatedAt(LocalDateTime.now()); if(unionid!=null)user.setUnionid(unionid); userMapper.updateById(user); }
   WechatSession s=old==null?new WechatSession():old; s.setUserId(user.getId());s.setCode(code);s.setOpenid(openid);s.setSessionKey(sessionKey);s.setExpiresAt(LocalDateTime.now().plusDays(1));s.setUpdatedAt(LocalDateTime.now()); if(old==null){s.setCreatedAt(LocalDateTime.now());sessionMapper.insert(s);}else sessionMapper.updateById(s);
   return user;
 }
 public int decryptSteps(String code,String encryptedData,String iv, User user) {
   if(properties.isMock()) return 12345;
   WechatSession s=sessionMapper.selectOne(new QueryWrapper<WechatSession>().eq("code",code).eq("user_id",user.getId()));
   if(s==null) { login(code); s=sessionMapper.selectOne(new QueryWrapper<WechatSession>().eq("code",code).eq("user_id",user.getId())); }
   if(s==null) throw new BizException(40003,"微信会话不存在");
   try { byte[] key=Base64.getDecoder().decode(s.getSessionKey()); byte[] cipher=Base64.getDecoder().decode(encryptedData); byte[] ivBytes=Base64.getDecoder().decode(iv); Cipher c=Cipher.getInstance("AES/CBC/PKCS5Padding"); c.init(Cipher.DECRYPT_MODE,new SecretKeySpec(key,"AES"),new IvParameterSpec(ivBytes)); JsonNode n=objectMapper.readTree(new String(c.doFinal(cipher),StandardCharsets.UTF_8)); JsonNode list=n.path("stepInfoList"); if(!list.isArray()||list.isEmpty()) throw new BizException(40004,"微信运动数据为空"); return list.get(list.size()-1).path("step").asInt(0); }
   catch(BizException e){throw e;} catch(Exception e){log.warn("decrypt we-run-data failed",e);throw new BizException(40004,"微信运动数据解密失败");}
 }
}
