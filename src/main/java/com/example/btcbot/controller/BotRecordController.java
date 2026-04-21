package com.example.btcbot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.btcbot.common.ApiResponse;
import com.example.btcbot.entity.BotEventEntity;
import com.example.btcbot.entity.BotExecutionLogEntity;
import com.example.btcbot.entity.BotOrderRecordEntity;
import com.example.btcbot.entity.BotPositionEntity;
import com.example.btcbot.service.BotRecordService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/record")
public class BotRecordController {

    private final BotRecordService botRecordService;

    public BotRecordController(BotRecordService botRecordService) {
        this.botRecordService = botRecordService;
    }

    @GetMapping("/executions")
    public ApiResponse<Page<BotExecutionLogEntity>> executions(@RequestParam(defaultValue = "1") long current,
                                                               @RequestParam(defaultValue = "20") long size) {
        return ApiResponse.ok(botRecordService.pageExecutionLogs(current, size));
    }

    @GetMapping("/orders")
    public ApiResponse<Page<BotOrderRecordEntity>> orders(@RequestParam(defaultValue = "1") long current,
                                                          @RequestParam(defaultValue = "20") long size) {
        return ApiResponse.ok(botRecordService.pageOrderRecords(current, size));
    }

    @GetMapping("/events")
    public ApiResponse<Page<BotEventEntity>> events(@RequestParam(defaultValue = "1") long current,
                                                    @RequestParam(defaultValue = "20") long size) {
        return ApiResponse.ok(botRecordService.pageEvents(current, size));
    }

    @GetMapping("/positions")
    public ApiResponse<Page<BotPositionEntity>> positions(@RequestParam(defaultValue = "1") long current,
                                                          @RequestParam(defaultValue = "20") long size) {
        return ApiResponse.ok(botRecordService.pagePositions(current, size));
    }
}
