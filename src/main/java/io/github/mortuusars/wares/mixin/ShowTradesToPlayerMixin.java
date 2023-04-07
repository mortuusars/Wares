package io.github.mortuusars.wares.mixin;

import io.github.mortuusars.wares.Wares;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.behavior.ShowTradesToPlayer;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShowTradesToPlayer.class)
public abstract class ShowTradesToPlayerMixin {

    /**
     * ShowTradesToPlayer#tick interferes with any ways to set held item for villager.
     * They could remove only their display item, but now we have to use this to fix it.
     */
    @Inject(method = "clearHeldItem", cancellable = true, at = @At("HEAD"))
    private static void clearHeldItem(Villager villager, CallbackInfo ci) {
        if (villager.getItemInHand(InteractionHand.MAIN_HAND).is(Wares.Items.DELIVERY_PACKAGE.get()))
            ci.cancel();
    }
}
