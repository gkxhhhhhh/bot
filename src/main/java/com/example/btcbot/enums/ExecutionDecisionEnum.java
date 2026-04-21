package com.example.btcbot.enums;

public enum ExecutionDecisionEnum {
    RULE3_MARKET_BUY,
    RULE4_LIMIT_BUY,
    NO_ACTION,
    RULE4_INVALIDATED,
    TAKE_PROFIT_FILLED,
    STOP_LOSS_FILLED,
    FORCE_TIMEOUT_SELL
}
