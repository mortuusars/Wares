package io.github.mortuusars.wares.data.serialization;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public class ComponentCodec {
    public static final PrimitiveCodec<Component> CODEC = new PrimitiveCodec<Component>() {
        @Override
        public <T> DataResult<Component> read(DynamicOps<T> ops, T input) {
            Optional<String> stringOptional = ops.getStringValue(input).resultOrPartial(s -> {});

            return stringOptional.isPresent() ?
                    DataResult.success(Component.Serializer.fromJsonLenient(stringOptional.get()))
                    : DataResult.error("Cannot read string from OPS.");
        }

        @Override
        public <T> T write(DynamicOps<T> ops, Component value) {
            return ops.createString(Component.Serializer.toJson(value));
        }
    };
}
