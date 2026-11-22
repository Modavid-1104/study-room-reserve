package com.example.campusstudyroombookingsystem.model;

public class SeatBatchRequest {
    private Long roomId;
    private Integer count;
    private String prefix;

    public SeatBatchRequest(){
    }

    public SeatBatchRequest(Long roomId,Integer count,String prefix){
        this.roomId = roomId;
        this.count = count;
        this.prefix = prefix;
    }
    public Long getRoomId(){return roomId;}
    public void setRoomId(Long roomId) {this.roomId = roomId;}
    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }


}
