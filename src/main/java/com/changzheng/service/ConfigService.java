package com.changzheng.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.changzheng.entity.SystemConfig;
import com.changzheng.mapper.SystemConfigMapper;
import java.math.BigDecimal;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConfigService {
    private final SystemConfigMapper mapper;

    public int intValue(String key, int fallback) {
        SystemConfig c = mapper.selectOne(new QueryWrapper<SystemConfig>().eq("config_key", key));
        try {
            return c == null ? fallback : Integer.parseInt(c.getConfigValue());
        } catch (Exception e) {
            return fallback;
        }
    }

    // Default: 500 steps = 0.6 km; 5000 steps/day reaches Gansu (180 km) in 30 days.
    public ExchangeRules exchangeRules() {
        int stepUnit = intValue("step_unit", 500);
        if (stepUnit <= 0) stepUnit = 500;
        BigDecimal mileageUnit = new BigDecimal("0.6");
        SystemConfig stored = mapper.selectOne(new QueryWrapper<SystemConfig>().eq("config_key", "mileage_unit"));
        if (stored != null) {
            try {
                BigDecimal value = new BigDecimal(stored.getConfigValue());
                if (value.signum() > 0 && value.scale() <= 3) mileageUnit = value;
            } catch (NumberFormatException ignored) { }
        }
        int minimum = Math.max(stepUnit, intValue("minimum_exchange_steps", 500));
        return new ExchangeRules(stepUnit, mileageUnit, minimum);
    }

    public record ExchangeRules(int stepUnit, BigDecimal mileageUnit, int minimumExchangeSteps) {}
}
