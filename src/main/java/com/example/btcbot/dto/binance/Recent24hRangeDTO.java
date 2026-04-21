package com.example.btcbot.dto.binance;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Recent24hRangeDTO {

    /**
     * 最近24小时最高价 H
     */
    private BigDecimal highPrice;

    /**
     * 最近24小时最低价 L
     */
    private BigDecimal lowPrice;

    /**
     * 当前价格
     */
    private BigDecimal currentPrice;

    /**
     * 统计开始时间
     */
    private Long startTime;

    /**
     * 统计结束时间
     */
    private Long endTime;

    /**
     * 拉取到的k线数量
     */
    private Integer klineCount;
}
