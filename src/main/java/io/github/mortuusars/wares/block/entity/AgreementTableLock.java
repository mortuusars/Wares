package io.github.mortuusars.wares.block.entity;

import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.data.agreement.DeliveryAgreement;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class AgreementTableLock {
    protected boolean locked;
    @Nullable
    protected Component buyerNameLock = null;
    @Nullable
    protected Component buyerAddressLock = null;
    @Nullable
    protected String sealLock = null;
    protected UnlockBehavior unlockBehavior = UnlockBehavior.COMPLETED_OR_EXPIRED;

    protected final DeliveryTableBlockEntity blockEntity;

    public AgreementTableLock(DeliveryTableBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    public boolean isLocked() {
        return shouldBeLocked() && !unlockBehavior.shouldUnlock(blockEntity.getAgreementItem());
    }

    private boolean shouldBeLocked() {
        DeliveryAgreement agreement = blockEntity.agreement;
        if (blockEntity.getAgreementItem().isEmpty())
            return false;

        return locked
                || agreement.getBuyerName().equals(buyerNameLock)
                || agreement.getBuyerAddress().equals(buyerAddressLock)
                || agreement.getSeal().equals(sealLock);
    }

    public CompoundTag save(CompoundTag tag) {
        if (this.locked)
            tag.putBoolean("Locked", true);

        if (this.buyerNameLock != null)
            tag.putString("MatchBuyerName", Component.Serializer.toJson(this.buyerNameLock));

        if (this.buyerAddressLock != null)
            tag.putString("MatchBuyerAddress", Component.Serializer.toJson(this.buyerAddressLock));

        if (this.sealLock != null)
            tag.putString("MatchSeal", sealLock);

        tag.putString("UnlockBehavior", unlockBehavior.getSerializedName());

        return tag;
    }

    public void load(CompoundTag tag) {
        this.locked = tag.getBoolean("Locked");

        String buyerNameLock = tag.getString("MatchBuyerName");
        if (buyerNameLock.length() > 0)
            this.buyerNameLock = Component.Serializer.fromJson(buyerNameLock);

        String buyerAddressLock = tag.getString("MatchBuyerAddress");
        if (buyerAddressLock.length() > 0)
            this.buyerAddressLock = Component.Serializer.fromJson(buyerAddressLock);

        String sealLock = tag.getString("MatchSeal");
        if (sealLock.length() > 0)
            this.sealLock = sealLock;

        String unlockBehaviorString = tag.getString("UnlockBehavior");
        this.unlockBehavior = UnlockBehavior.byName(unlockBehaviorString, UnlockBehavior.COMPLETED_OR_EXPIRED);
    }

    public enum UnlockBehavior implements StringRepresentable {
        NEVER("never", stack -> false),
        WHEN_COMPLETED("completed", stack -> stack.is(Wares.Items.COMPLETED_DELIVERY_AGREEMENT.get())),
        WHEN_EXPIRED("expired", stack -> stack.is(Wares.Items.EXPIRED_DELIVERY_AGREEMENT.get())),
        COMPLETED_OR_EXPIRED("completed_or_expired", stack ->
                stack.is(Wares.Items.COMPLETED_DELIVERY_AGREEMENT.get()) || stack.is(Wares.Items.EXPIRED_DELIVERY_AGREEMENT.get()));

        private final String name;
        private final Predicate<ItemStack> predicate;

        UnlockBehavior(String name, Predicate<ItemStack> predicate) {
            this.name = name;
            this.predicate = predicate;
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }

        public static UnlockBehavior byName(@NotNull String name, UnlockBehavior defaultValue) {
            for (UnlockBehavior value : UnlockBehavior.values()) {
                if (value.getSerializedName().equals(name))
                    return value;
            }

            return defaultValue;
        }

        public boolean shouldUnlock(ItemStack agreementStack) {
            return predicate.test(agreementStack);
        }
    }
}
