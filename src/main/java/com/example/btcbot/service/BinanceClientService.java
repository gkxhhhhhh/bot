package com.example.btcbot.service;

import com.example.btcbot.dto.binance.Recent24hRangeDTO;

import java.math.BigDecimal;
import java.util.Map;

public interface BinanceClientService {
    Map<String, Object> getTicker24h(String symbol);

    Map<String, Object> getExchangeInfo(String symbol);

    Map<String, Object> getAccount();

    Map<String, Object> placeMarketBuyByQuote(String symbol, BigDecimal quoteOrderQty, String clientOrderId);

    Map<String, Object> placeLimitBuy(String symbol, BigDecimal quantity, BigDecimal price, String clientOrderId);

    Map<String, Object> placeMarketSell(String symbol, BigDecimal quantity, String clientOrderId);

    Map<String, Object> placeOcoSell(String symbol, BigDecimal quantity,
                                     BigDecimal takeProfitPrice, BigDecimal stopPrice,
                                     BigDecimal stopLimitPrice,
                                     String listClientOrderId,
                                     String tpClientOrderId,
                                     String slClientOrderId);

    Map<String, Object> queryOrder(String symbol, Long orderId);

    Map<String, Object> queryOrderList(Long orderListId);

    Map<String, Object> cancelOrder(String symbol, Long orderId);

    Map<String, Object> cancelOrderList(String symbol, Long orderListId);
    /**
     * 基于 klines 自行计算最近24小时的 H / L / current
     */
    Recent24hRangeDTO getRecent24hRange(String symbol);

    Long getServerTime();
}
