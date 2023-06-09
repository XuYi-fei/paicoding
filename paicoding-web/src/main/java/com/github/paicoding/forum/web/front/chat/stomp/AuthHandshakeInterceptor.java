package com.github.paicoding.forum.web.front.chat.stomp;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.core.mdc.MdcUtil;
import com.github.paicoding.forum.core.mdc.SelfTraceIdGenerator;
import com.github.paicoding.forum.core.util.JsonUtil;
import com.github.paicoding.forum.core.util.SessionUtil;
import com.github.paicoding.forum.core.util.SpringUtil;
import com.github.paicoding.forum.service.user.service.SessionService;
import com.github.paicoding.forum.web.global.GlobalInitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 握手拦截器, 用于身份验证识别
 *
 * @author YiHui
 * @date 2023/6/8
 */
@Slf4j
public class AuthHandshakeInterceptor extends HttpSessionHandshakeInterceptor {

    /**
     * 握手前，进行用户身份校验识别
     *
     * @param request
     * @param response
     * @param wsHandler
     * @param attributes
     * @return
     * @throws Exception
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        log.info("准备开始握手了!");
        String session = SessionUtil.findCookieByName(request, SessionService.SESSION_KEY);
        ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
        SpringUtil.getBean(GlobalInitService.class).initLoginUser(session, reqInfo);

        if (reqInfo.getUser() == null) {
            log.info("websocket 握手失败，请登录之后再试");
            return false;
        }

        // 将用户信息写入到属性中
        attributes.put(MdcUtil.TRACE_ID_KEY, SelfTraceIdGenerator.generate());
        attributes.put(SessionService.SESSION_KEY, reqInfo);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception ex) {
        log.info("握手成功了!!!");
        super.afterHandshake(request, response, wsHandler, ex);
    }
}
