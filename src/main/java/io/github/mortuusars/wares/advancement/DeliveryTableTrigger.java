package io.github.mortuusars.wares.advancement;

import com.google.gson.JsonObject;
import io.github.mortuusars.wares.block.entity.DeliveryTableBlockEntity;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class DeliveryTableTrigger extends SimpleCriterionTrigger<DeliveryTableTrigger.TriggerInstance> {
    private final ResourceLocation id;

    public DeliveryTableTrigger(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return id;
    }

    @Override
    protected DeliveryTableTrigger.@NotNull TriggerInstance createInstance(@NotNull JsonObject json,
                                                                           @NotNull ContextAwarePredicate predicate,
                                                                           @NotNull DeserializationContext conditionsParser) {
        return new DeliveryTableTrigger.TriggerInstance(getId(), predicate,
                AgreementPredicate.fromJson(json.get("agreement")),
                NbtPredicate.fromJson(json.get("agreement_nbt")),
                LocationPredicate.fromJson(json.get("location")));
    }

    public void trigger(ServerPlayer player, DeliveryTableBlockEntity tableBlockEntity) {
        this.trigger(player, triggerInstance -> triggerInstance.matches(player, tableBlockEntity));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance
    {
        private final AgreementPredicate agreement;
        private final NbtPredicate agreementNbt;
        private final LocationPredicate location;

        public TriggerInstance(ResourceLocation id, ContextAwarePredicate predicate, AgreementPredicate agreementPredicate,
                               NbtPredicate agreementNbtPredicate, LocationPredicate locationPredicate) {
            super(id, predicate);
            agreement = agreementPredicate;
            agreementNbt = agreementNbtPredicate;
            location = locationPredicate;
        }

        public boolean matches(ServerPlayer player, DeliveryTableBlockEntity tableBlockEntity) {
            if (!(tableBlockEntity.getLevel() instanceof ServerLevel serverLevel))
                return false;

            BlockPos pos = tableBlockEntity.getBlockPos();
            double x = pos.getX();
            double y = pos.getY();
            double z = pos.getZ();

            return agreement.matches(tableBlockEntity.getAgreement())
                    && agreementNbt.matches(tableBlockEntity.getAgreementItem().getTag())
                    && location.matches(serverLevel, x, y, z);
        }

        public @NotNull JsonObject serializeToJson(@NotNull SerializationContext pConditions) {
            JsonObject jsonobject = super.serializeToJson(pConditions);
            jsonobject.add("agreement", this.agreement.serializeToJson());
            jsonobject.add("agreement_nbt", this.agreementNbt.serializeToJson());
            jsonobject.add("location", this.location.serializeToJson());
            return jsonobject;
        }
    }
}