package com.example.btcbot.service.impl;

import com.example.btcbot.common.BizException;
import com.example.btcbot.dto.ExecutionResultVO;
import com.example.btcbot.dto.binance.Recent24hRangeDTO;
import com.example.btcbot.entity.BotExecutionLogEntity;
import com.example.btcbot.entity.BotOrderRecordEntity;
import com.example.btcbot.entity.BotRuntimeStateEntity;
import com.example.btcbot.enums.BotStageEnum;
import com.example.btcbot.enums.ExecutionDecisionEnum;
import com.example.btcbot.enums.OrderActionEnum;
import com.example.btcbot.service.*;
import com.example.btcbot.util.BigDecimalHelper;
import com.example.btcbot.util.JsonHelper;
import com.example.btcbot.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class BotExecutionServiceImpl implements BotExecutionService {

    private final BotConfigService botConfigService;
    private final RuntimeStateService runtimeStateService;
    private final BinanceClientService binanceClientService;
    private final BotRecordService botRecordService;
    private final BotMessageService botMessageService;
    private final JsonHelper jsonHelper;

    public BotExecutionServiceImpl(BotConfigService botConfigService,
                                   RuntimeStateService runtimeStateService,
                                   BinanceClientService binanceClientService,
                                   BotRecordService botRecordService,
                                   BotMessageService botMessageService,
                                   JsonHelper jsonHelper) {
        this.botConfigService = botConfigService;
        this.runtimeStateService = runtimeStateService;
        this.binanceClientService = binanceClientService;
        this.botRecordService = botRecordService;
        this.botMessageService = botMessageService;
        this.jsonHelper = jsonHelper;
    }

    @Override
    public ExecutionResultVO runOnce(String symbol) {
        String actualSymbol = StringUtils.hasText(symbol) ? symbol : botConfigService.getRequiredString("BINANCE_SYMBOL");
        if (!botConfigService.getBoolean("BOT_ENABLE", true)) {
            return new ExecutionResultVO(ExecutionDecisionEnum.NO_ACTION.name(), "机器人已关闭");
        }
        log.info("当前开始执行本次定时任务！");
        BotRuntimeStateEntity state = runtimeStateService.initIfAbsent(actualSymbol);
        StrategyConfig config = loadConfig();
        log.info("当前阶段：" + BotStageEnum.getDescByName(state.getStage()));
        if (BotStageEnum.IDLE.name().equals(state.getStage())) {
            return handleIdle(actualSymbol, state, config);
        }
        if (BotStageEnum.RULE4_PENDING_BUY.name().equals(state.getStage())) {
            return handleRule4Pending(actualSymbol, state, config);
        }
        if (BotStageEnum.HOLDING.name().equals(state.getStage())) {
            return handleHolding(actualSymbol, state, config);
        }
        errorEvent(symbol, "ERROR", "未知阶段: " + state.getStage(), null);
        throw new BizException("未知阶段: " + state.getStage());
    }

    private ExecutionResultVO handleIdle(String symbol, BotRuntimeStateEntity state, StrategyConfig config) {
        Recent24hRangeDTO range = binanceClientService.getRecent24hRange(symbol);

        // 获取交易对的交易规则（价格精度、数量精度、最小下单量、最小名义金额等）
        // 后续计算限价单价格、下单数量时，需要按照这个规则做截断和校验
        Map<String, Object> exchangeInfo = binanceClientService.getExchangeInfo(symbol);
        SymbolRule symbolRule = parseSymbolRule(exchangeInfo);

        // 最近24小时最高价 H
        BigDecimal high = range.getHighPrice();

        // 最近24小时最低价 L
        BigDecimal low = range.getLowPrice();

        // 当前价格（最新价）
        BigDecimal current = range.getCurrentPrice();

        // 区间宽度 R = H - L
        BigDecimal subtract = high.subtract(low);

        // 规则3上沿价格 = L + 0.25R
        // 当当前价落在 [L, L + 0.25R] 之间时，认为进入下沿买入区，可触发规则3
        BigDecimal rule3Upper = low.add(subtract.multiply(config.rule3UpperRatio));

        // 区间中点 = L + 0.50R
        // 规则4要求当前价必须在中点或中点以下
        BigDecimal middle = low.add(subtract.multiply(config.middleRatio));

        // 规则4挂单失效价 = L + 0.75R
        // 如果规则4限价买单一直没成交，且价格上涨到这个位置，则认为机会失效，应撤单
        BigDecimal invalidationPrice = low.add(subtract.multiply(config.invalidRatio));

        // 24小时波动百分比 = (H - L) / 当前价 * 100
        // 用来判断最近24小时波动是否足够大，是否满足规则4的最小波动要求
        BigDecimal volatilityPercent = BigDecimalHelper.volatilityPercent(high, low, current);

        // 规则3是否命中：
        // 条件：当前价 >= L 且 当前价 <= L + 0.25R
        // 含义：当前价格已经进入你定义的“下沿买入区”
        boolean rule3Hit = current.compareTo(low) >= 0 && current.compareTo(rule3Upper) <= 0;

        // 规则4-1是否命中：
        // 条件：当前价 <= 中点
        // 含义：当前价格至少位于区间中部或以下，而不是高位
        boolean rule41Hit = current.compareTo(middle) <= 0;

        // 规则4-2是否命中：
        // 条件：24小时波动百分比 > 配置的最小波动阈值（例如 1.5%）
        // 含义：近期波动足够，具备做规则4挂低吸单的价值
        boolean rule42Hit = volatilityPercent.compareTo(config.minVolatilityPercent) > 0;

        // 规则4最终是否命中：
        // 必须同时满足：
        // 1）当前价在中点或以下
        // 2）24小时波动超过最小阈值
        boolean rule4Hit = rule41Hit && rule42Hit;
        log.info("策略参数开始，交易对：{}", symbol);
        log.info("24小时价格区间：最高价H={}, 最低价L={}, 当前价={}, 区间宽度R={}", high, low, current, subtract);
        log.info("关键位置参数：规则3上沿(L+0.25R)={}, 中点(L+0.50R)={}, 规则4失效价(L+0.75R)={}", rule3Upper, middle, invalidationPrice);
        log.info("波动参数：24小时波动百分比={}，最小波动阈值={}", volatilityPercent, config.minVolatilityPercent);
        log.info("规则命中情况：规则3是否命中={}，规则4-1是否命中(当前价<=中点)={}，规则4-2是否命中(波动>{})={}，规则4是否最终命中={}",
                rule3Hit,
                rule41Hit,
                config.minVolatilityPercent,
                rule42Hit,
                rule4Hit);
        log.info("策略参数结束，交易对：{}", symbol);
        List<String> metReasons = new ArrayList<String>();
        List<String> unmetReasons = new ArrayList<String>();
        if (rule3Hit) {
            metReasons.add("规则3满足：当前价位于[L, L + 0.25R]");
        } else {
            unmetReasons.add("规则3不满足：当前价未进入下沿买入区");
        }
        if (rule41Hit) {
            metReasons.add("规则4-1满足：当前价 <= L + 0.50R");
        } else {
            unmetReasons.add("规则4-1不满足：当前价高于中点");
        }
        if (rule42Hit) {
            metReasons.add("规则4-2满足：24小时波动超过阈值");
        } else {
            unmetReasons.add("规则4-2不满足：24小时波动未超过阈值");
        }

        BotExecutionLogEntity logEntity = new BotExecutionLogEntity();
        logEntity.setSymbol(symbol);
        logEntity.setHighPrice(high);
        logEntity.setLowPrice(low);
        logEntity.setCurrentPrice(current);
        logEntity.setRangeValue(subtract);
        logEntity.setRule3UpperPrice(rule3Upper);
        logEntity.setMiddlePrice(middle);
        logEntity.setInvalidationPrice(invalidationPrice);
        logEntity.setVolatilityPercent(volatilityPercent);
        logEntity.setMetReasons(jsonHelper.toJson(metReasons));
        logEntity.setUnmetReasons(jsonHelper.toJson(unmetReasons));

        if (config.singleOrderU.compareTo(symbolRule.minNotional) < 0) {
            errorEvent(symbol, "ERROR", "单次下单金额小于minNotional，singleOrderU=" + config.singleOrderU.toPlainString(), null);
            throw new BizException("单次下单金额小于minNotional，singleOrderU=" + config.singleOrderU.toPlainString());
        }

        if (rule3Hit) {
            String clientOrderId = buildClientOrderId("r3m");
            Map<String, Object> buyResp = binanceClientService.placeMarketBuyByQuote(symbol, config.singleOrderU, clientOrderId);
            FilledTrade buyTrade = parseFilledTrade(buyResp);
            BotOrderRecordEntity buyRecord = botRecordService.saveOrderRecord(symbol, "RULE3",
                    OrderActionEnum.MARKET_BUY.name(), "BUY", "MARKET",
                    toLong(buyResp.get("orderId")), null, clientOrderId,
                    buyTrade.avgPrice, buyTrade.executedQty, config.singleOrderU,
                    String.valueOf(buyResp.get("status")), null, jsonHelper.toJson(buyResp), "规则3市价买入");

            BigDecimal freeBase = fetchFreeBalance(symbolRule.baseAsset);
            BigDecimal sellQty = BigDecimalHelper.roundDown(freeBase, symbolRule.lotStepSize);
            if (sellQty.compareTo(symbolRule.minQty) < 0) {
                errorEvent(symbol, "ERROR", "规则3买入后可卖数量不足minQty，sellQty=" + sellQty.toPlainString(), null);
                throw new BizException("规则3买入后可卖数量不足minQty，sellQty=" + sellQty.toPlainString());

            }
            BigDecimal tp = BigDecimalHelper.roundDown(buyTrade.avgPrice.multiply(BigDecimal.ONE.add(config.takeProfitRate)), symbolRule.tickSize);
            BigDecimal slStop = BigDecimalHelper.roundDown(buyTrade.avgPrice.multiply(BigDecimal.ONE.subtract(config.stopLossRate)), symbolRule.tickSize);
            BigDecimal slLimit = BigDecimalHelper.roundDown(slStop.multiply(BigDecimal.ONE.subtract(config.stopLimitGapRate)), symbolRule.tickSize);
            String ocoClientId = buildClientOrderId("oco");
            String tpClientId = buildClientOrderId("tp");
            String slClientId = buildClientOrderId("sl");
            Map<String, Object> ocoResp = binanceClientService.placeOcoSell(symbol, sellQty, tp, slStop, slLimit, ocoClientId, tpClientId, slClientId);
            Long orderListId = toLong(ocoResp.get("orderListId"));
            Long tpOrderId = findChildOrderId(ocoResp, tpClientId);
            Long slOrderId = findChildOrderId(ocoResp, slClientId);
            botRecordService.saveOrderRecord(symbol, "RULE3", OrderActionEnum.OCO_SELL.name(), "SELL", "OCO",
                    null, orderListId, ocoClientId, null, sellQty, null, "NEW", null, jsonHelper.toJson(ocoResp), "规则3 OCO保护单");
            botRecordService.createPosition(symbol, "RULE3", buyRecord.getId(), orderListId, tpOrderId, slOrderId,
                    buyTrade.avgPrice, sellQty, tp, slStop, LocalDateTime.now(), LocalDateTime.now().plusHours(24));

            state.setStage(BotStageEnum.HOLDING.name());
            state.setRuleType("RULE3");
            state.setBaseAsset(symbolRule.baseAsset);
            state.setQuoteAsset(symbolRule.quoteAsset);
            state.setEntryPrice(buyTrade.avgPrice);
            state.setEntryQty(sellQty);
            state.setEntryTime(LocalDateTime.now());
            state.setExitExpireTime(LocalDateTime.now().plusHours(24));
            state.setSellOrderListId(orderListId);
            state.setTpOrderId(tpOrderId);
            state.setSlOrderId(slOrderId);
            state.setTakeProfitPrice(tp);
            state.setStopLossPrice(slStop);
            runtimeStateService.saveOrUpdate(state);

            logEntity.setDecision(ExecutionDecisionEnum.RULE3_MARKET_BUY.name());
            botRecordService.saveExecutionLog(logEntity);
            notifyEvent(symbol, "BUY_FILLED", "【买入成交】规则3现价买入成功，成交均价=" + buyTrade.avgPrice.toPlainString()
                    + "，数量=" + sellQty.toPlainString() + "，TP=" + tp.toPlainString() + "，SL=" + slStop.toPlainString(), buyResp);
            return new ExecutionResultVO(ExecutionDecisionEnum.RULE3_MARKET_BUY.name(), "规则3已买入并挂保护单");
        }

        if (rule4Hit) {
            BigDecimal limitBuyPrice = BigDecimalHelper.roundDown(current.multiply(BigDecimal.ONE.subtract(config.rule4PullbackRate)), symbolRule.tickSize);
            BigDecimal buyQty = BigDecimalHelper.roundDown(config.singleOrderU.divide(limitBuyPrice, 16, RoundingMode.DOWN), symbolRule.lotStepSize);
            if (buyQty.compareTo(symbolRule.minQty) < 0) {
                errorEvent(symbol, "ERROR", "规则4买单数量不足minQty，buyQty=" + buyQty.toPlainString(), null);
                throw new BizException("规则4买单数量不足minQty，buyQty=" + buyQty.toPlainString());
            }
            String clientOrderId = buildClientOrderId("r4b");
            Map<String, Object> buyResp = binanceClientService.placeLimitBuy(symbol, buyQty, limitBuyPrice, clientOrderId);
            botRecordService.saveOrderRecord(symbol, "RULE4", OrderActionEnum.LIMIT_BUY.name(), "BUY", "LIMIT",
                    toLong(buyResp.get("orderId")), null, clientOrderId, limitBuyPrice, buyQty,
                    config.singleOrderU, String.valueOf(buyResp.get("status")), null, jsonHelper.toJson(buyResp), "规则4限价买入");

            state.setStage(BotStageEnum.RULE4_PENDING_BUY.name());
            state.setRuleType("RULE4");
            state.setBaseAsset(symbolRule.baseAsset);
            state.setQuoteAsset(symbolRule.quoteAsset);
            state.setBuyOrderId(toLong(buyResp.get("orderId")));
            state.setBuyClientOrderId(clientOrderId);
            state.setLimitBuyPrice(limitBuyPrice);
            state.setEntryQty(buyQty);
            state.setH0(high);
            state.setL0(low);
            state.setR0(subtract);
            state.setP0(current);
            state.setInvalidationPrice(invalidationPrice);
            state.setTriggerTime(LocalDateTime.now());
            runtimeStateService.saveOrUpdate(state);

            logEntity.setDecision(ExecutionDecisionEnum.RULE4_LIMIT_BUY.name());
            botRecordService.saveExecutionLog(logEntity);
            return new ExecutionResultVO(ExecutionDecisionEnum.RULE4_LIMIT_BUY.name(), "规则4已挂限价买单");
        }

        logEntity.setDecision(ExecutionDecisionEnum.NO_ACTION.name());
        botRecordService.saveExecutionLog(logEntity);
        return new ExecutionResultVO(ExecutionDecisionEnum.NO_ACTION.name(), "当前无交易动作");
    }

    private ExecutionResultVO handleRule4Pending(String symbol, BotRuntimeStateEntity state, StrategyConfig config) {
        if (!StringUtils.hasText(symbol)) {
            log.error("规则4待买阶段查询订单失败，symbol为空，buyOrderId={}", state.getBuyOrderId());
            errorEvent(symbol, "QUERY_ORDER_ERROR", "规则4待买阶段查询订单失败：symbol为空", state);
            return new ExecutionResultVO(ExecutionDecisionEnum.NO_ACTION.name(), "symbol为空，无法查询挂单");
        }

        symbol = symbol.trim();

        if (!StringUtils.hasText(symbol)) {
            log.error("规则4待买阶段查询订单失败，symbol去空格后为空，buyOrderId={}", state.getBuyOrderId());
            errorEvent(symbol, "QUERY_ORDER_ERROR", "规则4待买阶段查询订单失败：symbol去空格后为空", state);
            return new ExecutionResultVO(ExecutionDecisionEnum.NO_ACTION.name(), "symbol为空，无法查询挂单");
        }

        if (state.getBuyOrderId() == null) {
            log.error("规则4待买阶段查询订单失败，buyOrderId为空，symbol={}", symbol);
            errorEvent(symbol, "QUERY_ORDER_ERROR", "规则4待买阶段查询订单失败：buyOrderId为空，已重置为空闲状态", state);
            runtimeStateService.resetToIdle(symbol);
            return new ExecutionResultVO(ExecutionDecisionEnum.NO_ACTION.name(), "buyOrderId为空，已重置为空闲状态");
        }

        log.info("规则4待买阶段开始查询订单，symbol={}, buyOrderId={}", symbol, state.getBuyOrderId());

        Map<String, Object> orderResp = binanceClientService.queryOrder(symbol, state.getBuyOrderId());
        log.info("规则4待买阶段查询订单结束，symbol={}, buyOrderId={}，result:{}", symbol, state.getBuyOrderId(), JsonUtils.toJson(orderResp));

        String status = String.valueOf(orderResp.get("status"));
        if ("FILLED".equals(status) || "PARTIALLY_FILLED".equals(status)) {
            if ("PARTIALLY_FILLED".equals(status)) {
                Map<String, Object> cancelResp = binanceClientService.cancelOrder(symbol, state.getBuyOrderId());
                botRecordService.saveOrderRecord(symbol, state.getRuleType(), OrderActionEnum.CANCEL_BUY.name(), "BUY", "LIMIT",
                        state.getBuyOrderId(), null, state.getBuyClientOrderId(), state.getLimitBuyPrice(), state.getEntryQty(),
                        null, "CANCELED", null, jsonHelper.toJson(cancelResp), "规则4部分成交，先撤余单");
                orderResp = binanceClientService.queryOrder(symbol, state.getBuyOrderId());
            }
            Map<String, Object> exchangeInfo = binanceClientService.getExchangeInfo(symbol);
            SymbolRule symbolRule = parseSymbolRule(exchangeInfo);
            FilledTrade buyTrade = parseFilledTrade(orderResp);
            BigDecimal freeBase = fetchFreeBalance(symbolRule.baseAsset);
            BigDecimal sellQty = BigDecimalHelper.roundDown(freeBase, symbolRule.lotStepSize);
            if (sellQty.compareTo(symbolRule.minQty) < 0) {
                errorEvent(symbol, "ERROR", "规则3买入后可卖数量不足minQty，sellQty=" + sellQty.toPlainString(), null);
                throw new BizException("规则3买入后可卖数量不足minQty，sellQty=" + sellQty.toPlainString());
            }
            BigDecimal tp = BigDecimalHelper.roundDown(buyTrade.avgPrice.multiply(BigDecimal.ONE.add(config.takeProfitRate)), symbolRule.tickSize);
            BigDecimal slStop = BigDecimalHelper.roundDown(buyTrade.avgPrice.multiply(BigDecimal.ONE.subtract(config.stopLossRate)), symbolRule.tickSize);
            BigDecimal slLimit = BigDecimalHelper.roundDown(slStop.multiply(BigDecimal.ONE.subtract(config.stopLimitGapRate)), symbolRule.tickSize);
            String ocoClientId = buildClientOrderId("oco");
            String tpClientId = buildClientOrderId("tp");
            String slClientId = buildClientOrderId("sl");
            Map<String, Object> ocoResp = binanceClientService.placeOcoSell(symbol, sellQty, tp, slStop, slLimit, ocoClientId, tpClientId, slClientId);
            Long orderListId = toLong(ocoResp.get("orderListId"));
            Long tpOrderId = findChildOrderId(ocoResp, tpClientId);
            Long slOrderId = findChildOrderId(ocoResp, slClientId);

            botRecordService.saveOrderRecord(symbol, state.getRuleType(), OrderActionEnum.OCO_SELL.name(), "SELL", "OCO",
                    null, orderListId, ocoClientId, null, sellQty, null, "NEW", null, jsonHelper.toJson(ocoResp), "规则4成交后挂OCO");
            botRecordService.createPosition(symbol, state.getRuleType(), null, orderListId, tpOrderId, slOrderId,
                    buyTrade.avgPrice, sellQty, tp, slStop, LocalDateTime.now(), LocalDateTime.now().plusHours(24));

            state.setStage(BotStageEnum.HOLDING.name());
            state.setEntryPrice(buyTrade.avgPrice);
            state.setEntryQty(sellQty);
            state.setEntryTime(LocalDateTime.now());
            state.setExitExpireTime(LocalDateTime.now().plusHours(24));
            state.setSellOrderListId(orderListId);
            state.setTpOrderId(tpOrderId);
            state.setSlOrderId(slOrderId);
            state.setTakeProfitPrice(tp);
            state.setStopLossPrice(slStop);
            runtimeStateService.saveOrUpdate(state);

            notifyEvent(symbol, "BUY_FILLED", "【买入成交】规则4限价买单已成交，成交均价=" + buyTrade.avgPrice.toPlainString()
                    + "，数量=" + sellQty.toPlainString() + "，TP=" + tp.toPlainString() + "，SL=" + slStop.toPlainString(), orderResp);
            return new ExecutionResultVO(ExecutionDecisionEnum.RULE4_LIMIT_BUY.name(), "规则4买单已成交并挂保护单");
        }

        if ("NEW".equals(status)) {
            Map<String, Object> ticker = binanceClientService.getTicker24h(symbol);
            BigDecimal current = BigDecimalHelper.toBigDecimal(ticker.get("lastPrice"));
            if (state.getInvalidationPrice() != null && current.compareTo(state.getInvalidationPrice()) >= 0) {
                Map<String, Object> cancelResp = binanceClientService.cancelOrder(symbol, state.getBuyOrderId());
                botRecordService.saveOrderRecord(symbol, state.getRuleType(), OrderActionEnum.CANCEL_BUY.name(), "BUY", "LIMIT",
                        state.getBuyOrderId(), null, state.getBuyClientOrderId(), state.getLimitBuyPrice(), state.getEntryQty(),
                        null, "CANCELED", null, jsonHelper.toJson(cancelResp), "规则4未成交失效撤单");
                runtimeStateService.resetToIdle(symbol);
                notifyEvent(symbol, "BUY_INVALIDATED", "【挂单失效】规则4限价买单未成交，且价格已达到失效位，已撤单。当前价="
                        + current.toPlainString() + "，失效位=" + state.getInvalidationPrice().toPlainString(), cancelResp);
                return new ExecutionResultVO(ExecutionDecisionEnum.RULE4_INVALIDATED.name(), "规则4失效已撤单");
            }
            return new ExecutionResultVO(ExecutionDecisionEnum.NO_ACTION.name(), "规则4挂单继续有效");
        }

        if ("CANCELED".equals(status) || "EXPIRED".equals(status) || "REJECTED".equals(status)) {
            runtimeStateService.resetToIdle(symbol);
            notifyEvent(symbol, "BUY_INVALIDATED", "【挂单失效】规则4买单状态=" + status + "，已重置为空仓状态", orderResp);
            return new ExecutionResultVO(ExecutionDecisionEnum.RULE4_INVALIDATED.name(), "规则4买单已结束");
        }

        return new ExecutionResultVO(ExecutionDecisionEnum.NO_ACTION.name(), "规则4挂单继续有效");
    }

    private ExecutionResultVO handleHolding(String symbol, BotRuntimeStateEntity state, StrategyConfig config) {
        Map<String, Object> orderListResp = binanceClientService.queryOrderList(state.getSellOrderListId());
        String listStatus = String.valueOf(orderListResp.get("listOrderStatus"));
        if ("ALL_DONE".equals(listStatus)) {
            Map<String, Object> tpOrderResp = binanceClientService.queryOrder(symbol, state.getTpOrderId());
            Map<String, Object> slOrderResp = binanceClientService.queryOrder(symbol, state.getSlOrderId());
            if ("FILLED".equals(String.valueOf(tpOrderResp.get("status")))) {
                FilledTrade sellTrade = parseFilledTrade(tpOrderResp);
                botRecordService.closePositionBySellOrderListId(state.getSellOrderListId(), sellTrade.avgPrice, LocalDateTime.now(), "TAKE_PROFIT");
                runtimeStateService.resetToIdle(symbol);
                notifyEvent(symbol, "SELL_FILLED", "【卖出成交】已止盈卖出，成交均价=" + sellTrade.avgPrice.toPlainString(), tpOrderResp);
                return new ExecutionResultVO(ExecutionDecisionEnum.TAKE_PROFIT_FILLED.name(), "止盈成交");
            }
            if ("FILLED".equals(String.valueOf(slOrderResp.get("status")))) {
                FilledTrade sellTrade = parseFilledTrade(slOrderResp);
                botRecordService.closePositionBySellOrderListId(state.getSellOrderListId(), sellTrade.avgPrice, LocalDateTime.now(), "STOP_LOSS");
                runtimeStateService.resetToIdle(symbol);
                notifyEvent(symbol, "SELL_FILLED", "【卖出成交】已止损卖出，成交均价=" + sellTrade.avgPrice.toPlainString(), slOrderResp);
                return new ExecutionResultVO(ExecutionDecisionEnum.STOP_LOSS_FILLED.name(), "止损成交");
            }
        }

        if (state.getExitExpireTime() != null && LocalDateTime.now().isAfter(state.getExitExpireTime())) {
            Map<String, Object> cancelResp = binanceClientService.cancelOrderList(symbol, state.getSellOrderListId());
            botRecordService.saveOrderRecord(symbol, state.getRuleType(), OrderActionEnum.CANCEL_OCO.name(), "SELL", "OCO",
                    null, state.getSellOrderListId(), null, null, state.getEntryQty(), null, "CANCELED", null,
                    jsonHelper.toJson(cancelResp), "持仓超时撤销OCO");

            Map<String, Object> exchangeInfo = binanceClientService.getExchangeInfo(symbol);
            SymbolRule symbolRule = parseSymbolRule(exchangeInfo);
            BigDecimal freeBase = fetchFreeBalance(symbolRule.baseAsset);
            BigDecimal sellQty = BigDecimalHelper.roundDown(freeBase, symbolRule.lotStepSize);
            if (sellQty.compareTo(symbolRule.minQty) < 0) {
                runtimeStateService.resetToIdle(symbol);
                return new ExecutionResultVO(ExecutionDecisionEnum.FORCE_TIMEOUT_SELL.name(), "持仓超时但可卖数量不足minQty，请人工处理");
            }
            String clientOrderId = buildClientOrderId("fms");
            Map<String, Object> sellResp = binanceClientService.placeMarketSell(symbol, sellQty, clientOrderId);
            FilledTrade sellTrade = parseFilledTrade(sellResp);
            botRecordService.saveOrderRecord(symbol, state.getRuleType(), OrderActionEnum.FORCE_MARKET_SELL.name(), "SELL", "MARKET",
                    toLong(sellResp.get("orderId")), null, clientOrderId, sellTrade.avgPrice, sellTrade.executedQty, null,
                    String.valueOf(sellResp.get("status")), null, jsonHelper.toJson(sellResp), "持仓超时强制平仓");
            botRecordService.closePositionBySellOrderListId(state.getSellOrderListId(), sellTrade.avgPrice, LocalDateTime.now(), "TIMEOUT_CLOSE");
            runtimeStateService.resetToIdle(symbol);
            notifyEvent(symbol, "SELL_FILLED", "【持仓失效已卖出】成交后24小时未触发止盈/止损，已现价平仓，成交均价="
                    + sellTrade.avgPrice.toPlainString(), sellResp);
            return new ExecutionResultVO(ExecutionDecisionEnum.FORCE_TIMEOUT_SELL.name(), "持仓超时已平仓");
        }

        return new ExecutionResultVO(ExecutionDecisionEnum.NO_ACTION.name(), "持仓继续有效");
    }

    private void notifyEvent(String symbol, String eventType, String message, Object payload) {
        botRecordService.saveEvent(symbol, eventType, message, jsonHelper.toJson(payload));
        botMessageService.send(message);
    }

    private void errorEvent(String symbol, String eventType, String message, Object payload) {
        botRecordService.saveEvent(symbol, eventType, message, jsonHelper.toJson(payload));
        botMessageService.errorSend(message);
    }

    private FilledTrade parseFilledTrade(Map<String, Object> orderResp) {
        FilledTrade trade = new FilledTrade();
        trade.orderId = toLong(orderResp.get("orderId"));
        trade.executedQty = BigDecimalHelper.toBigDecimal(orderResp.get("executedQty"));
        BigDecimal quoteQty = BigDecimalHelper.toBigDecimal(orderResp.get("cummulativeQuoteQty"));
        if (trade.executedQty.compareTo(BigDecimal.ZERO) > 0) {
            trade.avgPrice = quoteQty.divide(trade.executedQty, 16, RoundingMode.HALF_UP);
        } else {
            trade.avgPrice = BigDecimal.ZERO;
        }
        return trade;
    }

    private BigDecimal fetchFreeBalance(String asset) {
        Map<String, Object> account = binanceClientService.getAccount();
        Object balancesObj = account.get("balances");
        if (!(balancesObj instanceof List)) {
            return BigDecimal.ZERO;
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> balances = (List<Map<String, Object>>) balancesObj;
        for (Map<String, Object> balance : balances) {
            if (asset.equals(String.valueOf(balance.get("asset")))) {
                return BigDecimalHelper.toBigDecimal(balance.get("free"));
            }
        }
        return BigDecimal.ZERO;
    }

    private SymbolRule parseSymbolRule(Map<String, Object> exchangeInfo) {
        Object symbolsObj = exchangeInfo.get("symbols");
        if (!(symbolsObj instanceof List) || ((List<?>) symbolsObj).isEmpty()) {
            throw new BizException("exchangeInfo 无法解析 symbols");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> symbolMap = (Map<String, Object>) ((List<?>) symbolsObj).get(0);
        SymbolRule rule = new SymbolRule();
        rule.baseAsset = String.valueOf(symbolMap.get("baseAsset"));
        rule.quoteAsset = String.valueOf(symbolMap.get("quoteAsset"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> filters = (List<Map<String, Object>>) symbolMap.get("filters");
        for (Map<String, Object> filter : filters) {
            String filterType = String.valueOf(filter.get("filterType"));
            if ("PRICE_FILTER".equals(filterType)) {
                rule.tickSize = BigDecimalHelper.toBigDecimal(filter.get("tickSize"));
            } else if ("LOT_SIZE".equals(filterType)) {
                rule.minQty = BigDecimalHelper.toBigDecimal(filter.get("minQty"));
                rule.lotStepSize = BigDecimalHelper.toBigDecimal(filter.get("stepSize"));
            } else if ("MIN_NOTIONAL".equals(filterType) || "NOTIONAL".equals(filterType)) {
                Object v = filter.get("minNotional");
                rule.minNotional = BigDecimalHelper.toBigDecimal(v);
            }
        }
        return rule;
    }

    private Long findChildOrderId(Map<String, Object> ocoResp, String clientOrderId) {
        Object ordersObj = ocoResp.get("orders");
        if (!(ordersObj instanceof List)) {
            return null;
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> orders = (List<Map<String, Object>>) ordersObj;
        for (Map<String, Object> order : orders) {
            if (clientOrderId.equals(String.valueOf(order.get("clientOrderId")))) {
                return toLong(order.get("orderId"));
            }
        }
        return null;
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        return Long.valueOf(String.valueOf(value));
    }

    private String buildClientOrderId(String prefix) {
        return prefix + "_" + System.currentTimeMillis();
    }

    private StrategyConfig loadConfig() {
        StrategyConfig config = new StrategyConfig();
        config.singleOrderU = botConfigService.getRequiredBigDecimal("BINANCE_SINGLE_ORDER_U");
        config.rule3UpperRatio = botConfigService.getRequiredBigDecimal("BOT_RULE3_UPPER_RATIO");
        config.middleRatio = botConfigService.getRequiredBigDecimal("BOT_RULE4_MIDDLE_RATIO");
        config.invalidRatio = botConfigService.getRequiredBigDecimal("BOT_RULE4_INVALID_RATIO");
        config.rule4PullbackRate = botConfigService.getRequiredBigDecimal("BOT_RULE4_PULLBACK_RATE");
        config.takeProfitRate = botConfigService.getRequiredBigDecimal("BOT_TAKE_PROFIT_RATE");
        config.stopLossRate = botConfigService.getRequiredBigDecimal("BOT_STOP_LOSS_RATE");
        config.stopLimitGapRate = botConfigService.getRequiredBigDecimal("BOT_STOP_LIMIT_GAP_RATE");
        config.minVolatilityPercent = botConfigService.getRequiredBigDecimal("BOT_MIN_VOLATILITY_PERCENT");
        return config;
    }

    private static class StrategyConfig {
        private BigDecimal singleOrderU;
        private BigDecimal rule3UpperRatio;
        private BigDecimal middleRatio;
        private BigDecimal invalidRatio;
        private BigDecimal rule4PullbackRate;
        private BigDecimal takeProfitRate;
        private BigDecimal stopLossRate;
        private BigDecimal stopLimitGapRate;
        private BigDecimal minVolatilityPercent;
    }

    private static class SymbolRule {
        private String baseAsset;
        private String quoteAsset;
        private BigDecimal tickSize = BigDecimal.ZERO;
        private BigDecimal minQty = BigDecimal.ZERO;
        private BigDecimal lotStepSize = BigDecimal.ZERO;
        private BigDecimal minNotional = BigDecimal.ZERO;
    }

    private static class FilledTrade {
        private Long orderId;
        private BigDecimal executedQty;
        private BigDecimal avgPrice;
    }
}
