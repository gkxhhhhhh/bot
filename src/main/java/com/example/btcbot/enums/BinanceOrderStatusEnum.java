package com.example.btcbot.enums;

/**
 * Binance 订单状态枚举
 */
public enum BinanceOrderStatusEnum {

    NEW("订单已经被撮合引擎接受，但还没有成交"),
    FILLED("订单已经全部成交完成"),
    PARTIALLY_FILLED("订单已部分成交，仍有剩余数量未成交"),
    CANCELED("订单已被主动取消"),
    EXPIRED("订单已失效或过期"),
    REJECTED("订单被拒绝，未成功进入正常撮合流程");

    /**
     * 状态描述
     */
    private final String desc;

    BinanceOrderStatusEnum(String desc) {
        this.desc = desc;
    }

    /**
     * 获取状态描述
     *
     * @return 状态描述
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

        for (BinanceOrderStatusEnum item : BinanceOrderStatusEnum.values()) {
            if (item.name().equals(name)) {
                return item.getDesc();
            }
        }
        return "";
    }

    /**
     * 根据枚举名称获取枚举对象
     *
     * @param name 枚举名称
     * @return 枚举对象，未匹配到时返回 null
     */
    public static BinanceOrderStatusEnum getByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        for (BinanceOrderStatusEnum item : BinanceOrderStatusEnum.values()) {
            if (item.name().equals(name)) {
                return item;
            }
        }
        return null;
    }
}