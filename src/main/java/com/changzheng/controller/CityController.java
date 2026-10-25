package com.changzheng.controller;

import com.changzheng.common.ApiResponse;
import com.changzheng.entity.RouteNode;
import com.changzheng.service.RouteService;
import com.changzheng.vo.CityDetailVO;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/city")
@RequiredArgsConstructor
public class CityController {
    private final RouteService route;

    @GetMapping("/getDetail/{place}")
    public ApiResponse<java.util.Map<String, CityDetailVO>> get(@PathVariable String place) {
        RouteNode n = route.byName(place);
        if (n == null)
            return ApiResponse.success(java.util.Map.of("cityDetail", new CityDetailVO()));
        CityDetailVO v = new CityDetailVO();
        v.setCityName(n.getCityName());
        v.setCityImg(n.getCityImage());
        v.setCityDetail(n.getCityDetail());
        return ApiResponse.success(java.util.Map.of("cityDetail", v));
    }
}
