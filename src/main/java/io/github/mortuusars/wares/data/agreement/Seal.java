package io.github.mortuusars.wares.data.agreement;

import io.github.mortuusars.wares.Wares;
import net.minecraft.resources.ResourceLocation;

public class Seal {
    public enum Element {
        SHADOW(0),
        STRING(1),
        BASE(2),
        LOGO(3);

        private final int index;

        Element(int index) {
            this.index = index;
        }

        public int getVOffset() {
            return index * HEIGHT;
        }
    }

    public static final int WIDTH = 48;
    public static final int HEIGHT = 48;

    private final String name;
    private final ResourceLocation path;

    public Seal(String name) {
        this.name = name;
        this.path = Wares.resource("textures/gui/seal/%s.png".formatted(name));
    }

    public String getName() {
        return name;
    }

    public ResourceLocation getTexturePath() {
        return path;
    }
}
