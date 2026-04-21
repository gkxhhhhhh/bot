package com.example.btcbot.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("t_bot_runtime_state")
public class BotRuntimeStateEntity extends BaseEntity {
    @TableId
    private Long id;
    private String symbol;
    private String stage;
    private String ruleType;
    private String baseAsset;
    private String quoteAsset;
    private Long buyOrderId;
    private String buyClientOrderId;
    private BigDecimal limitBuyPrice;
    private Long sellOrderListId;
    private Long tpOrderId;
    private Long slOrderId;
    private BigDecimal entryPrice;
    private BigDecimal entryQty;
    private LocalDateTime entryTime;
    private LocalDateTime exitExpireTime;
    private BigDecimal takeProfitPrice;
    private BigDecimal stopLossPrice;
    private BigDecimal h0;
    private BigDecimal l0;
    private BigDecimal r0;
    private BigDecimal p0;
    private BigDecimal invalidationPrice;
    private LocalDateTime triggerTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }
    public String getRuleType() { return ruleType; }
    public void setRuleType(String ruleType) { this.ruleType = ruleType; }
    public String getBaseAsset() { return baseAsset; }
    public void setBaseAsset(String baseAsset) { this.baseAsset = baseAsset; }
    public String getQuoteAsset() { return quoteAsset; }
    public void setQuoteAsset(String quoteAsset) { this.quoteAsset = quoteAsset; }
    public Long getBuyOrderId() { return buyOrderId; }
    public void setBuyOrderId(Long buyOrderId) { this.buyOrderId = buyOrderId; }
    public String getBuyClientOrderId() { return buyClientOrderId; }
    public void setBuyClientOrderId(String buyClientOrderId) { this.buyClientOrderId = buyClientOrderId; }
    public BigDecimal getLimitBuyPrice() { return limitBuyPrice; }
    public void setLimitBuyPrice(BigDecimal limitBuyPrice) { this.limitBuyPrice = limitBuyPrice; }
    public Long getSellOrderListId() { return sellOrderListId; }
    public void setSellOrderListId(Long sellOrderListId) { this.sellOrderListId = sellOrderListId; }
    public Long getTpOrderId() { return tpOrderId; }
    public void setTpOrderId(Long tpOrderId) { this.tpOrderId = tpOrderId; }
    public Long getSlOrderId() { return slOrderId; }
    public void setSlOrderId(Long slOrderId) { this.slOrderId = slOrderId; }
    public BigDecimal getEntryPrice() { return entryPrice; }
    public void setEntryPrice(BigDecimal entryPrice) { this.entryPrice = entryPrice; }
    public BigDecimal getEntryQty() { return entryQty; }
    public void setEntryQty(BigDecimal entryQty) { this.entryQty = entryQty; }
    public LocalDateTime getEntryTime() { return entryTime; }
    public void setEntryTime(LocalDateTime entryTime) { this.entryTime = entryTime; }
    public LocalDateTime getExitExpireTime() { return exitExpireTime; }
    public void setExitExpireTime(LocalDateTime exitExpireTime) { this.exitExpireTime = exitExpireTime; }
    public BigDecimal getTakeProfitPrice() { return takeProfitPrice; }
    public void setTakeProfitPrice(BigDecimal takeProfitPrice) { this.takeProfitPrice = takeProfitPrice; }
    public BigDecimal getStopLossPrice() { return stopLossPrice; }
    public void setStopLossPrice(BigDecimal stopLossPrice) { this.stopLossPrice = stopLossPrice; }
    public BigDecimal getH0() { return h0; }
    public void setH0(BigDecimal h0) { this.h0 = h0; }
    public BigDecimal getL0() { return l0; }
    public void setL0(BigDecimal l0) { this.l0 = l0; }
    public BigDecimal getR0() { return r0; }
    public void setR0(BigDecimal r0) { this.r0 = r0; }
    public BigDecimal getP0() { return p0; }
    public void setP0(BigDecimal p0) { this.p0 = p0; }
    public BigDecimal getInvalidationPrice() { return invalidationPrice; }
    public void setInvalidationPrice(BigDecimal invalidationPrice) { this.invalidationPrice = invalidationPrice; }
    public LocalDateTime getTriggerTime() { return triggerTime; }
    public void setTriggerTime(LocalDateTime triggerTime) { this.triggerTime = triggerTime; }
}
