package com.example.btcbot.controller;

import com.example.btcbot.common.ApiResponse;
import com.example.btcbot.dto.ExecutionResultVO;
import com.example.btcbot.dto.RunCycleRequest;
import com.example.btcbot.service.BotExecutionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bot")
public class BotExecutionController {

    private final BotExecutionService botExecutionService;

    public BotExecutionController(BotExecutionService botExecutionService) {
        this.botExecutionService = botExecutionService;
    }

    @PostMapping("/run-once")
    public ApiResponse<ExecutionResultVO> runOnce(@RequestBody(required = false) RunCycleRequest request) {
        String symbol = request == null ? null : request.getSymbol();
        return ApiResponse.ok(botExecutionService.runOnce(symbol));
    }
}
