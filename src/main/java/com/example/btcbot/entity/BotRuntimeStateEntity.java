package com.example.btcbot.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_bot_runtime_state")
public class BotRuntimeStateEntity extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 交易对，例如 BTCUSDT
     */
    private String symbol;

    /**
     * 当前阶段
     */
    private String stage;

    /**
     * 当前命中的规则类型
     */
    private String ruleType;

    /**
     * 基础资产，例如 BTC
     */
    private String baseAsset;

    /**
     * 计价资产，例如 USDT
     */
    private String quoteAsset;

    /**
     * 买单订单ID
     */
    private Long buyOrderId;

    /**
     * 买单客户端订单号
     */
    private String buyClientOrderId;

    /**
     * 限价买入价格
     */
    private BigDecimal limitBuyPrice;

    /**
     * 卖出OCO订单列表ID
     */
    private Long sellOrderListId;

    /**
     * 止盈单订单ID
     */
    private Long tpOrderId;

    /**
     * 止损单订单ID
     */
    private Long slOrderId;

    /**
     * 入场价格
     */
    private BigDecimal entryPrice;

    /**
     * 入场数量
     */
    private BigDecimal entryQty;

    /**
     * 入场时间
     */
    private LocalDateTime entryTime;

    /**
     * 退出超时时间
     */
    private LocalDateTime exitExpireTime;

    /**
     * 止盈价格
     */
    private BigDecimal takeProfitPrice;

    /**
     * 止损触发价格
     */
    private BigDecimal stopLossPrice;

    /**
     * 触发时记录的24小时最高价
     */
    private BigDecimal h0;

    /**
     * 触发时记录的24小时最低价
     */
    private BigDecimal l0;

    /**
     * 触发时记录的24小时区间宽度
     */
    private BigDecimal r0;

    /**
     * 触发时记录的当前价格
     */
    private BigDecimal p0;

    /**
     * 规则4失效价格
     */
    private BigDecimal invalidationPrice;

    /**
     * 规则触发时间
     */
    private LocalDateTime triggerTime;

}
