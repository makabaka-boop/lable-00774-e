package com.alert.controller;

import com.alert.common.Result;
import com.alert.entity.AlertRecord;
import com.alert.service.AlertRecordService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/records")
public class AlertRecordController {
    private final AlertRecordService alertRecordService;

    public AlertRecordController(AlertRecordService alertRecordService) {
        this.alertRecordService = alertRecordService;
    }

    @GetMapping
    public Result<Page<AlertRecord>> list(@RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "10") int size,
                                          @RequestParam(required = false) Long ruleId,
                                          @RequestParam(required = false) String severity) {
        return Result.success(alertRecordService.pageList(page, size, ruleId, severity));
    }

    @GetMapping("/{id}")
    public Result<AlertRecord> get(@PathVariable Long id) {
        return Result.success(alertRecordService.getById(id));
    }
}
