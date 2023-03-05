package io.github.mortuusars.wares.data.bill;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.wares.Wares;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.Optional;


public record Bill(Optional<Component> buyerName, Optional<Component> buyerAddress, Optional<Component> title, Optional<Component> message,
                   List<ItemStack> requestedItems, List<ItemStack> paymentItems,
                   int quantity, int experience, long expireTime) {
    public static final Codec<Bill> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("buyerName").forGetter(bill -> bill.buyerName.map(component -> Component.Serializer.toJson(component))),
            Codec.STRING.optionalFieldOf("buyerAddress").forGetter(bill -> bill.buyerAddress.map(component -> Component.Serializer.toJson(component))),
            Codec.STRING.optionalFieldOf("title").forGetter(bill -> bill.title.map(component -> Component.Serializer.toJson(component))),
            Codec.STRING.optionalFieldOf("message").forGetter(bill -> bill.message.map(component -> Component.Serializer.toJson(component))),
            Codec.list(ItemStack.CODEC).fieldOf("requestedItems").forGetter(Bill::requestedItems),
            Codec.list(ItemStack.CODEC).fieldOf("paymentItems").forGetter(Bill::paymentItems),
            Codec.INT.optionalFieldOf("quantity", -1).forGetter(Bill::quantity),
            Codec.INT.optionalFieldOf("experience", -1).forGetter(Bill::experience),
            Codec.LONG.optionalFieldOf("expireTime", -1L).forGetter(Bill::expireTime))
        .apply(instance, (name, address, title, message, requested, payment, quant, xp, expire) ->
                new Bill(deserializeComponent(name), deserializeComponent(address), deserializeComponent(title), deserializeComponent(message),
                        requested, payment, quant, xp, expire)));

    public static final Bill EMPTY = new Bill(
            Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
            Collections.emptyList(), Collections.emptyList(), -1, -1, -1);

    private static final String BILL_TAG = "Bill";

    public static Optional<Bill> fromItemStack(ItemStack itemStack) {
        if (itemStack.hasTag() && itemStack.getTag().contains(BILL_TAG, Tag.TAG_COMPOUND)) {
            CompoundTag billTag = itemStack.getTag().getCompound(BILL_TAG);

            try {
                DataResult<Pair<Bill, Tag>> result = CODEC.decode(NbtOps.INSTANCE, billTag);
                return Optional.of(result.getOrThrow(false, s -> {}).getFirst());
            }
            catch (Exception e) {
                Wares.LOGGER.error("Failed to decode Bill from item : '" + itemStack + "'.\n" + e);
            }
        }

        return Optional.empty();
    }

    public int getDeliveryDuration() {
        return 60;
    }

    private static Optional<Component> deserializeComponent(Optional<String> json) {
        return json.map(Component.Serializer::fromJsonLenient);
    }
}
