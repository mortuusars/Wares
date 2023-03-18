package io.github.mortuusars.wares.data.agreement;

import net.minecraft.util.StringRepresentable;

public enum AgreementStatus implements StringRepresentable {
    NONE("none"),
    IN_PROGRESS("in_progress"),
    COMPLETED("completed"),
    EXPIRED("expired");

    private final String name;

    AgreementStatus(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
