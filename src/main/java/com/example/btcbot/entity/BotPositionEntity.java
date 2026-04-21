package com.example.btcbot.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("t_bot_position")
public class BotPositionEntity extends BaseEntity {
    @TableId
    private Long id;
    private String symbol;
    private String ruleType;
    private Long entryOrderRecordId;
    private Long sellOrderListId;
    private Long tpOrderId;
    private Long slOrderId;
    private BigDecimal entryPrice;
    private BigDecimal entryQty;
    private BigDecimal takeProfitPrice;
    private BigDecimal stopLossPrice;
    private LocalDateTime entryTime;
    private LocalDateTime expireTime;
    private String status;
    private BigDecimal closePrice;
    private LocalDateTime closeTime;
    private String closeReason;
    private BigDecimal profitAmount;
    private BigDecimal profitRate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getRuleType() { return ruleType; }
    public void setRuleType(String ruleType) { this.ruleType = ruleType; }
    public Long getEntryOrderRecordId() { return entryOrderRecordId; }
    public void setEntryOrderRecordId(Long entryOrderRecordId) { this.entryOrderRecordId = entryOrderRecordId; }
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
    public BigDecimal getTakeProfitPrice() { return takeProfitPrice; }
    public void setTakeProfitPrice(BigDecimal takeProfitPrice) { this.takeProfitPrice = takeProfitPrice; }
    public BigDecimal getStopLossPrice() { return stopLossPrice; }
    public void setStopLossPrice(BigDecimal stopLossPrice) { this.stopLossPrice = stopLossPrice; }
    public LocalDateTime getEntryTime() { return entryTime; }
    public void setEntryTime(LocalDateTime entryTime) { this.entryTime = entryTime; }
    public LocalDateTime getExpireTime() { return expireTime; }
    public void setExpireTime(LocalDateTime expireTime) { this.expireTime = expireTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getClosePrice() { return closePrice; }
    public void setClosePrice(BigDecimal closePrice) { this.closePrice = closePrice; }
    public LocalDateTime getCloseTime() { return closeTime; }
    public void setCloseTime(LocalDateTime closeTime) { this.closeTime = closeTime; }
    public String getCloseReason() { return closeReason; }
    public void setCloseReason(String closeReason) { this.closeReason = closeReason; }
    public BigDecimal getProfitAmount() { return profitAmount; }
    public void setProfitAmount(BigDecimal profitAmount) { this.profitAmount = profitAmount; }
    public BigDecimal getProfitRate() { return profitRate; }
    public void setProfitRate(BigDecimal profitRate) { this.profitRate = profitRate; }
}
