package com.example.btcbot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.btcbot.common.BizException;
import com.example.btcbot.dto.BotConfigSaveRequest;
import com.example.btcbot.entity.BotConfigEntity;
import com.example.btcbot.mapper.BotConfigMapper;
import com.example.btcbot.service.BotConfigService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class BotConfigServiceImpl implements BotConfigService {

    private final BotConfigMapper botConfigMapper;

    public BotConfigServiceImpl(BotConfigMapper botConfigMapper) {
        this.botConfigMapper = botConfigMapper;
    }

    @Override
    public Map<String, String> getEnabledConfigMap() {
        LambdaQueryWrapper<BotConfigEntity> wrapper = new LambdaQueryWrapper<BotConfigEntity>()
                .eq(BotConfigEntity::getEnabled, 1)
                .orderByAsc(BotConfigEntity::getId);
        List<BotConfigEntity> list = botConfigMapper.selectList(wrapper);
        Map<String, String> map = new LinkedHashMap<String, String>();
        for (BotConfigEntity entity : list) {
            map.put(entity.getConfigKey(), entity.getConfigValue());
        }
        return map;
    }

    @Override
    public String getRequiredString(String key) {
        String value = getEnabledConfigMap().get(key);
        if (!StringUtils.hasText(value)) {
            throw new BizException("缺少配置项: " + key);
        }
        return value;
    }

    @Override
    public BigDecimal getRequiredBigDecimal(String key) {
        return new BigDecimal(getRequiredString(key));
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = getEnabledConfigMap().get(key);
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }

    @Override
    public BotConfigEntity saveConfig(BotConfigSaveRequest request) {
        BotConfigEntity entity = new BotConfigEntity();
        entity.setId(request.getId());
        entity.setConfigKey(request.getConfigKey());
        entity.setConfigValue(request.getConfigValue());
        entity.setValueType(request.getValueType());
        entity.setRemark(request.getRemark());
        entity.setEnabled(request.getEnabled());
        if (entity.getId() == null) {
            botConfigMapper.insert(entity);
        } else {
            botConfigMapper.updateById(entity);
        }
        return botConfigMapper.selectById(entity.getId());
    }

    @Override
    public Page<BotConfigEntity> page(long current, long size) {
        Page<BotConfigEntity> page = new Page<BotConfigEntity>(current, size);
        return botConfigMapper.selectPage(page, new LambdaQueryWrapper<BotConfigEntity>().orderByAsc(BotConfigEntity::getId));
    }
}
