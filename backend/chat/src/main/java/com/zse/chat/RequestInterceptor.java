package com.zse.chat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class RequestInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        final var requestType = request.getMethod();
        final var requestPath = request.getServletPath();

        if (requestPath.equals("/error")) {
            log.error("{} {}", requestType, requestPath);
        } else {
            log.info("{} {}", requestType, requestPath);
        }
        return true;
    }
}
