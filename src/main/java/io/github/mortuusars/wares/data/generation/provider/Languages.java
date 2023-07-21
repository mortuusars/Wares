package io.github.mortuusars.wares.data.generation.provider;

import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.data.Lang;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Languages extends LanguageProvider {
    public enum MissingEntriesPolicy {
        EXCEPTION,
        WARNING
    }

    private final String locale;
    private final MissingEntriesPolicy policy;

    protected final List<String> translatedKeys = new ArrayList<>();

    public Languages(DataGenerator gen, String locale, MissingEntriesPolicy policy) {
        super(gen, Wares.ID, locale);
        this.locale = locale;
        this.policy = policy;
    }

    @Override
    protected void addTranslations() {
        if (locale.equals("en_us"))
            en_us();
        else if (locale.equals("uk_ua"))
            uk_ua();

        checkForMissingTranslations();
    }


    protected void en_us() {
        translate(Lang.BLOCK_DELIVERY_TABLE, "Delivery Table");
        translate(Lang.BLOCK_CARDBOARD_BOX, "Cardboard Box");
        translate(Lang.BLOCK_PACKAGE, "Package");

        translate(Lang.ITEM_SEALED_DELIVERY_AGREEMENT, "Sealed Delivery Agreement");
        translate(Lang.ITEM_DAMAGED_SEALED_DELIVERY_AGREEMENT, "Damaged Sealed Delivery Agreement");
        translate(Lang.ITEM_DELIVERY_AGREEMENT, "Delivery Agreement");
        translate(Lang.ITEM_COMPLETED_DELIVERY_AGREEMENT, "Completed Delivery Agreement");
        translate(Lang.ITEM_EXPIRED_DELIVERY_AGREEMENT, "Expired Delivery Agreement");

        translate(Lang.SEALED_AGREEMENT_DAMAGED_ERROR_MESSAGE, "The letter is badly damaged, torn paper and faded ink " +
                "make the contents unreadable.");
        translate(Lang.SEALED_AGREEMENT_UNOPENABLE_ERROR_MESSAGE, "The letter seems intact but sealed in a way that will " +
                "damage and make the contents unreadable when opened.");

        translate(Lang.ITEM_AGREEMENT_VIEW_TOOLTIP, "Right click to view");
        translate(Lang.ITEM_SEALED_AGREEMENT_INSPECT_TOOLTIP, "Right click to inspect");

        translate(Lang.VILLAGER_PACKAGER, "Packager");

        translate(Lang.GUI_AGREEMENT_TITLE, "Delivery Agreement");
        translate(Lang.GUI_AGREEMENT_MESSAGE, "I have some wares that I believe might interest you. Would you be open to trading your goods for mine?");

        translate(Lang.GUI_CARDBOARD_BOX, "Cardboard Box");
        translate(Lang.GUI_CARDBOARD_BOX_PACK, "Pack");
        translate(Lang.GUI_CARDBOARD_BOX_PACK_TOOLTIP, "Pack");

        translate(Lang.GUI_DELIVERY_TABLE_NO_AGREEMENT_TOOLTIP, "Place a Delivery Agreement to start a delivery");
        translate(Lang.GUI_DELIVERY_TABLE_NO_BOXES_TOOLTIP, "Delivery Boxes are required for delivery");
        translate(Lang.GUI_DELIVERY_TABLE_MANUAL_DELIVERY, "Deliver Manually");
        translate(Lang.GUI_DELIVERY_TABLE_MANUAL_DELIVERY_TOOLTIP, "Deliver Manually");
        translate(Lang.GUI_DELIVERY_TABLE_MANUAL_DELIVERY_TOOLTIP_EXTRA_INFO, "%sx longer");

        translate(Lang.GUI_AGREEMENT_EXPIRE_TIME, "Expire time");
        translate(Lang.GUI_AGREEMENT_EXPIRE_TIME_TOOLTIP, "Expires in: %s");
        translate(Lang.GUI_AGREEMENT_DELIVERIES, "Ordered deliveries");
        translate(Lang.GUI_AGREEMENT_DELIVERIES_TOOLTIP, "%s / %s");
        translate(Lang.GUI_AGREEMENT_COMPLETED, "Completed");
        translate(Lang.GUI_AGREEMENT_EXPIRED, "Expired");
        translate(Lang.GUI_AGREEMENT_TAG_SLOT_TOOLTIP, "Any of ");
        translate(Lang.GUI_TIME_DAYS,"%sd");
        translate(Lang.GUI_TIME_HOURS, "%sh");
        translate(Lang.GUI_TIME_MINUTES, "%sm");

        translate(Lang.GUI_SEALED_AGREEMENT_SHOW_REMAINING_TEXT_MESSAGE, "Hold Shift to show remaining text");

        translate(Lang.GUI_SEAL_TEXTURE_NOT_FOUND_MESSAGE, "Seal texture '%s' not found at '%s'. Make sure the name is correct and ResourcePack with the texture is loaded. Default seal will be used instead.");

        translate(Lang.ADVANCEMENT_LAST_MINUTES_TITLE, "At The Last Minutes");
        translate(Lang.ADVANCEMENT_LAST_MINUTES_DESCRIPTION, "Manage to complete a Delivery Agreement just before it expires");

        translate(Lang.STATS_SEALED_AGREEMENTS_OPENED, "Sealed Delivery Agreements Opened");
        translate(Lang.STATS_PACKAGES_OPENED, "Packages Opened");
        translate(Lang.STATS_INTERACT_WITH_DELIVERY_TABLE, "Interactions with Delivery Table");

        translate(Lang.COMMAND_AGREEMENT_COMPLETE_WRONG_ITEM, "Wrong item. Expected: '%s', got: '%s'");
        translate(Lang.COMMAND_AGREEMENT_COMPLETE_IS_EMPTY,  "Failed: Agreement is empty or not deserialized properly.");
        translate(Lang.COMMAND_AGREEMENT_COMPLETE_ALREADY_COMPLETED, "Failed: Agreement is already completed.");
        translate(Lang.COMMAND_AGREEMENT_COMPLETE_IS_EXPIRED, "Failed: Cannot complete an expired Agreement.");

        translate(Lang.SUBTITLE_PAPER_CRACKLE, "Paper crackles");
        translate(Lang.SUBTITLE_PAPER_TEAR, "Paper tears");
        translate(Lang.SUBTITLE_WRITING, "Quill writes on paper");
        translate(Lang.SUBTITLE_PACKAGER_WORKS, "Packager works");
    }

    protected void uk_ua() {
        translate(Lang.BLOCK_DELIVERY_TABLE, "Торговий стіл");
        translate(Lang.BLOCK_CARDBOARD_BOX, "Картонна коробка");
        translate(Lang.BLOCK_PACKAGE, "Пакунок");

        translate(Lang.ITEM_SEALED_DELIVERY_AGREEMENT, "Запечатаний торговий договір");
        translate(Lang.ITEM_DAMAGED_SEALED_DELIVERY_AGREEMENT, "Пошкоджений запечатаний торговий договір");
        translate(Lang.ITEM_DELIVERY_AGREEMENT, "Торговий договір");
        translate(Lang.ITEM_COMPLETED_DELIVERY_AGREEMENT, "Виконаний торговий договір");
        translate(Lang.ITEM_EXPIRED_DELIVERY_AGREEMENT, "Прострочений торговий договір");

        translate(Lang.SEALED_AGREEMENT_DAMAGED_ERROR_MESSAGE, "Лист сильно пошкоджений, надриви та вицвіле чорнило " +
                "роблять його нечитабельним.");
        translate(Lang.SEALED_AGREEMENT_UNOPENABLE_ERROR_MESSAGE, "Лист здається неушкодженим, але запечатаний таким чином, " +
                "що відкрити його без пошкодження вмісту, що зробить його нечитабельним, неможливо.");

        translate(Lang.ITEM_AGREEMENT_VIEW_TOOLTIP, "ПКМ щоб переглянути");
        translate(Lang.ITEM_SEALED_AGREEMENT_INSPECT_TOOLTIP, "ПКМ щоб оглянути");

        translate(Lang.VILLAGER_PACKAGER, "Пакувальник");

        translate(Lang.GUI_CARDBOARD_BOX, "Картонна коробка");
        translate(Lang.GUI_CARDBOARD_BOX_PACK, "Упакувати");
        translate(Lang.GUI_CARDBOARD_BOX_PACK_TOOLTIP, "Упакувати");

        translate(Lang.GUI_DELIVERY_TABLE_NO_AGREEMENT_TOOLTIP, "Покладіть сюди Торговий Договір щоб почати доставку");
        translate(Lang.GUI_DELIVERY_TABLE_NO_BOXES_TOOLTIP, "Для доставки товарів потрібні коробки");
        translate(Lang.GUI_DELIVERY_TABLE_MANUAL_DELIVERY, "Відправити вручну");
        translate(Lang.GUI_DELIVERY_TABLE_MANUAL_DELIVERY_TOOLTIP, "Відправити вручну");
        translate(Lang.GUI_DELIVERY_TABLE_MANUAL_DELIVERY_TOOLTIP_EXTRA_INFO, "Займе у %s раз(и) більше часу");

        translate(Lang.GUI_AGREEMENT_TITLE, "Торговий договір");
        translate(Lang.GUI_AGREEMENT_MESSAGE, "У мене є товари, які, можливо, будуть Вам цікаві. Згодні на торгівлю?");
        translate(Lang.GUI_AGREEMENT_EXPIRE_TIME, "Термін дії");
        translate(Lang.GUI_AGREEMENT_EXPIRE_TIME_TOOLTIP, "Термін дії закінчується через: %s");
        translate(Lang.GUI_AGREEMENT_DELIVERIES, "Замовлені поставки");
        translate(Lang.GUI_AGREEMENT_DELIVERIES_TOOLTIP, "%s / %s");
        translate(Lang.GUI_AGREEMENT_COMPLETED, "Виконано");
        translate(Lang.GUI_AGREEMENT_EXPIRED, "Минув термін дії");
        translate(Lang.GUI_AGREEMENT_TAG_SLOT_TOOLTIP, "Будь-що з ");
        translate(Lang.GUI_TIME_DAYS, "%sдн");
        translate(Lang.GUI_TIME_HOURS, "%sгод");
        translate(Lang.GUI_TIME_MINUTES, "%sхв");

        translate(Lang.GUI_SEALED_AGREEMENT_SHOW_REMAINING_TEXT_MESSAGE, "Утримуйте Shift для показу залишку тексту");

        translate(Lang.GUI_SEAL_TEXTURE_NOT_FOUND_MESSAGE, "Текстуру печатки '%s' не знайдено в '%s'. Переконайтеся, що назва печатки правильна та Пакет Ресурсів з цією текстурою увімкнено. Буде використана печатка за замовчуванням");

        translate(Lang.ADVANCEMENT_LAST_MINUTES_TITLE, "В Останній Момент");
        translate(Lang.ADVANCEMENT_LAST_MINUTES_DESCRIPTION, "Встигніть виконати Торговий договір в останні хвилини терміну дії");

        translate(Lang.STATS_SEALED_AGREEMENTS_OPENED, "Відкрито Запечатаних торгових договорів");
        translate(Lang.STATS_PACKAGES_OPENED, "Пакунків відкрито");
        translate(Lang.STATS_INTERACT_WITH_DELIVERY_TABLE, "Взаємодії з Торговим столом");

        translate(Lang.COMMAND_AGREEMENT_COMPLETE_WRONG_ITEM, "Неправильний предмет. Очікувався: '%s', отримано: '%s'");
        translate(Lang.COMMAND_AGREEMENT_COMPLETE_IS_EMPTY, "Помилка: Договір порожній або неправильно прочитаний.");
        translate(Lang.COMMAND_AGREEMENT_COMPLETE_ALREADY_COMPLETED, "Помилка: Договір вже виконаний.");
        translate(Lang.COMMAND_AGREEMENT_COMPLETE_IS_EXPIRED, "Помилка: Неможливо виконати Договір у якого закінчився термін дії.");

        translate(Lang.SUBTITLE_PAPER_CRACKLE, "Папір мнеться");
        translate(Lang.SUBTITLE_PAPER_TEAR, "Папір рветься");
        translate(Lang.SUBTITLE_WRITING, "Перо пише на папері");
        translate(Lang.SUBTITLE_PACKAGER_WORKS, "Пакувальник працює");
    }

    protected void translate(Lang key, String translation) {
        translatedKeys.add(key.key);
        add(key.key, translation);
    }

    protected void checkForMissingTranslations() {
        List<String> langKeys = Arrays.stream(Lang.values()).map(lang -> lang.key).toList();
        List<String> untranslated = new ArrayList<>();

        for (String langKey : langKeys) {
            if (!translatedKeys.contains(langKey))
                untranslated.add(langKey);
        }

        if (untranslated.size() > 0) {
            String message = "[" + locale + "] - Not all Lang entries have been translated.\n" + String.join("\n", untranslated);

            if (policy == MissingEntriesPolicy.EXCEPTION)
                throw new IllegalStateException(message);
            else if (policy == MissingEntriesPolicy.WARNING)
                Wares.LOGGER.warn(message);
        }
    }
}
