package com.changzheng.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.changzheng.entity.RouteNode;
import com.changzheng.mapper.RouteNodeMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RouteService {
    private final RouteNodeMapper mapper;

    public List<RouteNode> nodes() {
        return mapper.selectList(
                new QueryWrapper<RouteNode>().eq("enabled", 1).orderByAsc("node_order"));
    }

    public RouteNode byName(String name) {
        return mapper.selectOne(
                new QueryWrapper<RouteNode>().eq("city_name", name).eq("enabled", 1));
    }
}
