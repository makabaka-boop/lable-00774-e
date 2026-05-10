package com.alert.service;

import com.alert.entity.NotifyChannel;
import com.alert.mapper.NotifyChannelMapper;
import com.alert.util.EncryptUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class NotifyChannelService extends ServiceImpl<NotifyChannelMapper, NotifyChannel> {
    private final EncryptUtil encryptUtil;

    public NotifyChannelService(EncryptUtil encryptUtil) {
        this.encryptUtil = encryptUtil;
    }

    public NotifyChannel saveChannel(NotifyChannel channel) {
        channel.setConfig(encryptUtil.encrypt(channel.getConfig()));
        saveOrUpdate(channel);
        channel.setConfig("******");
        return channel;
    }

    public Page<NotifyChannel> pageList(int page, int size, String type) {
        LambdaQueryWrapper<NotifyChannel> wrapper = new LambdaQueryWrapper<>();
        if (type != null && !type.isEmpty()) {
            wrapper.eq(NotifyChannel::getType, type);
        }
        Page<NotifyChannel> result = page(new Page<>(page, size), wrapper);
        result.getRecords().forEach(c -> c.setConfig("******"));
        return result;
    }

    public NotifyChannel getDecrypted(Long id) {
        NotifyChannel channel = getById(id);
        if (channel != null) {
            channel.setConfig(encryptUtil.decrypt(channel.getConfig()));
        }
        return channel;
    }
}
