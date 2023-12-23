package io.github.mortuusars.wares.advancement;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import io.github.mortuusars.wares.data.agreement.DeliveryAgreement;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AgreementPredicate {
    public static final AgreementPredicate ANY = new AgreementPredicate(null, null,
            MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY);

    @Nullable
    private final String idPredicate;
    @Nullable
    private final String sealPredicate;
    @NotNull
    private final MinMaxBounds.Ints orderedPredicate;
    @NotNull
    private final MinMaxBounds.Ints deliveredPredicate;
    @NotNull
    private final MinMaxBounds.Ints experiencePredicate;
    @NotNull
    private final MinMaxBounds.Ints deliveryTimePredicate;

    public AgreementPredicate(@Nullable String idPredicate, @Nullable String sealPredicate,
                              @NotNull MinMaxBounds.Ints orderedPredicate, @NotNull MinMaxBounds.Ints deliveredPredicate,
                              @NotNull MinMaxBounds.Ints experiencePredicate, @NotNull MinMaxBounds.Ints deliveryTimePredicate) {
        this.idPredicate = idPredicate;
        this.sealPredicate = sealPredicate;
        this.orderedPredicate = orderedPredicate;
        this.deliveredPredicate = deliveredPredicate;
        this.experiencePredicate = experiencePredicate;
        this.deliveryTimePredicate = deliveryTimePredicate;
    }

    public boolean matches(DeliveryAgreement agreement) {
        if (this == ANY)
            return true;

        if (idPredicate != null && !idPredicate.equals(agreement.getId()))
            return false;
        if (sealPredicate != null && !sealPredicate.equals(agreement.getSeal()))
            return false;
        return orderedPredicate.matches(agreement.getOrdered())
                && deliveredPredicate.matches(agreement.getDelivered())
                && experiencePredicate.matches(agreement.getExperience())
                && deliveryTimePredicate.matches(agreement.getDeliveryTime());
    }

    public JsonElement serializeToJson() {
        if (this == ANY)
            return JsonNull.INSTANCE;

        JsonObject json = new JsonObject();
        if (idPredicate != null)
            json.addProperty("id", idPredicate);
        if (sealPredicate != null)
            json.addProperty("seal", sealPredicate);

        json.add("ordered", orderedPredicate.serializeToJson());
        json.add("delivered", deliveredPredicate.serializeToJson());
        json.add("experience", experiencePredicate.serializeToJson());
        json.add("delivery_time", deliveryTimePredicate.serializeToJson());
        return json;
    }

    public static AgreementPredicate fromJson(@Nullable JsonElement jsonElement) {
        if (jsonElement == null || jsonElement.isJsonNull())
            return ANY;

        JsonObject json = GsonHelper.convertToJsonObject(jsonElement, "agreement");

        return new AgreementPredicate(
                json.has("id") ? GsonHelper.convertToString(json, "id") : null,
                json.has("seal") ? GsonHelper.convertToString(json, "seal") : null,
                MinMaxBounds.Ints.fromJson(json.get("ordered")),
                MinMaxBounds.Ints.fromJson(json.get("delivered")),
                MinMaxBounds.Ints.fromJson(json.get("experience")),
                MinMaxBounds.Ints.fromJson(json.get("delivery_time")));
    }
}
