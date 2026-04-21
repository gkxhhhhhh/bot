package com.example.btcbot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.btcbot.common.ApiResponse;
import com.example.btcbot.dto.BotConfigSaveRequest;
import com.example.btcbot.entity.BotConfigEntity;
import com.example.btcbot.service.BotConfigService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/config")
public class BotConfigController {

    private final BotConfigService botConfigService;

    public BotConfigController(BotConfigService botConfigService) {
        this.botConfigService = botConfigService;
    }

    @GetMapping("/page")
    public ApiResponse<Page<BotConfigEntity>> page(@RequestParam(defaultValue = "1") long current,
                                                   @RequestParam(defaultValue = "20") long size) {
        return ApiResponse.ok(botConfigService.page(current, size));
    }

    @PostMapping("/save")
    public ApiResponse<BotConfigEntity> save(@Validated @RequestBody BotConfigSaveRequest request) {
        return ApiResponse.ok("保存成功", botConfigService.saveConfig(request));
    }
}
