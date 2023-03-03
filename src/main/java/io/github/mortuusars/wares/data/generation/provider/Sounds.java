package io.github.mortuusars.wares.data.generation.provider;

import io.github.mortuusars.wares.Wares;
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
//        add(Salt.Sounds.SALT_DISSOLVE.get(), definition()
//                .subtitle(LangKeys.SUBTITLES_DISSOLVES)
//                .with(multiple(6, "block/beehive/drip", 0.6f, 0.6f)));
    }

    private SoundDefinition.Sound[] multiple(int count, String name, float volume, float pitch) {
        SoundDefinition.Sound[] sounds = new SoundDefinition.Sound[count];
        for (int i = 0; i < count; i++) {
            sounds[i] = sound(name + (i + 1)).volume(volume).pitch(pitch);
        }
        return sounds;
    }
}
