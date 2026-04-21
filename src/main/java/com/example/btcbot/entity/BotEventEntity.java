package com.example.btcbot.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("t_bot_event")
public class BotEventEntity extends BaseEntity {
    @TableId
    private Long id;
    private String symbol;
    private String eventType;
    private String eventMessage;
    private String payloadJson;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getEventMessage() { return eventMessage; }
    public void setEventMessage(String eventMessage) { this.eventMessage = eventMessage; }
    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }
}
