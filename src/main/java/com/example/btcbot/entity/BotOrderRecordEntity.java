package com.example.btcbot.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;

@TableName("t_bot_order_record")
public class BotOrderRecordEntity extends BaseEntity {
    @TableId
    private Long id;
    private String symbol;
    private String ruleType;
    private String actionType;
    private String side;
    private String orderType;
    private Long exchangeOrderId;
    private Long exchangeOrderListId;
    private String clientOrderId;
    private BigDecimal price;
    private BigDecimal quantity;
    private BigDecimal quoteAmount;
    private String status;
    private String requestJson;
    private String responseJson;
    private String remark;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getRuleType() { return ruleType; }
    public void setRuleType(String ruleType) { this.ruleType = ruleType; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public String getSide() { return side; }
    public void setSide(String side) { this.side = side; }
    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }
    public Long getExchangeOrderId() { return exchangeOrderId; }
    public void setExchangeOrderId(Long exchangeOrderId) { this.exchangeOrderId = exchangeOrderId; }
    public Long getExchangeOrderListId() { return exchangeOrderListId; }
    public void setExchangeOrderListId(Long exchangeOrderListId) { this.exchangeOrderListId = exchangeOrderListId; }
    public String getClientOrderId() { return clientOrderId; }
    public void setClientOrderId(String clientOrderId) { this.clientOrderId = clientOrderId; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getQuoteAmount() { return quoteAmount; }
    public void setQuoteAmount(BigDecimal quoteAmount) { this.quoteAmount = quoteAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRequestJson() { return requestJson; }
    public void setRequestJson(String requestJson) { this.requestJson = requestJson; }
    public String getResponseJson() { return responseJson; }
    public void setResponseJson(String responseJson) { this.responseJson = responseJson; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
