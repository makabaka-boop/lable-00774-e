package com.alert.controller;

import com.alert.common.Result;
import com.alert.entity.SysLog;
import com.alert.mapper.SysLogMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/logs")
public class SysLogController {
    private final SysLogMapper sysLogMapper;

    public SysLogController(SysLogMapper sysLogMapper) {
        this.sysLogMapper = sysLogMapper;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<SysLog>> list(@RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "10") int size,
                                     @RequestParam(required = false) String username) {
        LambdaQueryWrapper<SysLog> wrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            wrapper.like(SysLog::getUsername, username);
        }
        wrapper.orderByDesc(SysLog::getCreatedAt);
        return Result.success(sysLogMapper.selectPage(new Page<>(page, size), wrapper));
    }
}
