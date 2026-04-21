package com.example.btcbot.task;

import com.example.btcbot.service.BotExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * BTC 机器人定时执行任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BotExecutionScheduleTask {

    private final BotExecutionService botExecutionService;


    /**
     * 每2分钟执行一次机器人策略
     */
    @Scheduled(cron = "0 */2 * * * ?")
    public void runBotTask() {
        try {
            log.info("定时任务开始执行 BTC 现货策略");
            botExecutionService.runOnce("BTCUSDT");
            log.info("定时任务执行 BTC 现货策略完成");
        } catch (Exception e) {
            log.error("定时任务执行 BTC 现货策略失败", e);
        }
    }
}