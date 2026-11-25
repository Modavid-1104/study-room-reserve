package com.studyroom.studyroomreserve.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Conversation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long adminId;
    private String lastMessage;
    private LocalDateTime lastTime;
    private Integer userUnread;
    private Integer adminUnread;
    private LocalDateTime createTime;
}
