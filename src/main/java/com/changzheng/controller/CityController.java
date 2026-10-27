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
        if (n == null) throw new com.changzheng.common.BizException(40007, "暂无该站点的详情信息");
        CityDetailVO v = new CityDetailVO();
        v.setCityName(n.getCityName());
        v.setCityImg(n.getCityImage());
        v.setCityDetail(n.getCityDetail());
        return ApiResponse.success(java.util.Map.of("cityDetail", v));
    }
}
