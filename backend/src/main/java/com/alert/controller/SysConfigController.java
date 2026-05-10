package com.alert.controller;

import com.alert.aop.OperationLog;
import com.alert.common.Result;
import com.alert.entity.SysConfig;
import com.alert.service.SysConfigService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/configs")
public class SysConfigController {
    private final SysConfigService sysConfigService;

    public SysConfigController(SysConfigService sysConfigService) {
        this.sysConfigService = sysConfigService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<SysConfig>> list() {
        return Result.success(sysConfigService.getAllConfigs());
    }

    @PutMapping("/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog("更新系统配置")
    public Result<?> update(@PathVariable String key, @RequestBody String value) {
        sysConfigService.updateConfig(key, value);
        return Result.success();
    }
}
