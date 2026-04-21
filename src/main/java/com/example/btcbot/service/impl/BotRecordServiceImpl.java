package com.example.btcbot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.btcbot.entity.BotEventEntity;
import com.example.btcbot.entity.BotExecutionLogEntity;
import com.example.btcbot.entity.BotOrderRecordEntity;
import com.example.btcbot.entity.BotPositionEntity;
import com.example.btcbot.enums.PositionStatusEnum;
import com.example.btcbot.mapper.BotEventMapper;
import com.example.btcbot.mapper.BotExecutionLogMapper;
import com.example.btcbot.mapper.BotOrderRecordMapper;
import com.example.btcbot.mapper.BotPositionMapper;
import com.example.btcbot.service.BotRecordService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
public class BotRecordServiceImpl implements BotRecordService {

    private final BotExecutionLogMapper executionLogMapper;
    private final BotOrderRecordMapper orderRecordMapper;
    private final BotEventMapper botEventMapper;
    private final BotPositionMapper botPositionMapper;

    public BotRecordServiceImpl(BotExecutionLogMapper executionLogMapper,
                                BotOrderRecordMapper orderRecordMapper,
                                BotEventMapper botEventMapper,
                                BotPositionMapper botPositionMapper) {
        this.executionLogMapper = executionLogMapper;
        this.orderRecordMapper = orderRecordMapper;
        this.botEventMapper = botEventMapper;
        this.botPositionMapper = botPositionMapper;
    }

    @Override
    public BotExecutionLogEntity saveExecutionLog(BotExecutionLogEntity entity) {
        executionLogMapper.insert(entity);
        return entity;
    }

    @Override
    public BotOrderRecordEntity saveOrderRecord(String symbol, String ruleType, String actionType, String side,
                                                String orderType, Long exchangeOrderId, Long exchangeOrderListId,
                                                String clientOrderId, BigDecimal price, BigDecimal quantity,
                                                BigDecimal quoteAmount, String status, String requestJson,
                                                String responseJson, String remark) {
        BotOrderRecordEntity entity = new BotOrderRecordEntity();
        entity.setSymbol(symbol);
        entity.setRuleType(ruleType);
        entity.setActionType(actionType);
        entity.setSide(side);
        entity.setOrderType(orderType);
        entity.setExchangeOrderId(exchangeOrderId);
        entity.setExchangeOrderListId(exchangeOrderListId);
        entity.setClientOrderId(clientOrderId);
        entity.setPrice(price);
        entity.setQuantity(quantity);
        entity.setQuoteAmount(quoteAmount);
        entity.setStatus(status);
        entity.setRequestJson(requestJson);
        entity.setResponseJson(responseJson);
        entity.setRemark(remark);
        orderRecordMapper.insert(entity);
        return entity;
    }

    @Override
    public BotEventEntity saveEvent(String symbol, String eventType, String eventMessage, String payloadJson) {
        BotEventEntity entity = new BotEventEntity();
        entity.setSymbol(symbol);
        entity.setEventType(eventType);
        entity.setEventMessage(eventMessage);
        entity.setPayloadJson(payloadJson);
        botEventMapper.insert(entity);
        return entity;
    }

    @Override
    public BotPositionEntity createPosition(String symbol, String ruleType, Long entryOrderRecordId,
                                            Long sellOrderListId, Long tpOrderId, Long slOrderId,
                                            BigDecimal entryPrice, BigDecimal entryQty,
                                            BigDecimal takeProfitPrice, BigDecimal stopLossPrice,
                                            LocalDateTime entryTime, LocalDateTime expireTime) {
        BotPositionEntity entity = new BotPositionEntity();
        entity.setSymbol(symbol);
        entity.setRuleType(ruleType);
        entity.setEntryOrderRecordId(entryOrderRecordId);
        entity.setSellOrderListId(sellOrderListId);
        entity.setTpOrderId(tpOrderId);
        entity.setSlOrderId(slOrderId);
        entity.setEntryPrice(entryPrice);
        entity.setEntryQty(entryQty);
        entity.setTakeProfitPrice(takeProfitPrice);
        entity.setStopLossPrice(stopLossPrice);
        entity.setEntryTime(entryTime);
        entity.setExpireTime(expireTime);
        entity.setStatus(PositionStatusEnum.OPEN.name());
        botPositionMapper.insert(entity);
        return entity;
    }

    @Override
    public void closePositionBySellOrderListId(Long sellOrderListId, BigDecimal closePrice,
                                               LocalDateTime closeTime, String closeReason) {
        BotPositionEntity position = botPositionMapper.selectOne(new LambdaQueryWrapper<BotPositionEntity>()
                .eq(BotPositionEntity::getSellOrderListId, sellOrderListId)
                .eq(BotPositionEntity::getStatus, PositionStatusEnum.OPEN.name())
                .last("limit 1"));
        if (position == null) {
            return;
        }
        position.setStatus(PositionStatusEnum.CLOSED.name());
        position.setClosePrice(closePrice);
        position.setCloseTime(closeTime);
        position.setCloseReason(closeReason);
        if (position.getEntryPrice() != null && position.getEntryQty() != null
                && closePrice != null && position.getEntryQty().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal profitAmount = closePrice.subtract(position.getEntryPrice()).multiply(position.getEntryQty());
            position.setProfitAmount(profitAmount);
            if (position.getEntryPrice().compareTo(BigDecimal.ZERO) > 0) {
                position.setProfitRate(closePrice.subtract(position.getEntryPrice())
                        .divide(position.getEntryPrice(), 8, RoundingMode.HALF_UP));
            }
        }
        botPositionMapper.updateById(position);
    }

    @Override
    public Page<BotExecutionLogEntity> pageExecutionLogs(long current, long size) {
        return executionLogMapper.selectPage(new Page<BotExecutionLogEntity>(current, size),
                new LambdaQueryWrapper<BotExecutionLogEntity>().orderByDesc(BotExecutionLogEntity::getId));
    }

    @Override
    public Page<BotOrderRecordEntity> pageOrderRecords(long current, long size) {
        return orderRecordMapper.selectPage(new Page<BotOrderRecordEntity>(current, size),
                new LambdaQueryWrapper<BotOrderRecordEntity>().orderByDesc(BotOrderRecordEntity::getId));
    }

    @Override
    public Page<BotEventEntity> pageEvents(long current, long size) {
        return botEventMapper.selectPage(new Page<BotEventEntity>(current, size),
                new LambdaQueryWrapper<BotEventEntity>().orderByDesc(BotEventEntity::getId));
    }

    @Override
    public Page<BotPositionEntity> pagePositions(long current, long size) {
        return botPositionMapper.selectPage(new Page<BotPositionEntity>(current, size),
                new LambdaQueryWrapper<BotPositionEntity>().orderByDesc(BotPositionEntity::getId));
    }
}
