package io.github.mortuusars.wares.data;

import io.github.mortuusars.wares.Wares;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public enum Lang {
    BLOCK_DELIVERY_TABLE(Wares.Blocks.DELIVERY_TABLE.get(), "Delivery Table", "Торговий Стіл"),

    ITEM_SEALED_DELIVERY_AGREEMENT(Wares.Items.SEALED_DELIVERY_AGREEMENT.get(), "Sealed Delivery Agreement", "Запечатаний Торговий Договір"),
    ITEM_DAMAGED_SEALED_DELIVERY_AGREEMENT(Wares.Items.SEALED_DELIVERY_AGREEMENT.get().getDescriptionId() + "_damaged", "Damaged Sealed Delivery Agreement", "Пошкоджений Запечатаний Торговий Договір"),
    ITEM_DELIVERY_AGREEMENT(Wares.Items.DELIVERY_AGREEMENT.get(), "Delivery Agreement", "Торговий Договір"),

    SEALED_AGREEMENT_DAMAGED_ERROR_MESSAGE("item", "sealed_delivery_agreement.damaged_error_message",
            "The letter is badly damaged, torn paper and faded ink make the contents unreadable.",
            "Лист сильно пошкоджений, надриви та вицвіле чорнило роблять його нечитабельним."),
    SEALED_AGREEMENT_UNOPENABLE_ERROR_MESSAGE("item", "sealed_delivery_agreement.unopenable_error_message",
            "The letter seems intact but sealed in a way that will damage and make the contents unreadable when opened.",
            "Лист здається неушкодженим, але запечатаний таким чином, що відкрити його без пошкодження вмісту, що зробить його нечитабельним, неможливо."),

    AGREEMENT_VIEW_TOOLTIP("item", "agreement.view.tooltip", "Right-click to view", "ПКМ щоб переглянути"),

    GUI_AGREEMENT_TITLE("gui", "agreement.title",
            "Delivery Agreement",
            "Торговий Договір"),
    GUI_AGREEMENT_MESSAGE("gui", "agreement.message",
            "I have some wares that I believe might interest you. Would you be open to trading your goods for mine?",
            "У мене є товари, які, можливо, будуть Вам цікаві. Згодні на торгівлю?"),
    GUI_AGREEMENT_EXPIRE_TIME("gui", "agreement.expire_time", "Expire time", "Термін дії"),
    GUI_AGREEMENT_EXPIRE_TIME_TOOLTIP("gui", "agreement.expire_time.tooltip", "Expires in: %s", "Термін дії закінчується через: %s"),
    GUI_AGREEMENT_DELIVERIES("gui", "agreement.deliveries", "Ordered deliveries", "Замовлені поставки"),
    GUI_AGREEMENT_DELIVERIES_TOOLTIP("gui", "agreement.deliveries.tooltip", "%s / %s", "%s / %s"),
    GUI_AGREEMENT_COMPLETED("gui", "agreement.completed.tooltip", "Completed", "Виконано"),
    GUI_AGREEMENT_EXPIRED("gui", "agreement.expired.tooltip", "Expired", "Минув термін дії"),
    GUI_TIME_DAYS("gui", "days.short","%sd", "%sдн"),
    GUI_TIME_HOURS("gui", "hours.short", "%sh", "%sгод"),
    GUI_TIME_MINUTES("gui", "minutes.short", "%sm", "%sхв");

    public final String en_us;
    public final String uk_ua;
    public final String key;


    Lang(String category, String key, String en_us, String uk_ua) {
        this.key = category + "." + Wares.ID + "." + key;
        this.en_us = en_us;
        this.uk_ua = uk_ua;
    }

    Lang(Item item, String en_us, String uk_ua) {
        this(item.getDescriptionId(), en_us, uk_ua);
    }

    Lang(Block block, String en_us, String uk_ua) {
        this(block.getDescriptionId(), en_us, uk_ua);
    }

    Lang(String key, String en_us, String uk_ua) {
        this.key = key;
        this.en_us = en_us;
        this.uk_ua = uk_ua;
    }

    public MutableComponent translate(Object... args) {
        return new TranslatableComponent(key, args);
    }
}
