package io.github.mortuusars.wares.data.generation.provider;

import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.data.Lang;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SoundDefinition;
import net.minecraftforge.common.data.SoundDefinitionsProvider;

public class Sounds extends SoundDefinitionsProvider {
    public Sounds(DataGenerator generator, ExistingFileHelper helper) {
        super(generator, Wares.ID, helper);
    }

    @Override
    public void registerSounds() {
        add(Wares.SoundEvents.AGREEMENT_TEAR.get(), definition()
                .subtitle(Lang.SUBTITLE_AGREEMENT_TEAR.key)
                .with(multiple(3, "item/book/open_flip", 1f, 1f)));

        add(Wares.SoundEvents.AGREEMENT_CRACKLE.get(), definition()
                .subtitle(Lang.SUBTITLE_AGREEMENT_TEAR.key)
                .with(multiple(3, "item/book/open_flip", 1.5f, 0.9f)));
    }

    private SoundDefinition.Sound[] multiple(int count, String name, float volume, float pitch) {
        SoundDefinition.Sound[] sounds = new SoundDefinition.Sound[count];
        for (int i = 0; i < count; i++) {
            sounds[i] = sound(name + (i + 1)).volume(volume).pitch(pitch);
        }
        return sounds;
    }
}
