package com.example.campusstudyroombookingsystem.model;

public class StatisticsResponse {
    private long totalMinutes;
    private long todayMinutes;
    private int checkInCount;
    private int continuousDays;

    public long getTotalMinutes() { return totalMinutes; }
    public long getTodayMinutes() { return todayMinutes; }
    public int getCheckInCount() { return checkInCount; }
    public int getContinuousDays() { return continuousDays; }
}
