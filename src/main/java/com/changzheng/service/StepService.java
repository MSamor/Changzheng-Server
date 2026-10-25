package com.changzheng.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.changzheng.entity.*;
import com.changzheng.mapper.DailyStepMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.*;

@Service
@RequiredArgsConstructor
public class StepService {
    private final DailyStepMapper mapper;

    public int saveMax(Long userId, int count) {
        LocalDate d = LocalDate.now();
        DailyStep x =
                mapper.selectOne(
                        new QueryWrapper<DailyStep>().eq("user_id", userId).eq("step_date", d));
        if (x == null) {
            x = new DailyStep();
            x.setUserId(userId);
            x.setStepDate(d);
            x.setStepCount(count);
            x.setCreatedAt(LocalDateTime.now());
            x.setUpdatedAt(LocalDateTime.now());
            mapper.insert(x);
        } else if (count > x.getStepCount()) {
            x.setStepCount(count);
            x.setUpdatedAt(LocalDateTime.now());
            mapper.updateById(x);
        }
        return x.getStepCount();
    }

    public int today(Long userId) {
        DailyStep x =
                mapper.selectOne(
                        new QueryWrapper<DailyStep>()
                                .eq("user_id", userId)
                                .eq("step_date", LocalDate.now()));
        return x == null ? 0 : x.getStepCount();
    }
}
