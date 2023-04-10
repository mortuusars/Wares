package io.github.mortuusars.wares.data.agreement;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.config.Config;
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

@SuppressWarnings({"unused", "BooleanMethodIsAlwaysInverted"})
public class Agreement {
    public static final Codec<Agreement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.optionalFieldOf("id", "").forGetter(Agreement::getId),
                    ComponentCodec.CODEC.optionalFieldOf("buyerName", TextComponent.EMPTY).forGetter(Agreement::getBuyerName),
                    ComponentCodec.CODEC.optionalFieldOf("buyerAddress", TextComponent.EMPTY).forGetter(Agreement::getBuyerAddress),
                    ComponentCodec.CODEC.optionalFieldOf("title", TextComponent.EMPTY).forGetter(Agreement::getTitle),
                    ComponentCodec.CODEC.optionalFieldOf("message", TextComponent.EMPTY).forGetter(Agreement::getMessage),
                    Codec.STRING.optionalFieldOf("seal", "default").forGetter(Agreement::getSeal),
                    Codec.list(ItemStack.CODEC).fieldOf("requestedItems").forGetter(Agreement::getRequestedItems),
                    Codec.list(ItemStack.CODEC).fieldOf("paymentItems").forGetter(Agreement::getPaymentItems),
                    Codec.INT.optionalFieldOf("ordered", 0).forGetter(Agreement::getOrdered),
                    Codec.INT.optionalFieldOf("delivered", 0).forGetter(Agreement::getDelivered),
                    Codec.INT.optionalFieldOf("experience", 0).forGetter(Agreement::getExperience),
                    Codec.INT.optionalFieldOf("deliveryTime", 0).forGetter(Agreement::getDeliveryTime),
                    Codec.LONG.optionalFieldOf("expireTimestamp", -1L).forGetter(Agreement::getExpireTimestamp),
                    Codec.BOOL.optionalFieldOf("isCompleted", false).forGetter(Agreement::getIsCompleted),
                    Codec.BOOL.optionalFieldOf("isExpired", false).forGetter(Agreement::getIsExpired))
            .apply(instance, Agreement::new));

    public static final int MAX_REQUESTED_STACKS = 6;
    public static final int MAX_PAYMENT_STACKS = 6;

    public static final Agreement EMPTY = new AgreementBuilder()
            .addRequestedItem(ItemStack.EMPTY)
            .addPaymentItem(ItemStack.EMPTY)
            .build();

    private final @NotNull String id;
    private final @NotNull Component buyerName;
    private final @NotNull Component buyerAddress;
    private final @NotNull Component title;
    private final @NotNull Component message;
    private final @NotNull String seal;
    private final List<ItemStack> requestedItems;
    private final List<ItemStack> paymentItems;
    private final int ordered;
    private final int experience;
    private final int deliveryTime;
    private final long expireTimestamp;

    private int delivered;
    private boolean isCompleted;
    private boolean isExpired;

    public Agreement(@NotNull String id, @NotNull Component buyerName, @NotNull Component buyerAddress, @NotNull Component title, @NotNull Component message, @NotNull String seal,
                     List<ItemStack> requestedItems, List<ItemStack> paymentItems,
                     int orderedQuantity, int delivered, int experience, int deliveryTime, long expireTimestamp,
                     boolean isCompleted, boolean isExpired) {
        this.id = id;
        this.buyerName = buyerName;
        this.buyerAddress = buyerAddress;
        this.title = title;
        this.message = message;
        this.seal = seal;
        this.requestedItems = requestedItems;
        this.paymentItems = paymentItems;
        this.ordered = orderedQuantity;
        this.delivered = delivered;
        this.experience = experience;
        this.deliveryTime = deliveryTime;
        this.expireTimestamp = expireTimestamp;
        this.isCompleted = isCompleted;
        this.isExpired = isExpired;
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
    public @NotNull String getSeal() {
        return seal;
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
        return deliveryTime > 0 ? deliveryTime : Config.DEFAULT_DELIVERY_TIME.get();
    }
    public long getExpireTimestamp() {
        return expireTimestamp;
    }
    protected boolean getIsExpired() {
        return isExpired;
    }
    public boolean canExpire() {
        return getExpireTimestamp() > -1L;
    }
    public boolean isExpired(long gameTime) {
        if (isCompleted())
            return false;

        return isExpired || (getExpireTimestamp() >= 0 && getExpireTimestamp() <= gameTime);
    }
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

        if (getDelivered() < getOrdered())
            this.delivered = getOrdered();
    }

    public void expire() {
        this.isExpired = true;
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Agreement agreement = (Agreement) o;
        return ordered == agreement.ordered && experience == agreement.experience && deliveryTime == agreement.deliveryTime
                && expireTimestamp == agreement.expireTimestamp && delivered == agreement.delivered
                && isCompleted == agreement.isCompleted && isExpired == agreement.isExpired && id.equals(agreement.id)
                && buyerName.equals(agreement.buyerName) && buyerAddress.equals(agreement.buyerAddress) && title.equals(agreement.title)
                && message.equals(agreement.message)
                && requestedItems.equals(agreement.requestedItems) && paymentItems.equals(agreement.paymentItems);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, buyerName, buyerAddress, title, message, requestedItems, paymentItems, ordered, experience, deliveryTime, expireTimestamp, delivered, isCompleted, isExpired);
    }

    @Override
    public String toString() {
        return "Agreement{" +
                "id='" + id + '\'' +
                ", buyerName=" + buyerName +
                ", buyerAddress=" + buyerAddress +
                ", title=" + title +
                ", message=" + message +
                ", seal='" + seal + '\'' +
                ", requestedItems=" + requestedItems +
                ", paymentItems=" + paymentItems +
                ", ordered=" + ordered +
                ", experience=" + experience +
                ", deliveryTime=" + deliveryTime +
                ", expireTimestamp=" + expireTimestamp +
                ", delivered=" + delivered +
                ", isCompleted=" + isCompleted +
                ", isExpired=" + isExpired +
                '}';
    }
}
