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
        add(LangKeys.GUI_EXPIRED, "Expired");
        add(LangKeys.GUI_EXPIRES_IN, "Expires in ");
        add(LangKeys.GUI_TIME_DAYS, "%sd");
        add(LangKeys.GUI_TIME_HOURS, "%sh");
        add(LangKeys.GUI_TIME_MINUTES, "%sm");
    }
}
