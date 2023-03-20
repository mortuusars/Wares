package io.github.mortuusars.wares.data.agreement;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.data.serialization.ComponentCodec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class Agreement {
    public static final Codec<Agreement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ComponentCodec.CODEC.optionalFieldOf("buyerName").forGetter(Agreement::getBuyerName),
                    ComponentCodec.CODEC.optionalFieldOf("buyerAddress").forGetter(Agreement::getBuyerAddress),
                    ComponentCodec.CODEC.optionalFieldOf("title").forGetter(Agreement::getTitle),
                    ComponentCodec.CODEC.optionalFieldOf("message").forGetter(Agreement::getMessage),
                    Codec.list(ItemStack.CODEC).fieldOf("requestedItems").forGetter(Agreement::getRequestedItems),
                    Codec.list(ItemStack.CODEC).fieldOf("paymentItems").forGetter(Agreement::getPaymentItems),
                    Codec.INT.optionalFieldOf("ordered", -1).forGetter(Agreement::getOrdered),
                    Codec.INT.optionalFieldOf("remaining", -1).forGetter(Agreement::getRemaining),
                    Codec.INT.optionalFieldOf("experience", -1).forGetter(Agreement::getExperience),
                    Codec.INT.optionalFieldOf("deliveryTime", -1).forGetter(Agreement::getDeliveryTimeOrDefault),
                    Codec.LONG.optionalFieldOf("expireTime", -1L).forGetter(Agreement::getExpireTime))
            .apply(instance, Agreement::new));

    public static final Agreement EMPTY = new Agreement(
            Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
            Collections.emptyList(), Collections.emptyList(), -1, -1, -1, -1, -1);

    private static final String AGREEMENT_TAG = "Agreement";

    private final Optional<Component> buyerName;
    private final Optional<Component> buyerAddress;
    private final Optional<Component> title;
    private final Optional<Component> message;
    private final List<ItemStack> requestedItems;
    private final List<ItemStack> paymentItems;
    private final int ordered;
    private final int experience;
    private final int deliveryTime;
    private final long expireTime;
    private int remaining;

    public Agreement(Optional<Component> buyerName, Optional<Component> buyerAddress, Optional<Component> title, Optional<Component> message,
                     List<ItemStack> requestedItems, List<ItemStack> paymentItems,
                     int orderedQuantity, int quantity, int experience, int deliveryTime, long expireTime) {
        this.buyerName = buyerName;
        this.buyerAddress = buyerAddress;
        this.title = title;
        this.message = message;
        this.requestedItems = requestedItems;
        this.paymentItems = paymentItems;
        this.ordered = orderedQuantity;
        this.remaining = quantity;
        this.experience = experience;
        this.deliveryTime = deliveryTime;
        this.expireTime = expireTime;
    }

    public static Optional<Agreement> fromItemStack(ItemStack itemStack) {
        if (itemStack.hasTag() && itemStack.getTag().contains(AGREEMENT_TAG, Tag.TAG_COMPOUND)) {
            CompoundTag agreementTag = itemStack.getTag().getCompound(AGREEMENT_TAG);

            try {
                DataResult<Pair<Agreement, Tag>> result = CODEC.decode(NbtOps.INSTANCE, agreementTag);
                return Optional.of(result.getOrThrow(false, s -> {
                }).getFirst());
            } catch (Exception e) {
                Wares.LOGGER.error("Failed to decode DeliveryAgreement from item : '" + itemStack + "'.\n" + e);
            }
        }

        return Optional.empty();
    }

    public boolean toItemStack(ItemStack stack) {
        try {
            CompoundTag tag = stack.getOrCreateTag();
            DataResult<Tag> result = CODEC.encodeStart(NbtOps.INSTANCE, this);
            tag.put(AGREEMENT_TAG, result.getOrThrow(false, s -> {
            }));
            return true;
        } catch (Exception e) {
            Wares.LOGGER.error("Failed to encode DeliveryAgreement to item :\n" + e);
            return false;
        }
    }

    public boolean isInfinite() {
        return getOrdered() <= 0;
    }

    public boolean isCompleted() {
        return !isInfinite() && getRemaining() <= 0;
    }

    public boolean canExpire() {
        return expireTime > 0;
    }

    public boolean isExpired(long gameTime) {
        return canExpire() && expireTime <= gameTime;
    }

    public boolean isNotExpired(long gameTime) {
        return !isExpired(gameTime);
    }


    public void setRemaining(int remaining) {
        this.remaining = remaining;
    }

    public Optional<Component> getBuyerName() {
        return buyerName;
    }

    public Optional<Component> getBuyerAddress() {
        return buyerAddress;
    }

    public Optional<Component> getTitle() {
        return title;
    }

    public Optional<Component> getMessage() {
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

    public int getRemaining() {
        return remaining;
    }

    public int getDelivered() {
        return isInfinite() ? -1 : Math.max(0, getOrdered() - getRemaining());
    }

    public int getExperience() {
        return experience;
    }

    public int getDeliveryTimeOrDefault() {
        return deliveryTime > 0 ? deliveryTime : 40; // TODO: config duration
    }

    public long getExpireTime() {
        return expireTime;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Agreement) obj;
        return Objects.equals(this.buyerName, that.buyerName) &&
                Objects.equals(this.buyerAddress, that.buyerAddress) &&
                Objects.equals(this.title, that.title) &&
                Objects.equals(this.message, that.message) &&
                Objects.equals(this.requestedItems, that.requestedItems) &&
                Objects.equals(this.paymentItems, that.paymentItems) &&
                this.ordered == that.ordered &&
                this.remaining == that.remaining &&
                this.experience == that.experience &&
                this.deliveryTime == that.deliveryTime &&
                this.expireTime == that.expireTime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(buyerName, buyerAddress, title, message, requestedItems, paymentItems, ordered, remaining, experience, expireTime);
    }

    @Override
    public String toString() {
        return "DeliveryAgreement[" +
                "buyerName=" + buyerName + ", " +
                "buyerAddress=" + buyerAddress + ", " +
                "title=" + title + ", " +
                "message=" + message + ", " +
                "requestedItems=" + requestedItems + ", " +
                "paymentItems=" + paymentItems + ", " +
                "ordered=" + ordered + ", " +
                "remaining=" + remaining + ", " +
                "experience=" + experience + ", " +
                "deliveryTime=" + deliveryTime + ", " +
                "expireTime=" + expireTime + ']';
    }

    public String toJsonString() {
        Optional<JsonElement> jsonElement = CODEC.encodeStart(JsonOps.INSTANCE, this).resultOrPartial(s -> {});
        return jsonElement.isPresent() ? jsonElement.get().toString() : "{}";
    }
}
