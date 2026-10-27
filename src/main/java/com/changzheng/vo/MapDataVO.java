package com.changzheng.vo;

import lombok.Data;

import java.util.*;

@Data
public class MapDataVO {
    private long allRanking;
    private String arrivedCityName;
    private int convertSteps;
    private int todaySteps;
    private boolean exchangedToday;
    private int minimumExchangeSteps;
    private int stepUnit;
    private int mileageUnit;
    private long daysElapsed;
    private int medal;
    // Null when route data is unavailable; zero only after completing the route.
    private Integer nextStopKilometer;
    private boolean routeCompleted;
    private List<PointVO> points;
    private int todayRedeemedSteps;
    private int totalMileage;

    @Data
    public static class PointVO {
        private double longitude;
        private double latitude;

        public PointVO(double longitude, double latitude) {
            this.longitude = longitude;
            this.latitude = latitude;
        }
    }
}
