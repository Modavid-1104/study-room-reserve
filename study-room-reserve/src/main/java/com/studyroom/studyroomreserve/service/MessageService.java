package com.studyroom.studyroomreserve.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.studyroom.studyroomreserve.VO.MessageSendVo;
import com.studyroom.studyroomreserve.entity.Message;

import java.util.List;

public interface MessageService extends IService<Message> {
    List<Message> myList(Long userId);
    void markRead(Long userid,Long id);
    void sendMessage(MessageSendVo vo);
}
