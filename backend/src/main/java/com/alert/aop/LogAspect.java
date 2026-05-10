package com.alert.aop;

import com.alibaba.fastjson.JSON;
import com.alert.entity.SysLog;
import com.alert.mapper.SysLogMapper;
import com.alert.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
public class LogAspect {
    private final SysLogMapper sysLogMapper;

    public LogAspect(SysLogMapper sysLogMapper) {
        this.sysLogMapper = sysLogMapper;
    }

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint point, OperationLog operationLog) throws Throwable {
        long start = System.currentTimeMillis();
        SysLog sysLog = new SysLog();
        sysLog.setOperation(operationLog.value());
        sysLog.setMethod(point.getSignature().getDeclaringTypeName() + "." + point.getSignature().getName());
        sysLog.setCreatedAt(LocalDateTime.now());

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof UserPrincipal) {
                UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
                sysLog.setUserId(principal.getUserId());
                sysLog.setUsername(principal.getUsername());
            }
        } catch (Exception ignored) {}

        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                sysLog.setIp(getIpAddr(request));
            }
        } catch (Exception ignored) {}

        try {
            Object[] args = point.getArgs();
            sysLog.setParams(JSON.toJSONString(args));
        } catch (Exception ignored) {}

        Object result;
        try {
            result = point.proceed();
            sysLog.setStatus(1);
        } catch (Throwable e) {
            sysLog.setStatus(0);
            sysLog.setErrorMsg(e.getMessage());
            throw e;
        } finally {
            sysLog.setDuration(System.currentTimeMillis() - start);
            try {
                sysLogMapper.insert(sysLog);
            } catch (Exception e) {
                log.error("保存操作日志失败", e);
            }
        }
        return result;
    }

    private String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
