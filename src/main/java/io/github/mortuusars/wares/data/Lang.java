package io.github.mortuusars.wares.data;

import io.github.mortuusars.wares.Wares;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public enum Lang {
    BLOCK_DELIVERY_TABLE(Wares.Blocks.DELIVERY_TABLE.get()),
    BLOCK_DELIVERY_PACKAGE(Wares.Blocks.DELIVERY_PACKAGE.get()),

    ITEM_SEALED_DELIVERY_AGREEMENT(Wares.Items.SEALED_DELIVERY_AGREEMENT.get()),
    ITEM_DAMAGED_SEALED_DELIVERY_AGREEMENT(Wares.Items.SEALED_DELIVERY_AGREEMENT.get().getDescriptionId() + "_damaged"),
    ITEM_DELIVERY_AGREEMENT(Wares.Items.DELIVERY_AGREEMENT.get()),
    ITEM_COMPLETED_DELIVERY_AGREEMENT(Wares.Items.COMPLETED_DELIVERY_AGREEMENT.get()),
    ITEM_EXPIRED_DELIVERY_AGREEMENT(Wares.Items.EXPIRED_DELIVERY_AGREEMENT.get()),

    SEALED_AGREEMENT_DAMAGED_ERROR_MESSAGE("item", "sealed_delivery_agreement.damaged_error_message"),
    SEALED_AGREEMENT_UNOPENABLE_ERROR_MESSAGE("item", "sealed_delivery_agreement.unopenable_error_message"),

    ITEM_AGREEMENT_VIEW_TOOLTIP("item", "agreement.view.tooltip"),
    ITEM_SEALED_AGREEMENT_INSPECT_TOOLTIP("item", "sealed_agreement.view.tooltip"),

    VILLAGER_PACKAGER("entity.minecraft.villager", "packager"),

    GUI_DELIVERY_TABLE_NO_AGREEMENT_TOOLTIP("gui", "delivery_table.no_agreement.tooltip"),
    GUI_DELIVERY_TABLE_NO_PACKAGES_TOOLTIP("gui", "delivery_table.no_packages.tooltip"),
    GUI_DELIVERY_TABLE_MANUAL_DELIVERY("gui", "delivery_table.manual_delivery"),
    GUI_DELIVERY_TABLE_MANUAL_DELIVERY_TOOLTIP("gui", "delivery_table.manual_delivery.tooltip"),
    GUI_DELIVERY_TABLE_MANUAL_DELIVERY_TOOLTIP_EXTRA_INFO("gui", "delivery_table.manual_delivery.tooltip_extra_info"),

    GUI_AGREEMENT_TITLE("gui", "agreement.title"),
    GUI_AGREEMENT_MESSAGE("gui", "agreement.message"),
    GUI_AGREEMENT_EXPIRE_TIME("gui", "agreement.expire_time"),
    GUI_AGREEMENT_EXPIRE_TIME_TOOLTIP("gui", "agreement.expire_time.tooltip"),
    GUI_AGREEMENT_DELIVERIES("gui", "agreement.deliveries"),
    GUI_AGREEMENT_DELIVERIES_TOOLTIP("gui", "agreement.deliveries.tooltip"),
    GUI_AGREEMENT_COMPLETED("gui", "agreement.completed.tooltip"),
    GUI_AGREEMENT_EXPIRED("gui", "agreement.expired.tooltip"),
    GUI_TIME_DAYS("gui", "days.short"),
    GUI_TIME_HOURS("gui", "hours.short"),
    GUI_TIME_MINUTES("gui", "minutes.short"),

    GUI_SEALED_AGREEMENT_SHOW_REMAINING_TEXT_MESSAGE("gui", "sealed_agreement.show_remaining_text_message"),

    GUI_SEAL_TEXTURE_NOT_FOUND_MESSAGE("gui", "seal_texture_not_found"),

    ADVANCEMENT_LAST_MINUTES_TITLE("advancement", "last_minutes.title"),
    ADVANCEMENT_LAST_MINUTES_DESCRIPTION("advancement", "last_minutes.description"),

    STATS_INTERACT_WITH_DELIVERY_TABLE("stat", Wares.Stats.INTERACT_WITH_DELIVERY_TABLE.getPath()),
    STATS_SEALED_AGREEMENTS_OPENED("stat", Wares.Stats.SEALED_LETTERS_OPENED.getPath()),

    COMMAND_AGREEMENT_COMPLETE_WRONG_ITEM("commands", "agreement.complete.wrong_item"),
    COMMAND_AGREEMENT_COMPLETE_IS_EMPTY("commands", "agreement.complete.is_empty"),
    COMMAND_AGREEMENT_COMPLETE_ALREADY_COMPLETED("commands", "agreement.complete.already_completed"),
    COMMAND_AGREEMENT_COMPLETE_IS_EXPIRED("commands", "agreement.complete.is_expired"),

    SUBTITLE_PAPER_CRACKLE("subtitle", "paper.crackle"),
    SUBTITLE_PAPER_TEAR("subtitle", "paper.tear"),
    SUBTITLE_WRITING("subtitle", "writing"),
    SUBTITLE_PACKAGER_WORKS("subtitles.entity.villager.work_packager")
    ;

    public final String key;

    Lang(String category, String key) {
        this.key = category + "." + Wares.ID + "." + key;
    }

    Lang(Item item) {
        this(item.getDescriptionId());
    }

    Lang(Block block) {
        this(block.getDescriptionId());
    }

    Lang(String key) {
        this.key = key;
    }

    public MutableComponent translate(Object... args) {
        return new TranslatableComponent(key, args);
    }
}
