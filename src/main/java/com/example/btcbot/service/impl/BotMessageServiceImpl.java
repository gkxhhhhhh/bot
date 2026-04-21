package com.example.btcbot.service.impl;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.dingtalk.api.response.OapiRobotSendResponse;
import com.example.btcbot.service.BotConfigService;
import com.example.btcbot.service.BotMessageService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class BotMessageServiceImpl implements BotMessageService {

    @Resource
    private BotConfigService botConfigService;

    @Override
    public void send(String message) {
        Long timestamp = System.currentTimeMillis();
        String binanceSymbol = botConfigService.getRequiredString("DINGDING_TRADE_ROBOT_KEY");
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/robot/send?timestamp="+timestamp);
        OapiRobotSendRequest req = new OapiRobotSendRequest();
        //定义文本内容
        OapiRobotSendRequest.Text text = new OapiRobotSendRequest.Text();
        text.setContent("成交消息");
        //设置消息类型
        req.setMsgtype("成交消息：" + message);
        req.setText(text);
        try {
            OapiRobotSendResponse rsp = client.execute(req, binanceSymbol);
            System.out.println(rsp.getBody());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void errorSend(String message) {
        Long timestamp = System.currentTimeMillis();
        String binanceSymbol = botConfigService.getRequiredString("DINGDING_ERROR_ROBOT_KEY");
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/robot/send?timestamp="+timestamp);
        OapiRobotSendRequest req = new OapiRobotSendRequest();
        //定义文本内容
        OapiRobotSendRequest.Text text = new OapiRobotSendRequest.Text();
        text.setContent("错误消息");
        //设置消息类型
        req.setMsgtype("错误消息：" + message);
        req.setText(text);
        try {
            OapiRobotSendResponse rsp = client.execute(req, binanceSymbol);
            System.out.println(rsp.getBody());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
