package io.github.mortuusars.wares.client.gui.agreement.element;

import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.data.Lang;
import net.minecraft.ChatFormatting;
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
        Map<ResourceLocation, Resource> resourceLocationResourceMap = resourceManager.listResources(SEAL_FOLDER, this::sealTextureFilter);
        return resourceLocationResourceMap.size() > 0;
    }

    private boolean sealTextureFilter(ResourceLocation resourceLocation) {
        return resourceLocation.equals(getTexturePath());
    }

    public Seal defaultIfNotFound() {
        return isTextureValid() ? this : defaultSeal();
    }

    public Seal printErrorAndFallbackToDefaultIfNotFound() {
        boolean textureValid = isTextureValid();

        if (!textureValid) {
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.displayClientMessage(Lang.GUI_SEAL_TEXTURE_NOT_FOUND_MESSAGE
                        .translate(name, path).withStyle(ChatFormatting.RED), false);
            }
            else {
                Wares.LOGGER.error("Seal texture '{}' not found at '{}'. Check if name is correct and ResourcePack is loaded.", name, path);
            }
        }

        return textureValid ? this : defaultSeal();
    }
}
