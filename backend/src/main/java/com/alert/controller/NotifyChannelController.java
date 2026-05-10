package com.alert.controller;

import com.alert.aop.OperationLog;
import com.alert.common.Result;
import com.alert.entity.NotifyChannel;
import com.alert.security.UserPrincipal;
import com.alert.service.NotifyChannelService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/channels")
public class NotifyChannelController {
    private final NotifyChannelService notifyChannelService;

    public NotifyChannelController(NotifyChannelService notifyChannelService) {
        this.notifyChannelService = notifyChannelService;
    }

    @GetMapping
    public Result<Page<NotifyChannel>> list(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(required = false) String type) {
        return Result.success(notifyChannelService.pageList(page, size, type));
    }

    @GetMapping("/all")
    public Result<List<NotifyChannel>> listAll() {
        List<NotifyChannel> list = notifyChannelService.list();
        list.forEach(c -> c.setConfig("******"));
        return Result.success(list);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog("创建通知渠道")
    public Result<NotifyChannel> create(@RequestBody NotifyChannel channel,
                                        @AuthenticationPrincipal UserPrincipal principal) {
        channel.setCreatedBy(principal.getUserId());
        return Result.success(notifyChannelService.saveChannel(channel));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog("更新通知渠道")
    public Result<NotifyChannel> update(@PathVariable Long id, @RequestBody NotifyChannel channel) {
        channel.setId(id);
        return Result.success(notifyChannelService.saveChannel(channel));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog("删除通知渠道")
    public Result<?> delete(@PathVariable Long id) {
        notifyChannelService.removeById(id);
        return Result.success();
    }
}
