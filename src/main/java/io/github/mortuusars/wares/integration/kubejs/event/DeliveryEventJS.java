package io.github.mortuusars.wares.integration.kubejs.event;

import dev.latvian.mods.kubejs.level.SimpleLevelEventJS;
import io.github.mortuusars.wares.block.entity.DeliveryTableBlockEntity;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public class DeliveryEventJS extends SimpleLevelEventJS {
    private final DeliveryTableBlockEntity blockEntity;
    private final @Nullable ServerPlayer player;

    public DeliveryEventJS(DeliveryTableBlockEntity blockEntity, @Nullable ServerPlayer player) {
        super(blockEntity.getLevel());
        this.blockEntity = blockEntity;
        this.player = player;
    }

    public DeliveryTableBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public @Nullable ServerPlayer getPlayer() {
        return player;
    }
}
