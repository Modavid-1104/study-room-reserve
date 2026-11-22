package com.studyroom.studyroomreserve.VO;

import lombok.Data;

@Data
public class RoomVo {
    private Long id;          // 修改时带，新增时为空
    private String name;
    private String address;
    private String cover;
    private String description;
    private Integer totalSeat;
    private String openTime;
    private String closeTime;
}
