package com.example.btcbot.enums;

/**
 * 机器人阶段枚举
 */
public enum BotStageEnum {

    IDLE("空闲状态"),
    RULE4_PENDING_BUY("空仓状态"),
    HOLDING("当前没有活动中的策略单");

    /**
     * 阶段描述
     */
    private final String desc;

    BotStageEnum(String desc) {
        this.desc = desc;
    }

    /**
     * 获取阶段描述
     *
     * @return 阶段描述
     */
    public String getDesc() {
        return desc;
    }

    /**
     * 根据枚举名称获取描述
     *
     * @param name 枚举名称
     * @return 描述，未匹配到时返回空字符串
     */
    public static String getDescByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "";
        }

        for (BotStageEnum item : BotStageEnum.values()) {
            if (item.name().equals(name)) {
                return item.getDesc();
            }
        }
        return "";
    }
}