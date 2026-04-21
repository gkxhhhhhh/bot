package com.example.btcbot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.btcbot.entity.BotRuntimeStateEntity;
import com.example.btcbot.enums.BotStageEnum;
import com.example.btcbot.mapper.BotRuntimeStateMapper;
import com.example.btcbot.service.RuntimeStateService;
import org.springframework.stereotype.Service;

@Service
public class RuntimeStateServiceImpl implements RuntimeStateService {

    private final BotRuntimeStateMapper runtimeStateMapper;

    public RuntimeStateServiceImpl(BotRuntimeStateMapper runtimeStateMapper) {
        this.runtimeStateMapper = runtimeStateMapper;
    }

    @Override
    public BotRuntimeStateEntity getBySymbol(String symbol) {
        return runtimeStateMapper.selectOne(new LambdaQueryWrapper<BotRuntimeStateEntity>()
                .eq(BotRuntimeStateEntity::getSymbol, symbol)
                .last("limit 1"));
    }

    @Override
    public BotRuntimeStateEntity initIfAbsent(String symbol) {
        BotRuntimeStateEntity entity = getBySymbol(symbol);
        if (entity != null) {
            return entity;
        }
        BotRuntimeStateEntity init = new BotRuntimeStateEntity();
        init.setSymbol(symbol);
        init.setStage(BotStageEnum.IDLE.name());
        runtimeStateMapper.insert(init);
        return runtimeStateMapper.selectById(init.getId());
    }

    @Override
    public void saveOrUpdate(BotRuntimeStateEntity state) {
        if (state.getId() == null) {
            runtimeStateMapper.insert(state);
            return;
        }
        runtimeStateMapper.updateById(state);
    }

    @Override
    public void resetToIdle(String symbol) {
        BotRuntimeStateEntity entity = initIfAbsent(symbol);
        entity.setStage(BotStageEnum.IDLE.name());
        entity.setRuleType(null);
        entity.setBaseAsset(null);
        entity.setQuoteAsset(null);
        entity.setBuyOrderId(null);
        entity.setBuyClientOrderId(null);
        entity.setLimitBuyPrice(null);
        entity.setSellOrderListId(null);
        entity.setTpOrderId(null);
        entity.setSlOrderId(null);
        entity.setEntryPrice(null);
        entity.setEntryQty(null);
        entity.setEntryTime(null);
        entity.setExitExpireTime(null);
        entity.setTakeProfitPrice(null);
        entity.setStopLossPrice(null);
        entity.setH0(null);
        entity.setL0(null);
        entity.setR0(null);
        entity.setP0(null);
        entity.setInvalidationPrice(null);
        entity.setTriggerTime(null);
        runtimeStateMapper.updateById(entity);
    }
}
