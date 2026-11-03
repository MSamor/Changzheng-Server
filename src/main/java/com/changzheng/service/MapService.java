package com.changzheng.service;

import com.changzheng.entity.*;
import com.changzheng.vo.MapDataVO;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.*;
import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MapService {
    private final ExchangeService exchange;
    private final StepService steps;
    private final RouteService route;
    private final ConfigService config;
    private final RankService rank;
    private final UserService users;

    public MapDataVO get(Long userId) {
        BigDecimal mileage = exchange.totalMileage(userId);
        int todaySteps = steps.today(userId);
        boolean exchangedToday = exchange.exchangedToday(userId);
        List<RouteNode> nodes = route.nodes();
        int medal = 0;
        String arrived = nodes.isEmpty() ? "" : nodes.get(0).getCityName();
        BigDecimal next = null;
        List<MapDataVO.PointVO> points = new ArrayList<>();
        for (RouteNode n : nodes) {
            if (mileage.signum() > 0 && mileage.compareTo(BigDecimal.valueOf(n.getRequiredMileage())) >= 0) {
                medal++;
                arrived = n.getCityName();
                points.add(
                        new MapDataVO.PointVO(
                                n.getLongitude().doubleValue(), n.getLatitude().doubleValue()));
            }
            if (next == null && BigDecimal.valueOf(n.getRequiredMileage()).compareTo(mileage) > 0) {
                next = BigDecimal.valueOf(n.getRequiredMileage()).subtract(mileage);
            }
        }
        MapDataVO v = new MapDataVO();
        v.setAllRanking(rank.data(userId).getMyAllRank());
        v.setArrivedCityName(arrived);
        v.setTodaySteps(todaySteps);
        v.setExchangedToday(exchangedToday);
        // The activity permits one redemption per day; later steps are not redeemable today.
        v.setConvertSteps(exchangedToday ? 0 : todaySteps);
        ConfigService.ExchangeRules rules = config.exchangeRules();
        v.setMinimumExchangeSteps(rules.minimumExchangeSteps());
        v.setStepUnit(rules.stepUnit());
        v.setMileageUnit(rules.mileageUnit());
        v.setDaysElapsed(
                Math.max(0, Duration.between(userCreated(userId), LocalDateTime.now()).toDays()));
        v.setMedal(medal);
        boolean routeCompleted = !nodes.isEmpty() && next == null;
        v.setRouteCompleted(routeCompleted);
        v.setNextStopKilometer(routeCompleted ? BigDecimal.ZERO : next);
        v.setPoints(points);
        v.setTodayRedeemedSteps(exchange.todaySteps(userId));
        v.setTotalMileage(mileage);
        return v;
    }

    private LocalDateTime userCreated(Long id) {
        return users.get(id).getCreatedAt() == null
                ? LocalDateTime.now()
                : users.get(id).getCreatedAt();
    }
}
