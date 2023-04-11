package io.github.mortuusars.wares.mixin;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.villager.ai.behavior.ShowWorkItem;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.VillagerGoalPackages;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(VillagerGoalPackages.class)
public abstract class VillagerGoalPackagesMixin {
    @Inject(method = "getWorkPackage", cancellable = true, at = @At("RETURN"))
    private static void getWorkPackage(VillagerProfession profession, float speedModifier,
                                       CallbackInfoReturnable<ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>>> cir) {
        if (profession == Wares.Villagers.PACKAGER.get()) {
            List<Pair<Integer, ? extends Behavior<? super Villager>>> villagerList = new ArrayList<>(cir.getReturnValue());
            villagerList.add(Pair.of(11, new ShowWorkItem(new ItemStack(Wares.Items.CARDBOARD_BOX.get()), 30, 150)));
            Wares.LOGGER.info("getWorkPackage mixin applied");
            cir.setReturnValue(ImmutableList.copyOf(villagerList));
        }
    }
}
