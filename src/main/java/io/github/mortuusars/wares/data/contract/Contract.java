package io.github.mortuusars.wares.data.contract;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;


public record Contract(Optional<Component> buyerName, Optional<Component> buyerAddress, Optional<Component> title, Optional<Component> message,
                       List<ItemStack> requestedItems, List<ItemStack> paymentItems, int quantity,
                       int experience, long expireTime) {

    public static final Codec<Contract> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("buyerName").forGetter(contract -> contract.buyerName.map(component -> Component.Serializer.toJson(component))),
            Codec.STRING.optionalFieldOf("buyerAddress").forGetter(contract -> contract.buyerAddress.map(component -> Component.Serializer.toJson(component))),
            Codec.STRING.optionalFieldOf("title").forGetter(contract -> contract.title.map(component -> Component.Serializer.toJson(component))),
            Codec.STRING.optionalFieldOf("message").forGetter(contract -> contract.message.map(component -> Component.Serializer.toJson(component))),
            Codec.list(ItemStack.CODEC).fieldOf("requestedItems").forGetter(Contract::requestedItems),
            Codec.list(ItemStack.CODEC).fieldOf("paymentItems").forGetter(Contract::paymentItems),
            Codec.INT.optionalFieldOf("quantity", -1).forGetter(Contract::quantity),
            Codec.INT.optionalFieldOf("experience", -1).forGetter(Contract::experience),
            Codec.LONG.optionalFieldOf("expireTime", -1L).forGetter(Contract::expireTime))
        .apply(instance, (name, address, title, message, requested, payment, quant, xp, expire) ->
                new Contract(deserializeComponent(name), deserializeComponent(address), deserializeComponent(title), deserializeComponent(message),
                        requested, payment, quant, xp, expire)));

    private static Optional<Component> deserializeComponent(Optional<String> json) {
        return json.map(s -> Component.Serializer.fromJsonLenient(s));
    }
}
