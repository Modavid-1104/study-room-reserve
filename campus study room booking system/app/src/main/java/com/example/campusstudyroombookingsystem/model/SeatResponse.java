package com.example.campusstudyroombookingsystem.model;

public class SeatResponse {
    private Long id;
    private Long roomId;
    private String seatNo;
    private Integer status;   // 0空闲 1已预约 2禁用
    private String remark;

    public Long getId() { return id; }
    public Long getRoomId() { return roomId; }
    public String getSeatNo() { return seatNo; }
    public Integer getStatus() { return status; }
    public String getRemark() { return remark; }
}
