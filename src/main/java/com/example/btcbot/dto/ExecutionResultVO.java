package com.example.btcbot.dto;

public class ExecutionResultVO {
    private String decision;
    private String message;

    public ExecutionResultVO() {
    }

    public ExecutionResultVO(String decision, String message) {
        this.decision = decision;
        this.message = message;
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
