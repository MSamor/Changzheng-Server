package com.changzheng.service;

import com.changzheng.entity.*;
import com.changzheng.mapper.StepExchangeMapper;
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
    private final StepExchangeMapper exchangeMapper;
    private final RankService rank;
    private final UserService users;

    public MapDataVO get(Long userId) {
        int mileage = exchange.totalMileage(userId),
                todaySteps = steps.today(userId),
                redeemed = exchange.todayMileage(userId);
        List<RouteNode> nodes = route.nodes();
        int medal = 0;
        String arrived = nodes.isEmpty() ? "" : nodes.get(0).getCityName();
        int next = 0;
        List<MapDataVO.PointVO> points = new ArrayList<>();
        for (RouteNode n : nodes) {
            if (mileage > 0 && mileage >= n.getRequiredMileage()) {
                medal++;
                arrived = n.getCityName();
                points.add(
                        new MapDataVO.PointVO(
                                n.getLongitude().doubleValue(), n.getLatitude().doubleValue()));
            } else if (next == 0) next = Math.max(0, n.getRequiredMileage() - mileage);
        }
        MapDataVO v = new MapDataVO();
        v.setAllRanking(rank.data(userId).getMyAllRank());
        v.setArrivedCityName(arrived);
        v.setConvertSteps(Math.max(0, todaySteps - exchange.todaySteps(userId)));
        v.setDaysElapsed(
                Math.max(0, Duration.between(userCreated(userId), LocalDateTime.now()).toDays()));
        v.setMedal(medal);
        v.setNextStopKilometer(next);
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
