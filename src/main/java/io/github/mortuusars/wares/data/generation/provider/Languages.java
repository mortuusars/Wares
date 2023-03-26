package io.github.mortuusars.wares.data.generation.provider;

import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.data.Lang;
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
        if (locale.equals("en_us")) {
            for (Lang langEntry : Lang.values()) {
                add(langEntry.key, langEntry.en_us);
            }
        }

        if (locale.equals("uk_ua")) {
            for (Lang langEntry : Lang.values()) {
                add(langEntry.key, langEntry.uk_ua);
            }
        }
    }
}
