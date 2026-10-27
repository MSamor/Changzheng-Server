package com.changzheng.service;

import com.changzheng.entity.*;
import com.changzheng.vo.MapDataVO;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.*;
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
        int mileage = exchange.totalMileage(userId);
        int todaySteps = steps.today(userId);
        boolean exchangedToday = exchange.exchangedToday(userId);
        List<RouteNode> nodes = route.nodes();
        int medal = 0;
        String arrived = nodes.isEmpty() ? "" : nodes.get(0).getCityName();
        Integer next = null;
        List<MapDataVO.PointVO> points = new ArrayList<>();
        for (RouteNode n : nodes) {
            if (mileage > 0 && mileage >= n.getRequiredMileage()) {
                medal++;
                arrived = n.getCityName();
                points.add(
                        new MapDataVO.PointVO(
                                n.getLongitude().doubleValue(), n.getLatitude().doubleValue()));
            }
            if (next == null && n.getRequiredMileage() > mileage) {
                next = n.getRequiredMileage() - mileage;
            }
        }
        MapDataVO v = new MapDataVO();
        v.setAllRanking(rank.data(userId).getMyAllRank());
        v.setArrivedCityName(arrived);
        v.setTodaySteps(todaySteps);
        v.setExchangedToday(exchangedToday);
        // The activity permits one redemption per day; later steps are not redeemable today.
        v.setConvertSteps(exchangedToday ? 0 : todaySteps);
        v.setMinimumExchangeSteps(config.intValue("minimum_exchange_steps", 100));
        v.setStepUnit(config.intValue("step_unit", 100));
        v.setMileageUnit(config.intValue("mileage_unit", 20));
        v.setDaysElapsed(
                Math.max(0, Duration.between(userCreated(userId), LocalDateTime.now()).toDays()));
        v.setMedal(medal);
        boolean routeCompleted = !nodes.isEmpty() && next == null;
        v.setRouteCompleted(routeCompleted);
        v.setNextStopKilometer(routeCompleted ? Integer.valueOf(0) : next);
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
