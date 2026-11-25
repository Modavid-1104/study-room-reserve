package com.studyroom.studyroomreserve.ws;

import com.studyroom.studyroomreserve.util.JwtUtil;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/ws")
public class WebSocketServer {

    // 在线用户：userId -> 会话
    private static final ConcurrentHashMap<Long, Session> CLIENTS = new ConcurrentHashMap<>();
    private Long currentUserId;

    @OnOpen
    public void onOpen(Session session) {
        // 连接地址形如 ws://host:8080/ws?token=xxx，从 query 里取 token
        String token = null;
        String query = session.getQueryString();
        if (query != null) {
            for (String pair : query.split("&")) {
                String[] kv = pair.split("=", 2);
                if ("token".equals(kv[0]) && kv.length == 2) {
                    token = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                }
            }
        }
        // 校验不通过直接关闭连接
        if (token == null || !JwtUtil.verifyToken(token)) {
            try { session.close(); } catch (IOException ignored) {}
            return;
        }
        currentUserId = JwtUtil.getUserId(token);
        CLIENTS.put(currentUserId, session);
    }

    @OnClose
    public void onClose(Session session) {
        if (currentUserId != null) CLIENTS.remove(currentUserId);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        if (currentUserId != null) CLIENTS.remove(currentUserId);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        // 留给客户端发心跳，业务上不处理
    }

    // 给指定用户推送，在线返回 true；不在线返回 false（消息仍已入库，可稍后拉取）
    public static boolean sendToUser(Long userId, String message) {
        Session session = CLIENTS.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
                return true;
            } catch (IOException e) {
                return false;
            }
        }
        return false;
    }
}
