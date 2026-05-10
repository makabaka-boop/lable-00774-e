package com.alert.service;

import com.alert.entity.AlertRule;
import com.alert.mapper.AlertRuleMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AlertRuleService extends ServiceImpl<AlertRuleMapper, AlertRule> {

    public Page<AlertRule> pageList(int page, int size, String keyword, Integer status) {
        LambdaQueryWrapper<AlertRule> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(AlertRule::getName, keyword);
        }
        if (status != null) {
            wrapper.eq(AlertRule::getStatus, status);
        }
        wrapper.orderByDesc(AlertRule::getCreatedAt);
        return page(new Page<>(page, size), wrapper);
    }

    public List<AlertRule> getActiveRules() {
        return list(new LambdaQueryWrapper<AlertRule>().eq(AlertRule::getStatus, 1));
    }
}
