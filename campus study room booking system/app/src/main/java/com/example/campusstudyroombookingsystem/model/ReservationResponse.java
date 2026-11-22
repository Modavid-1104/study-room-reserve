package com.example.campusstudyroombookingsystem.model;

public class ReservationResponse {
    private Long id;
    private Long seatId;
    private String seatNo;
    private String roomName;
    private String startTime;   // 后端 LocalDateTime 转成的字符串，用 String 接最省事
    private String endTime;
    private Integer status;
    private String signInTime;
    private String signOutTime;   // 新增
    private String createTime;



    public Long getId() { return id; }
    public Long getSeatId() { return seatId; }
    public String getSeatNo() { return seatNo; }
    public String getRoomName() { return roomName; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public Integer getStatus() { return status; }
    public String getSignInTime() { return signInTime; }
    public String getCreateTime() { return createTime; }
    public String getSignOutTime(){return signOutTime;}
}
