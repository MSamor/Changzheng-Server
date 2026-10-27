package com.changzheng.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.changzheng.common.BizException;
import com.changzheng.entity.*;
import com.changzheng.mapper.StepExchangeMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;

@Service
@RequiredArgsConstructor
public class ExchangeService {
    private final StepExchangeMapper mapper;
    private final StepService stepService;
    private final ConfigService config;

    @Transactional
    public StepExchange exchange(Long userId) {
        LocalDate d = LocalDate.now();
        StepExchange old =
                mapper.selectOne(
                        new QueryWrapper<StepExchange>()
                                .eq("user_id", userId)
                                .eq("exchange_date", d));
        if (old != null) return old;
        int steps = stepService.today(userId);
        int unit = config.intValue("step_unit", 100),
                mileageUnit = config.intValue("mileage_unit", 20),
                min = config.intValue("minimum_exchange_steps", 100);
        if (steps < min) throw new BizException(40005, "步数不足，暂不能兑换");
        StepExchange x = new StepExchange();
        x.setUserId(userId);
        x.setExchangeDate(d);
        x.setStepCount(steps);
        x.setStepUnit(unit);
        x.setMileageUnit(mileageUnit);
        x.setMileage((steps / unit) * mileageUnit);
        x.setStatus(1);
        x.setCreatedAt(LocalDateTime.now());
        x.setUpdatedAt(LocalDateTime.now());
        try {
            mapper.insert(x);
            return x;
        } catch (DuplicateKeyException e) {
            return mapper.selectOne(
                    new QueryWrapper<StepExchange>().eq("user_id", userId).eq("exchange_date", d));
        }
    }

    public int totalMileage(Long userId) {
        return mapper
                .selectList(new QueryWrapper<StepExchange>().eq("user_id", userId).eq("status", 1))
                .stream()
                .mapToInt(StepExchange::getMileage)
                .sum();
    }

    public int todaySteps(Long userId) {
        StepExchange x =
                mapper.selectOne(
                        new QueryWrapper<StepExchange>()
                                .eq("user_id", userId)
                                .eq("exchange_date", LocalDate.now())
                                .eq("status", 1));
        return x == null ? 0 : x.getStepCount();
    }

    public boolean exchangedToday(Long userId) {
        return mapper.selectCount(
                        new QueryWrapper<StepExchange>()
                                .eq("user_id", userId)
                                .eq("exchange_date", LocalDate.now())
                                .eq("status", 1))
                > 0;
    }

    public int todayMileage(Long userId) {
        StepExchange x =
                mapper.selectOne(
                        new QueryWrapper<StepExchange>()
                                .eq("user_id", userId)
                                .eq("exchange_date", LocalDate.now())
                                .eq("status", 1));
        return x == null ? 0 : x.getMileage();
    }
}
