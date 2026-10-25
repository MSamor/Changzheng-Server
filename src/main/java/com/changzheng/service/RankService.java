package com.changzheng.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.changzheng.entity.*;
import com.changzheng.mapper.*;
import com.changzheng.vo.*;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.*;

@Service
@RequiredArgsConstructor
public class RankService {
    private final UserMapper userMapper;
    private final StepExchangeMapper exchangeMapper;
    private final RouteService routeService;

    public RankDataVO data(Long me) {
        List<User> users = userMapper.selectList(new QueryWrapper<User>().eq("status", 1));
        List<StepExchange> all =
                exchangeMapper.selectList(new QueryWrapper<StepExchange>().eq("status", 1));
        Map<Long, Integer> total = new HashMap<>(), today = new HashMap<>();
        for (StepExchange x : all) {
            total.merge(x.getUserId(), x.getMileage(), Integer::sum);
            if (LocalDate.now().equals(x.getExchangeDate()))
                today.merge(x.getUserId(), x.getMileage(), Integer::sum);
        }
        List<RankUserVO> allList = make(users, total);
        List<RankUserVO> todayList = make(users, today);
        RankDataVO out = new RankDataVO();
        out.setAllRank(allList);
        out.setTodayRank(todayList);
        out.setTotalPeople(users.size());
        out.setTotalMileage(total.values().stream().mapToInt(Integer::intValue).sum());
        out.setMyAllRank(rankOf(allList, me));
        out.setMyTodayRank(rankOf(todayList, me));
        return out;
    }

    private List<RankUserVO> make(List<User> users, Map<Long, Integer> scores) {
        return users.stream()
                .map(
                        u -> {
                            RankUserVO v = new RankUserVO();
                            v.setUserId(u.getId());
                            v.setAvatarUrl(u.getAvatarUrl());
                            v.setNickname(u.getNickname());
                            v.setTotalMileage(scores.getOrDefault(u.getId(), 0));
                            v.setArrivedCityName(arrived(v.getTotalMileage()));
                            return v;
                        })
                .sorted(
                        Comparator.comparingInt(RankUserVO::getTotalMileage)
                                .reversed()
                                .thenComparing(RankUserVO::getUserId))
                .collect(Collectors.toList());
    }

    private int rankOf(List<RankUserVO> list, Long id) {
        for (int i = 0; i < list.size(); i++)
            if (Objects.equals(list.get(i).getUserId(), id)) return i + 1;
        return 0;
    }

    private String arrived(int mileage) {
        String result = "瑞金";
        for (RouteNode n : routeService.nodes())
            if (mileage >= n.getRequiredMileage()) result = n.getCityName();
        return mileage <= 0 ? "瑞金" : result;
    }
}
