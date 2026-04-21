package com.example.btcbot.service;

public interface BotMessageService {
    void send(String message);
    void errorSend(String message);
}
