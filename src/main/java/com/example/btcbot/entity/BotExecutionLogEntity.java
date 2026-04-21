package com.example.btcbot.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;

@TableName("t_bot_execution_log")
public class BotExecutionLogEntity extends BaseEntity {
    @TableId
    private Long id;
    private String symbol;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private BigDecimal currentPrice;
    private BigDecimal rangeValue;
    private BigDecimal rule3UpperPrice;
    private BigDecimal middlePrice;
    private BigDecimal invalidationPrice;
    private BigDecimal volatilityPercent;
    private String decision;
    private String metReasons;
    private String unmetReasons;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public BigDecimal getHighPrice() { return highPrice; }
    public void setHighPrice(BigDecimal highPrice) { this.highPrice = highPrice; }
    public BigDecimal getLowPrice() { return lowPrice; }
    public void setLowPrice(BigDecimal lowPrice) { this.lowPrice = lowPrice; }
    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }
    public BigDecimal getRangeValue() { return rangeValue; }
    public void setRangeValue(BigDecimal rangeValue) { this.rangeValue = rangeValue; }
    public BigDecimal getRule3UpperPrice() { return rule3UpperPrice; }
    public void setRule3UpperPrice(BigDecimal rule3UpperPrice) { this.rule3UpperPrice = rule3UpperPrice; }
    public BigDecimal getMiddlePrice() { return middlePrice; }
    public void setMiddlePrice(BigDecimal middlePrice) { this.middlePrice = middlePrice; }
    public BigDecimal getInvalidationPrice() { return invalidationPrice; }
    public void setInvalidationPrice(BigDecimal invalidationPrice) { this.invalidationPrice = invalidationPrice; }
    public BigDecimal getVolatilityPercent() { return volatilityPercent; }
    public void setVolatilityPercent(BigDecimal volatilityPercent) { this.volatilityPercent = volatilityPercent; }
    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
    public String getMetReasons() { return metReasons; }
    public void setMetReasons(String metReasons) { this.metReasons = metReasons; }
    public String getUnmetReasons() { return unmetReasons; }
    public void setUnmetReasons(String unmetReasons) { this.unmetReasons = unmetReasons; }
}
