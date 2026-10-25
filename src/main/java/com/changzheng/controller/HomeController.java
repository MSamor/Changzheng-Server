package com.changzheng.controller;

import com.changzheng.common.ApiResponse;
import com.changzheng.dto.*;
import com.changzheng.entity.User;
import com.changzheng.service.*;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/home")
@RequiredArgsConstructor
public class HomeController {
    private final WechatService wechat;
    private final UserService users;
    private final StepService steps;

    @GetMapping("/getUserIdByCode/{code}")
    public ApiResponse<Map<String, Long>> login(@PathVariable String code) {
        User u = wechat.login(code);
        return ApiResponse.success(Map.of("userId", u.getId()));
    }

    @PostMapping("/steps")
    public ApiResponse<Map<String, Integer>> step(
            @RequestBody(required = false) StepsRequest body,
            @RequestParam(required = false) String encryptedData,
            @RequestParam(required = false) String iv,
            @RequestParam(required = false) String code) {
        StepsRequest r = body == null ? new StepsRequest() : body;
        if (r.getEncryptedData() == null) r.setEncryptedData(encryptedData);
        if (r.getIv() == null) r.setIv(iv);
        if (r.getCode() == null) r.setCode(code);
        if (r.getCode() == null || r.getEncryptedData() == null || r.getIv() == null)
            throw new com.changzheng.common.BizException(40001, "encryptedData、iv、code不能为空");
        User u = wechat.login(r.getCode());
        int value = wechat.decryptSteps(r.getCode(), r.getEncryptedData(), r.getIv(), u);
        return ApiResponse.success(Map.of("step", steps.saveMax(u.getId(), value)));
    }

    @PostMapping("/saveUserInfo")
    public ApiResponse<User> save(@Valid @RequestBody SaveUserInfoRequest r) {
        return ApiResponse.success(users.update(r));
    }
}
