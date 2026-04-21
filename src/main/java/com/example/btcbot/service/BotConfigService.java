package com.example.btcbot.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.btcbot.dto.BotConfigSaveRequest;
import com.example.btcbot.entity.BotConfigEntity;

import java.math.BigDecimal;
import java.util.Map;

public interface BotConfigService {
    Map<String, String> getEnabledConfigMap();

    String getRequiredString(String key);

    BigDecimal getRequiredBigDecimal(String key);

    boolean getBoolean(String key, boolean defaultValue);

    BotConfigEntity saveConfig(BotConfigSaveRequest request);

    Page<BotConfigEntity> page(long current, long size);
}
