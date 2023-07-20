package io.github.mortuusars.wares.client.gui.agreement.element;

import io.github.mortuusars.wares.Wares;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.Map;

@SuppressWarnings("unused")
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

    public static final String SEAL_FOLDER = "textures/gui/seal";
    public static final String DEFAULT = "default";
    public static final int WIDTH = 48;
    public static final int HEIGHT = 48;

    private final String name;
    private final ResourceLocation path;

    public Seal(String name) {
        this.name = name;
        this.path = Wares.resource(SEAL_FOLDER + "/" + name + ".png");
    }

    public static Seal defaultSeal() {
        return new Seal(DEFAULT);
    }

    public String getName() {
        return name;
    }

    public ResourceLocation getTexturePath() {
        return path;
    }

    public boolean isTextureValid() {
        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        Map<ResourceLocation, Resource> resourceLocationResourceMap = resourceManager.listResources(SEAL_FOLDER,
                resourceLocation -> resourceLocation.equals(getTexturePath()));
        return resourceLocationResourceMap.size() > 0;
    }
}
