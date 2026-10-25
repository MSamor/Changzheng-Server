package com.changzheng.vo;

import lombok.Data;

import java.util.*;

@Data
public class MapDataVO {
    private long allRanking;
    private String arrivedCityName;
    private int convertSteps;
    private long daysElapsed;
    private int medal;
    private int nextStopKilometer;
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
