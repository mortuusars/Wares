package io.github.mortuusars.wares.data.bill;

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

public final class Bill {
    public static final Codec<Bill> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ComponentCodec.CODEC.optionalFieldOf("buyerName").forGetter(Bill::getBuyerName),
                    ComponentCodec.CODEC.optionalFieldOf("buyerAddress").forGetter(Bill::getBuyerAddress),
                    ComponentCodec.CODEC.optionalFieldOf("title").forGetter(Bill::getTitle),
                    ComponentCodec.CODEC.optionalFieldOf("message").forGetter(Bill::getMessage),
                    Codec.list(ItemStack.CODEC).fieldOf("requestedItems").forGetter(Bill::getRequestedItems),
                    Codec.list(ItemStack.CODEC).fieldOf("paymentItems").forGetter(Bill::getPaymentItems),
                    Codec.INT.optionalFieldOf("orderedQuantity", -1).forGetter(Bill::getOrderedQuantity),
                    Codec.INT.optionalFieldOf("quantity", -1).forGetter(Bill::getQuantity),
                    Codec.INT.optionalFieldOf("experience", -1).forGetter(Bill::getExperience),
                    Codec.INT.optionalFieldOf("deliveryTime", -1).forGetter(Bill::getDeliveryDuration),
                    Codec.LONG.optionalFieldOf("expireTime", -1L).forGetter(Bill::getExpireTime))
            .apply(instance, Bill::new));

    public static final Bill EMPTY = new Bill(
            Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
            Collections.emptyList(), Collections.emptyList(), -1, -1, -1, -1, -1);

    private static final String BILL_TAG = "Bill";

    private final Optional<Component> buyerName;
    private final Optional<Component> buyerAddress;
    private final Optional<Component> title;
    private final Optional<Component> message;
    private final List<ItemStack> requestedItems;
    private final List<ItemStack> paymentItems;
    private final int orderedQuantity;
    private final int experience;
    private final int deliveryDuration;
    private final long expireTime;
    private int quantity;

    public Bill(Optional<Component> buyerName, Optional<Component> buyerAddress, Optional<Component> title, Optional<Component> message,
                List<ItemStack> requestedItems, List<ItemStack> paymentItems,
                int orderedQuantity, int quantity, int experience, int deliveryDuration, long expireTime) {
        this.buyerName = buyerName;
        this.buyerAddress = buyerAddress;
        this.title = title;
        this.message = message;
        this.requestedItems = requestedItems;
        this.paymentItems = paymentItems;
        this.orderedQuantity = orderedQuantity;
        this.quantity = quantity;
        this.experience = experience;
        this.deliveryDuration = deliveryDuration;
        this.expireTime = expireTime;
    }

    public static Optional<Bill> fromItemStack(ItemStack itemStack) {
        if (itemStack.hasTag() && itemStack.getTag().contains(BILL_TAG, Tag.TAG_COMPOUND)) {
            CompoundTag billTag = itemStack.getTag().getCompound(BILL_TAG);

            try {
                DataResult<Pair<Bill, Tag>> result = CODEC.decode(NbtOps.INSTANCE, billTag);
                return Optional.of(result.getOrThrow(false, s -> {
                }).getFirst());
            } catch (Exception e) {
                Wares.LOGGER.error("Failed to decode Bill from item : '" + itemStack + "'.\n" + e);
            }
        }

        return Optional.empty();
    }

    public boolean toItemStack(ItemStack stack) {
        try {
            CompoundTag tag = stack.getOrCreateTag();
            DataResult<Tag> result = CODEC.encodeStart(NbtOps.INSTANCE, this);
            tag.put(BILL_TAG, result.getOrThrow(false, s -> {
            }));
            return true;
        } catch (Exception e) {
            Wares.LOGGER.error("Failed to encode Bill to item :\n" + e);
            return false;
        }
    }


    public boolean hasExpirationTime() {
        return expireTime > 0;
    }

    public boolean isExpired(long gameTime) {
        return hasExpirationTime() && expireTime <= gameTime;
    }


    public void setQuantity(int quantity) {
        this.quantity = quantity;
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

    public int getOrderedQuantity() {
        return orderedQuantity;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getExperience() {
        return experience;
    }

    public int getDeliveryDuration() {
        return deliveryDuration > 0 ? deliveryDuration : 100; // TODO: config duration
    }

    public long getExpireTime() {
        return expireTime;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Bill) obj;
        return Objects.equals(this.buyerName, that.buyerName) &&
                Objects.equals(this.buyerAddress, that.buyerAddress) &&
                Objects.equals(this.title, that.title) &&
                Objects.equals(this.message, that.message) &&
                Objects.equals(this.requestedItems, that.requestedItems) &&
                Objects.equals(this.paymentItems, that.paymentItems) &&
                this.orderedQuantity == that.orderedQuantity &&
                this.quantity == that.quantity &&
                this.experience == that.experience &&
                this.expireTime == that.expireTime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(buyerName, buyerAddress, title, message, requestedItems, paymentItems, orderedQuantity, quantity, experience, expireTime);
    }

    @Override
    public String toString() {
        return "Bill[" +
                "buyerName=" + buyerName + ", " +
                "buyerAddress=" + buyerAddress + ", " +
                "title=" + title + ", " +
                "message=" + message + ", " +
                "requestedItems=" + requestedItems + ", " +
                "paymentItems=" + paymentItems + ", " +
                "orderedQuantity=" + orderedQuantity + ", " +
                "quantity=" + quantity + ", " +
                "experience=" + experience + ", " +
                "expireTime=" + expireTime + ']';
    }

    public String toJsonString() {
        Optional<JsonElement> jsonElement = CODEC.encodeStart(JsonOps.INSTANCE, this).resultOrPartial(s -> {});
        return jsonElement.isPresent() ? jsonElement.get().toString() : "{}";
    }

    //TODO: Remove
    private static Optional<Component> deserializeComponent(Optional<String> json) {
        return json.map(Component.Serializer::fromJsonLenient);
    }
}
