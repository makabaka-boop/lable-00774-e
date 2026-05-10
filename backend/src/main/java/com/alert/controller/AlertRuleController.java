package com.alert.controller;

import com.alert.aop.OperationLog;
import com.alert.common.Result;
import com.alert.entity.AlertRule;
import com.alert.security.UserPrincipal;
import com.alert.service.AlertCheckService;
import com.alert.service.AlertRuleService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rules")
public class AlertRuleController {
    private final AlertRuleService alertRuleService;
    private final AlertCheckService alertCheckService;

    public AlertRuleController(AlertRuleService alertRuleService, AlertCheckService alertCheckService) {
        this.alertRuleService = alertRuleService;
        this.alertCheckService = alertCheckService;
    }

    @GetMapping
    public Result<Page<AlertRule>> list(@RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "10") int size,
                                        @RequestParam(required = false) String keyword,
                                        @RequestParam(required = false) Integer status) {
        return Result.success(alertRuleService.pageList(page, size, keyword, status));
    }

    @GetMapping("/{id}")
    public Result<AlertRule> get(@PathVariable Long id) {
        return Result.success(alertRuleService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog("创建告警规则")
    public Result<AlertRule> create(@RequestBody AlertRule rule,
                                    @AuthenticationPrincipal UserPrincipal principal) {
        rule.setCreatedBy(principal.getUserId());
        alertRuleService.save(rule);
        return Result.success(rule);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog("更新告警规则")
    public Result<AlertRule> update(@PathVariable Long id, @RequestBody AlertRule rule) {
        rule.setId(id);
        alertRuleService.updateById(rule);
        return Result.success(rule);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog("删除告警规则")
    public Result<?> delete(@PathVariable Long id) {
        alertRuleService.removeById(id);
        return Result.success();
    }

    @PostMapping("/{id}/check")
    @OperationLog("手动触发告警检查")
    public Result<?> manualCheck(@PathVariable Long id) {
        alertCheckService.manualCheck(id);
        return Result.success();
    }
}
