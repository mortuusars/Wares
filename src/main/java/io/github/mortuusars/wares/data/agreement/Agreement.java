package io.github.mortuusars.wares.data.agreement;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.data.serialization.ComponentCodec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Agreement {
    public static final Codec<Agreement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.optionalFieldOf("id", "").forGetter(Agreement::getId),
                    ComponentCodec.CODEC.optionalFieldOf("buyerName", TextComponent.EMPTY).forGetter(Agreement::getBuyerName),
                    ComponentCodec.CODEC.optionalFieldOf("buyerAddress", TextComponent.EMPTY).forGetter(Agreement::getBuyerAddress),
                    ComponentCodec.CODEC.optionalFieldOf("title", TextComponent.EMPTY).forGetter(Agreement::getTitle),
                    ComponentCodec.CODEC.optionalFieldOf("message", TextComponent.EMPTY).forGetter(Agreement::getMessage),
                    Codec.list(ItemStack.CODEC).fieldOf("requestedItems").forGetter(Agreement::getRequestedItems),
                    Codec.list(ItemStack.CODEC).fieldOf("paymentItems").forGetter(Agreement::getPaymentItems),
                    Codec.INT.optionalFieldOf("ordered", 0).forGetter(Agreement::getOrdered),
                    Codec.INT.optionalFieldOf("delivered", 0).forGetter(Agreement::getDelivered),
                    Codec.INT.optionalFieldOf("experience", 0).forGetter(Agreement::getExperience),
                    Codec.INT.optionalFieldOf("deliveryTime", 0).forGetter(Agreement::getDeliveryTime),
                    Codec.LONG.optionalFieldOf("expireTime", -1L).forGetter(Agreement::getExpireTime),
                    Codec.BOOL.optionalFieldOf("isCompleted", false).forGetter(Agreement::getIsCompleted),
                    Codec.BOOL.optionalFieldOf("isExpired", false).forGetter(Agreement::getIsExpired))
            .apply(instance, Agreement::new));

    public static final Agreement EMPTY = new AgreementBuilder()
            .addRequestedItem(ItemStack.EMPTY)
            .addPaymentItem(ItemStack.EMPTY)
            .build();

    private final @NotNull String id;
    private final @NotNull Component buyerName;
    private final @NotNull Component buyerAddress;
    private final @NotNull Component title;
    private final @NotNull Component message;
    private final List<ItemStack> requestedItems;
    private final List<ItemStack> paymentItems;
    private final int ordered;
    private final int experience;
    private final int deliveryTime;

    private long expireTime;
    private int delivered;
    private boolean isCompleted;
    private boolean isExpired;

    public Agreement(@NotNull String id, @NotNull Component buyerName, @NotNull Component buyerAddress, @NotNull Component title, @NotNull Component message,
                     List<ItemStack> requestedItems, List<ItemStack> paymentItems,
                     int orderedQuantity, int delivered, int experience, int deliveryTime, long expireTime,
                     boolean isCompleted, boolean isExpired) {
        this.id = id;
        this.buyerName = buyerName;
        this.buyerAddress = buyerAddress;
        this.title = title;
        this.message = message;
        this.requestedItems = requestedItems;
        this.paymentItems = paymentItems;
        this.ordered = orderedQuantity;
        this.delivered = delivered;
        this.experience = experience;
        this.deliveryTime = deliveryTime;
        this.expireTime = expireTime;
        this.isCompleted = isCompleted;
        this.isExpired = isExpired;

//        if (requestedItems.size() == 0 && paymentItems.size() == 0)
//            throw new IllegalArgumentException("Agreement can't have both requestedItems and paymentItems empty. " +
//                    "At least one of them must be defined. " + this + ".");
    }

    public static AgreementBuilder builder() {
        return new AgreementBuilder();
    }

    public static Optional<Agreement> fromItemStack(ItemStack itemStack) {
        @Nullable CompoundTag compoundTag = itemStack.getTag();
        if (compoundTag == null)
            return Optional.empty();

        try {
            DataResult<Pair<Agreement, Tag>> result = CODEC.decode(NbtOps.INSTANCE, compoundTag);
            Agreement agreement = result.getOrThrow(false, Wares.LOGGER::error).getFirst();
            return Optional.of(agreement);
        } catch (Exception e) {
            Wares.LOGGER.error("Failed to decode Agreement from item : '" + itemStack + "'.\n" + e);
            return Optional.empty();
        }
    }

    public boolean toItemStack(ItemStack stack) {
        try {
            CompoundTag tag = stack.getOrCreateTag();
            DataResult<Tag> result = CODEC.encodeStart(NbtOps.INSTANCE, this);
            Tag encodedTag = result.getOrThrow(false, Wares.LOGGER::error);
            tag.merge((CompoundTag) encodedTag);
            return true;
        } catch (Exception e) {
            Wares.LOGGER.error("Failed to encode Agreement to item :\n" + e);
            return false;
        }
    }

    public @NotNull String getId() {
        return this.id;
    }
    public @NotNull Component getBuyerName() {
        return buyerName;
    }
    public @NotNull Component getBuyerAddress() {
        return buyerAddress;
    }
    public @NotNull Component getTitle() {
        return title;
    }
    public @NotNull Component getMessage() {
        return message;
    }
    public List<ItemStack> getRequestedItems() {
        return requestedItems;
    }
    public List<ItemStack> getPaymentItems() {
        return paymentItems;
    }
    public int getOrdered() {
        return ordered;
    }
    public int getDelivered() {
        return delivered;
    }
    public boolean isInfinite() { return getOrdered() <= 0; }
    public int getExperience() {
        return experience;
    }
    public int getDeliveryTime() { return deliveryTime; }
    public int getDeliveryTimeOrDefault() {
        return deliveryTime > 0 ? deliveryTime : 40; // TODO: config duration
    }
    public long getExpireTime() {
        return expireTime;
    }
    protected boolean getIsExpired() {
        return isExpired;
    }
    public boolean isExpired(long gameTime) { return (isExpired && !isCompleted()) || (getExpireTime() >= 0 && getExpireTime() <= gameTime); }
    protected boolean getIsCompleted() { return isCompleted; }
    public boolean isCompleted() {
        return isCompleted || (getOrdered() > 0 && getDelivered() == getOrdered());
    }

    public void setDelivered(int delivered) {
        this.delivered = delivered;
    }
    public void addDelivered(int add) {
        if (this.delivered < 0)
            this.delivered = 0;

        this.delivered += add;
    }

    public void onDeliver() {
        addDelivered(1);

        if (isCompleted())
            complete();
    }

    public boolean canDeliver(long gameTime) {
        return !isCompleted() && !isExpired(gameTime);
    }

    public void complete() {
        this.isCompleted = true;
        this.expireTime = -1;

        if (getDelivered() < getOrdered())
            this.delivered = getOrdered();
    }

    public void expire() {
        this.isExpired = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Agreement agreement = (Agreement) o;
        return ordered == agreement.ordered && experience == agreement.experience && deliveryTime == agreement.deliveryTime
                && expireTime == agreement.expireTime && delivered == agreement.delivered
                && isCompleted == agreement.isCompleted && isExpired == agreement.isExpired && id.equals(agreement.id)
                && buyerName.equals(agreement.buyerName) && buyerAddress.equals(agreement.buyerAddress) && title.equals(agreement.title)
                && message.equals(agreement.message)
                && requestedItems.stream().equals(agreement.requestedItems) && paymentItems.equals(agreement.paymentItems);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, buyerName, buyerAddress, title, message, requestedItems, paymentItems, ordered, experience, deliveryTime, expireTime, delivered, isCompleted, isExpired);
    }

    @Override
    public String toString() {
        return "Agreement{" +
                "id='" + id + '\'' +
                ", buyerName=" + buyerName +
                ", buyerAddress=" + buyerAddress +
                ", title=" + title +
                ", message=" + message +
                ", requestedItems=" + requestedItems +
                ", paymentItems=" + paymentItems +
                ", ordered=" + ordered +
                ", experience=" + experience +
                ", deliveryTime=" + deliveryTime +
                ", expireTime=" + expireTime +
                ", delivered=" + delivered +
                ", isCompleted=" + isCompleted +
                ", isExpired=" + isExpired +
                '}';
    }
}
