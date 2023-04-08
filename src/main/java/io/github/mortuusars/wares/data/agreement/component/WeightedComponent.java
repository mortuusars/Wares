package io.github.mortuusars.wares.data.agreement.component;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.wares.data.serialization.ComponentCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.util.random.Weight;

public record WeightedComponent(Component component, Weight weight) {
    private static final Codec<WeightedComponent> WEIGHTED_CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ComponentCodec.CODEC.fieldOf("component").forGetter(WeightedComponent::component),
                    Weight.CODEC.optionalFieldOf("weight", Weight.of(1)).forGetter(WeightedComponent::weight))
            .apply(instance, WeightedComponent::new));
    public static final Codec<Either<Component, WeightedComponent>> REGULAR_OR_WEIGHTED_CODEC = Codec.either(ComponentCodec.CODEC, WEIGHTED_CODEC);

    public static final Codec<WeightedComponent> CODEC = REGULAR_OR_WEIGHTED_CODEC.flatXmap(
            regularOrWeighted -> DataResult.success(regularOrWeighted.map(WeightedComponent::of, weightedComponent -> weightedComponent)),
            weightedComponent -> DataResult.success(Either.right(weightedComponent)));

    public static WeightedComponent of(Component component) {
        return new WeightedComponent(component, Weight.of(1));
    }

    public static WeightedComponent of(Component component, int weight) {
        return new WeightedComponent(component, Weight.of(weight));
    }
}
