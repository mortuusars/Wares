package io.github.mortuusars.wares.data.generation.provider;

import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.data.LangKeys;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

public class Languages extends LanguageProvider {
    private final String locale;

    public Languages(DataGenerator gen, String locale) {
        super(gen, Wares.ID, locale);
        this.locale = locale;
    }

    @Override
    protected void addTranslations() {
        if (locale.equals("en_us"))
            addEN_US();
    }

    protected void addEN_US() {
        add(Wares.Items.SEALED_AGREEMENT.get(), "Sealed Delivery Agreement");
        add(Wares.Items.SEALED_AGREEMENT.get().getDescriptionId() + "_damaged", "Damaged Sealed Delivery Agreement");
        add(Wares.Items.DELIVERY_AGREEMENT.get(), "Delivery Agreement");

        add(LangKeys.SEALED_AGREEMENT_DAMAGED_MESSAGE, "The letter is badly damaged, torn shape and faded ink make the contents unreadable.");
        add(LangKeys.SEALED_AGREEMENT_UNOPENABLE_MESSAGE, "The letter seems intact but sealed in a way that will damage and make the contents unreadable when opened.");

        add(LangKeys.GUI_DELIVERY_AGREEMENT_TITLE, "Delivery Agreement");
        add(LangKeys.GUI_DELIVERY_AGREEMENT_EXPIRES, "Expire time");
        add(LangKeys.GUI_DELIVERY_AGREEMENT_EXPIRES_TOOLTIP, "Expires in: %s");
        add(LangKeys.GUI_DELIVERY_AGREEMENT_ORDERS, "Deliveries");
        add(LangKeys.GUI_DELIVERY_AGREEMENT_ORDERS_TOOLTIP, "%s / %s");

        add(LangKeys.GUI_EXPIRED, "Expired");
        add(LangKeys.GUI_EXPIRES_IN, "Expires in ");
        add(LangKeys.GUI_TIME_DAYS, "%sd");
        add(LangKeys.GUI_TIME_HOURS, "%sh");
        add(LangKeys.GUI_TIME_MINUTES, "%sm");
    }
}
