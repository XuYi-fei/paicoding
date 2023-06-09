package com.github.paicoding.forum.web.front.chat.stomp;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.core.util.SpringUtil;
import com.github.paicoding.forum.web.front.chat.helper.WsAnswerHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

import java.security.Principal;

/**
 * 权限拦截器，消息发送前进行拦截
 *
 * @author YiHui
 * @date 2023/6/8
 */
@Slf4j
public class AuthInChannelInterceptor implements ChannelInterceptor {
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        final StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        String destination = accessor.getDestination();
        if (StringUtils.isBlank(destination)) {
            return message;
        }


        Principal uid = accessor.getUser();
        if (uid == null) {
            return message;
        }

        // 正常登录的用户，这个uid实际上应该是 ReqInfo 对象
        log.info("初始化用户标识：{}", uid);

//        注意：这里注释的这种方案，适用于所有的客户端订阅相同的路径，然后请求头中添加用户身份标识，然后再 AuthHandshakeInterceptor 进行身份识别设置全局属性，AuthHandshakeHandler 这里来决定怎么进行转发
//        if (destination.startsWith("/app")) {
//            // 开始进行聊天时，进行身份校验; 路由转发
//            String suffix = destination.substring("/chat".length());
//            String prepareDestination = String.format("%s%s", suffix, uid.getName());
//            accessor.setDestination(prepareDestination);
//        }

        return ChannelInterceptor.super.preSend(message, channel);
    }


    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        final StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (StringUtils.equalsIgnoreCase(String.valueOf(message.getHeaders().get("simpMessageType")), "SUBSCRIBE")
                && accessor != null && accessor.getUser() != null) {
            // 订阅成功，返回用户历史聊天记录
            ReqInfoContext.addReqInfo((ReqInfoContext.ReqInfo) accessor.getUser());
            SpringUtil.getBean(WsAnswerHelper.class).sendMsgHistoryToUser(accessor.getUser().getName());
            ReqInfoContext.clear();
            return;
        }
        ChannelInterceptor.super.postSend(message, channel, sent);
    }

    @Override
    public boolean preReceive(MessageChannel channel) {
        log.info("preReceive!");
        return ChannelInterceptor.super.preReceive(channel);
    }

    @Override
    public Message<?> postReceive(Message<?> message, MessageChannel channel) {
        log.info("postReceive");
        return ChannelInterceptor.super.postReceive(message, channel);
    }
}
