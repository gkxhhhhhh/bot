package com.example.btcbot.service;

import com.example.btcbot.entity.BotRuntimeStateEntity;

public interface RuntimeStateService {
    BotRuntimeStateEntity getBySymbol(String symbol);

    BotRuntimeStateEntity initIfAbsent(String symbol);

    void saveOrUpdate(BotRuntimeStateEntity state);

    void resetToIdle(String symbol);
}
