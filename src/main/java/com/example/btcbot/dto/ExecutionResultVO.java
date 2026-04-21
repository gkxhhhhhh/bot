package com.example.btcbot.dto;

import com.example.btcbot.enums.ExecutionDecisionEnum;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExecutionResultVO {
    private String decision;
    private String message;

    public ExecutionResultVO() {
    }

    public ExecutionResultVO(String decision, String message) {
        this.decision = decision;
        this.message = message;
        log.info("{}----{}", ExecutionDecisionEnum.getByName(decision).getDesc(), message);
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
