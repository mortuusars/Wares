package io.github.mortuusars.wares.data.agreement;

import io.github.mortuusars.wares.Wares;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public enum AgreementType implements StringRepresentable {
    NONE("none"),
    SEALED("sealed"),
    REGULAR("regular"),
    COMPLETED("completed"),
    EXPIRED("expired");

    private final String name;

    AgreementType(String name) {
        this.name = name;
    }

    public static AgreementType fromItemStack(ItemStack stack) {
        if (stack.is(Wares.Items.SEALED_DELIVERY_AGREEMENT.get()))
            return SEALED;
        else if (stack.is(Wares.Items.DELIVERY_AGREEMENT.get()))
            return REGULAR;
        else if (stack.is(Wares.Items.COMPLETED_DELIVERY_AGREEMENT.get()))
            return COMPLETED;
        else if (stack.is(Wares.Items.EXPIRED_DELIVERY_AGREEMENT.get()))
            return EXPIRED;
        else
            return NONE;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }
}
