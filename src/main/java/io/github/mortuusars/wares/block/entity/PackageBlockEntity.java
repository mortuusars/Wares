package io.github.mortuusars.wares.block.entity;

import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.data.Package;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class PackageBlockEntity extends BlockEntity {
    private Package pack;

    public PackageBlockEntity(BlockPos pos, BlockState blockState) {
        super(Wares.BlockEntities.PACKAGE.get(), pos, blockState);
    }

    public void setPackage(Package pack) {
        this.pack = pack;
        setChanged();
    }

    public Package getPackage() {
        return pack;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        pack = Package.fromTag(tag).orElse(Package.DEFAULT);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (pack != null)
            pack.toTag(tag);
        else {
            boolean a = true;
        }
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        super.onDataPacket(net, pkt);
        handleUpdateTag(pkt.getTag());
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        return serializeNBT();
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        load(tag);
    }
}
