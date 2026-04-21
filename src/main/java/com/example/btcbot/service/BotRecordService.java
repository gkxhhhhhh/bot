package com.example.btcbot.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.btcbot.entity.BotEventEntity;
import com.example.btcbot.entity.BotExecutionLogEntity;
import com.example.btcbot.entity.BotOrderRecordEntity;
import com.example.btcbot.entity.BotPositionEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public interface BotRecordService {
    BotExecutionLogEntity saveExecutionLog(BotExecutionLogEntity entity);

    BotOrderRecordEntity saveOrderRecord(String symbol, String ruleType, String actionType, String side,
                                         String orderType, Long exchangeOrderId, Long exchangeOrderListId,
                                         String clientOrderId, BigDecimal price, BigDecimal quantity,
                                         BigDecimal quoteAmount, String status, String requestJson,
                                         String responseJson, String remark);

    BotEventEntity saveEvent(String symbol, String eventType, String eventMessage, String payloadJson);

    BotPositionEntity createPosition(String symbol, String ruleType, Long entryOrderRecordId,
                                     Long sellOrderListId, Long tpOrderId, Long slOrderId,
                                     BigDecimal entryPrice, BigDecimal entryQty,
                                     BigDecimal takeProfitPrice, BigDecimal stopLossPrice,
                                     LocalDateTime entryTime, LocalDateTime expireTime);

    void closePositionBySellOrderListId(Long sellOrderListId, BigDecimal closePrice,
                                        LocalDateTime closeTime, String closeReason);

    Page<BotExecutionLogEntity> pageExecutionLogs(long current, long size);

    Page<BotOrderRecordEntity> pageOrderRecords(long current, long size);

    Page<BotEventEntity> pageEvents(long current, long size);

    Page<BotPositionEntity> pagePositions(long current, long size);
}
