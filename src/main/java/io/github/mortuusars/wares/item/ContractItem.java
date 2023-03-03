package io.github.mortuusars.wares.item;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.data.contract.Contract;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;

public class ContractItem extends Item {
    public ContractItem(Properties properties) {
        super(properties);
    }

    public Contract fromJson(String json) {
        return Contract.CODEC.decode(JsonOps.INSTANCE, GsonHelper.parse(json))
                .getOrThrow(false, error -> Wares.LOGGER.error("Failed to deserialize Contract from json: " + error))
                .getFirst();
    }
}
