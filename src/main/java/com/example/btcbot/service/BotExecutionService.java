package com.example.btcbot.service;

import com.example.btcbot.dto.ExecutionResultVO;

public interface BotExecutionService {
    ExecutionResultVO runOnce(String symbol);
}
