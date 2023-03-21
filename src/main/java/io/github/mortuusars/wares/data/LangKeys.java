package io.github.mortuusars.wares.data;

import io.github.mortuusars.wares.Wares;

public class LangKeys {

    public static final String SEALED_AGREEMENT_DAMAGED_MESSAGE = key("item", "sealed_delivery_agreement.damaged.message");
    public static final String SEALED_AGREEMENT_UNOPENABLE_MESSAGE = key("item", "sealed_delivery_agreement.unopenable.message");

    public static final String GUI_DELIVERY_AGREEMENT_TITLE = key("gui", "delivery_agreement.title");
    public static final String GUI_DELIVERY_AGREEMENT_EXPIRES = key("gui", "delivery_agreement.expires");
    public static final String GUI_DELIVERY_AGREEMENT_EXPIRES_TOOLTIP = key("gui", "delivery_agreement.expires.tooltip");
    public static final String GUI_DELIVERY_AGREEMENT_ORDERS = key("gui", "delivery_agreement.orders");
    public static final String GUI_DELIVERY_AGREEMENT_ORDERS_TOOLTIP = key("gui", "delivery_agreement.orders.tooltip");
    public static final String GUI_DELIVERY_AGREEMENT_EXPERIENCE = key("gui", "delivery_agreement.experience");
    public static final String GUI_DELIVERY_AGREEMENT_EXPERIENCE_TOOLTIP = key("gui", "delivery_agreement.experience.tooltip");

    public static final String GUI_EXPIRES_IN = key("gui", "tooltip.expires_in");
    public static final String GUI_EXPIRED = key("gui", "tooltip.expired");
    public static final String GUI_TIME_MINUTES = key("gui", "tooltip.minutes");
    public static final String GUI_TIME_HOURS = key("gui", "tooltip.hours");
    public static final String GUI_TIME_DAYS = key("gui", "tooltip.days");

    public static String key(String category, String key) {
        return category + "." + Wares.ID + "." + key;
    }
}
