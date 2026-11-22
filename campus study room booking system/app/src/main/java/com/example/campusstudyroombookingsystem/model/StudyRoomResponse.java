package com.example.campusstudyroombookingsystem.model;

public class StudyRoomResponse {
    private Long id;
    private String name;
    private String address;
    private String cover;
    private String description;
    private Integer totalSeat;
    private String openTime;
    private String closeTime;

    private Integer freeSeat;
    public Integer getFreeSeat() { return freeSeat; }


    public Long getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getCover() { return cover; }
    public String getDescription() { return description; }
    public Integer getTotalSeat() { return totalSeat; }
    public String getOpenTime() { return openTime; }
    public String getCloseTime() { return closeTime; }
}
