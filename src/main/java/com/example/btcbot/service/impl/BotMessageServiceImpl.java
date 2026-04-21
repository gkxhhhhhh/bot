package com.example.btcbot.service.impl;

import com.example.btcbot.service.BotMessageService;
import org.springframework.stereotype.Service;

@Service
public class BotMessageServiceImpl implements BotMessageService {
    @Override
    public void send(String message) {
        System.out.println(message);
    }
}
