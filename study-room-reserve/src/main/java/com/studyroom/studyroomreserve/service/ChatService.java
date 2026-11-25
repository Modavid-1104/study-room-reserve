package com.studyroom.studyroomreserve.service;

import com.studyroom.studyroomreserve.VO.ConversationVo;
import com.studyroom.studyroomreserve.VO.SendPrivateMsgVo;
import com.studyroom.studyroomreserve.entity.PrivateMessage;

import java.util.List;

public interface ChatService {

    // 学生端：获取（没有则创建）与管理员的会话
    ConversationVo getOrCreateConversation(Long userId);

    // 学生端：我的会话列表
    List<ConversationVo> myConversations(Long userId);

    // 管理员端：所有咨询会话
    List<ConversationVo> adminConversations(Long adminId);

    // 某会话的历史消息
    List<PrivateMessage> history(Long userId, Long conversationId);

    // 发送私信
    void sendMessage(Long senderId, SendPrivateMsgVo vo);

    // 进入会话：标记对方发来的消息为已读
    void markRead(Long userId, Long conversationId);
}
