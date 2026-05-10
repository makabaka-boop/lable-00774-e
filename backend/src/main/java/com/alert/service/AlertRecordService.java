package com.alert.service;

import com.alert.entity.AlertRecord;
import com.alert.mapper.AlertRecordMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class AlertRecordService extends ServiceImpl<AlertRecordMapper, AlertRecord> {

    public Page<AlertRecord> pageList(int page, int size, Long ruleId, String severity) {
        LambdaQueryWrapper<AlertRecord> wrapper = new LambdaQueryWrapper<>();
        if (ruleId != null) {
            wrapper.eq(AlertRecord::getRuleId, ruleId);
        }
        if (severity != null && !severity.isEmpty()) {
            wrapper.eq(AlertRecord::getSeverity, severity);
        }
        wrapper.orderByDesc(AlertRecord::getTriggeredAt);
        return page(new Page<>(page, size), wrapper);
    }
}
