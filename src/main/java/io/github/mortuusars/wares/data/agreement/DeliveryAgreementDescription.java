package io.github.mortuusars.wares.data.agreement;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public record DeliveryAgreementDescription(Optional<TextProvider> buyerName,
                                           Optional<TextProvider> buyerAddress,
                                           Optional<TextProvider> title,
                                           Optional<TextProvider> message,
                                           Either<String, List<ItemStack>> requested,
                                           Either<String, List<ItemStack>> payment,
                                           Either<Integer, SteppedInt> ordered,
                                           Either<Integer, SteppedInt> experience,
                                           Either<Integer, SteppedInt> deliveryTime,
                                           Either<Long, SteppedInt> expiresInSeconds) {
    public static final Codec<DeliveryAgreementDescription> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TextProvider.CODEC.optionalFieldOf("buyerName").forGetter(DeliveryAgreementDescription::buyerName),
            TextProvider.CODEC.optionalFieldOf("buyerAddress").forGetter(DeliveryAgreementDescription::buyerAddress),
            TextProvider.CODEC.optionalFieldOf("title").forGetter(DeliveryAgreementDescription::title),
            TextProvider.CODEC.optionalFieldOf("message").forGetter(DeliveryAgreementDescription::message),
            Codec.either(Codec.STRING, Codec.list(ItemStack.CODEC)).fieldOf("requested").forGetter(DeliveryAgreementDescription::requested),
            Codec.either(Codec.STRING, Codec.list(ItemStack.CODEC)).fieldOf("payment").forGetter(DeliveryAgreementDescription::payment),
            Codec.either(Codec.INT, SteppedInt.CODEC).optionalFieldOf("ordered", Either.left(-1)).forGetter(DeliveryAgreementDescription::ordered),
            Codec.either(Codec.INT, SteppedInt.CODEC).optionalFieldOf("experience", Either.left(-1)).forGetter(DeliveryAgreementDescription::experience),
            Codec.either(Codec.INT, SteppedInt.CODEC).optionalFieldOf("deliveryTime", Either.left(-1)).forGetter(DeliveryAgreementDescription::deliveryTime),
            Codec.either(Codec.LONG, SteppedInt.CODEC).optionalFieldOf("expiresInSeconds", Either.left(-1L)).forGetter(DeliveryAgreementDescription::expiresInSeconds)
        ).apply(instance, DeliveryAgreementDescription::new));

    // TODO: REALIZE TO CONCRETE AGREEMENT.

    /*private final Optional<Either<Component, List<Either<Component,WeightedComponent>>>> buyerName;
    private final Optional<Either<Component, List<Pair<Component, Integer>>>> buyerAddress;
    private final Optional<Either<Component, List<Pair<Component, Integer>>>> title;
    private final Optional<Either<Component, List<Pair<Component, Integer>>>> message;
    private final Either<String, List<ItemStack>> requestedItems;
    private final Either<String, List<ItemStack>> paymentItems;

    public DeliveryAgreementDescription(Optional<Either<Component, List<Either<Component,WeightedComponent>>>> buyerName,
                                        Optional<Either<Component, List<Pair<Component, Integer>>>> buyerAddress,
                                        Optional<Either<Component, List<Pair<Component, Integer>>>> title,
                                        Optional<Either<Component, List<Pair<Component, Integer>>>> message,
                                        Either<String, List<ItemStack>> requestedItems,
                                        Either<String, List<ItemStack>> paymentItems) {
        this.buyerName = buyerName;
        this.buyerAddress = buyerAddress;
        this.title = title;
        this.message = message;
    }*/

//    public Optional<Either<Component, List<Either<Component,WeightedComponent>>>> getBuyerName() {
//        return buyerName;
//    }
//
//    public Optional<Either<Component, List<Pair<Component, Integer>>>> getBuyerAddress() {
//        return buyerAddress;
//    }
//
//    public Optional<Either<Component, List<Pair<Component, Integer>>>> getTitle() {
//        return title;
//    }
//
//    public Optional<Either<Component, List<Pair<Component, Integer>>>> getMessage() {
//        return message;
//    }
//
//    public Either<String, List<ItemStack>> getRequestedItems() {
//        return requestedItems;
//    }
//
//    public Either<String, List<ItemStack>> getPaymentItems() {
//        return paymentItems;
//    }
}
