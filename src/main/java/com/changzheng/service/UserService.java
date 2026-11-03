package com.changzheng.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.changzheng.common.BizException;
import com.changzheng.dto.SaveUserInfoRequest;
import com.changzheng.entity.User;
import com.changzheng.mapper.UserMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper mapper;

    public User get(Long id) {
        User u = mapper.selectById(id);
        if (u == null) throw new BizException(40002, "用户不存在");
        return u;
    }

    public User update(SaveUserInfoRequest r) {
        User u = get(r.getUserId());
        if (r.getAvatarUrl() != null) {
            String avatar = r.getAvatarUrl().trim();
            if (!avatar.isEmpty()) {
                try {
                    java.net.URI uri = java.net.URI.create(avatar);
                    if (!("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))
                            || uri.getHost() == null || uri.getUserInfo() != null) throw new IllegalArgumentException();
                } catch (IllegalArgumentException e) {
                    throw new BizException(40001, "请先上传头像，不能保存临时图片路径");
                }
            }
            u.setAvatarUrl(avatar);
        }
        if (r.getNickname() != null) {
            String nickname = r.getNickname().trim();
            if (nickname.isEmpty() || nickname.length() > 32)
                throw new BizException(40001, "昵称长度应为1到32个字符");
            u.setNickname(nickname);
        }
        if (r.getCity() != null) u.setCity(r.getCity());
        if (r.getCountry() != null) u.setCountry(r.getCountry());
        if (r.getGender() != null) u.setGender(r.getGender());
        if (r.getProvince() != null) u.setProvince(r.getProvince());
        u.setUpdatedAt(LocalDateTime.now());
        if (mapper.updateById(u) != 1) throw new BizException(50000, "资料保存失败，请重试");
        return u;
    }

    public int count() {
        return Math.toIntExact(mapper.selectCount(new QueryWrapper<>()));
    }
}
