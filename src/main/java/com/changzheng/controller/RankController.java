package com.changzheng.controller;

import com.changzheng.common.ApiResponse;
import com.changzheng.service.*;
import com.changzheng.vo.RankDataVO;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rank")
@RequiredArgsConstructor
public class RankController {
    private final UserService users;
    private final RankService ranks;

    @GetMapping({"/getAllRank", "/getTodayRank"})
    public ApiResponse<RankDataVO> get(@RequestParam Long userId) {
        users.get(userId);
        return ApiResponse.success(ranks.data(userId));
    }
}
