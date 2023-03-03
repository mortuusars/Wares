package io.github.mortuusars.wares.data.provider;

import io.github.mortuusars.wares.Wares;
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

    }
}
