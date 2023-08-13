package io.github.mortuusars.wares.data.agreement.component;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum CompoundTagCompareBehavior implements StringRepresentable {
    /**
     * Any tag (or lack of it) will do.
     */
    IGNORE("ignore"),
    /**
     * Stack should have the necessary tags (can have other - they are ignored)
     */
    WEAK("weak"),
    /**
     * Stack tags should match completely (other tags are not allowed)
     */
    STRONG("strong");

    private final String name;

    CompoundTagCompareBehavior(String name) {
        this.name = name;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }
}
