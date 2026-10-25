package com.changzheng.vo;
import lombok.Data; import java.util.*;
@Data public class RankDataVO { private List<RankUserVO> allRank=new ArrayList<>(); private List<RankUserVO> todayRank=new ArrayList<>(); private int myAllRank; private int myTodayRank; private int totalPeople; private int totalMileage; }
