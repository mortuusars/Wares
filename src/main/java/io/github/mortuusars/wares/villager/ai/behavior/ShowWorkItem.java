package io.github.mortuusars.wares.villager.ai.behavior;

import com.google.common.collect.ImmutableMap;
import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.block.entity.DeliveryTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ShowWorkItem extends Behavior<Villager> {
    private static final int SHOW_COOLDOWN_MAX = 200;
    private static final int SHOW_COOLDOWN_MIN = 40;
    private static final int LAST_WORKED_TIME_LIMIT = 400;

    private final ItemStack item;
    private long showCooldownTimestamp;

    public ShowWorkItem(ItemStack item, int minDuration, int maxDuration) {
        super(ImmutableMap.of(MemoryModuleType.JOB_SITE, MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.LAST_WORKED_AT_POI, MemoryStatus.VALUE_PRESENT), minDuration, maxDuration);
        this.item = item;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, @NotNull Villager villager) {
        if (level.getGameTime() < showCooldownTimestamp)
            return false;

        Optional<GlobalPos> jobSiteMemory = villager.getBrain().getMemory(MemoryModuleType.JOB_SITE);
        if (jobSiteMemory.isPresent()) {
            BlockPos jobSitePos = jobSiteMemory.get().pos();
            if (level.isLoaded(jobSitePos)
                    && level.getBlockEntity(jobSitePos) instanceof DeliveryTableBlockEntity deliveryTableBlockEntity
                    && !deliveryTableBlockEntity.getAgreementItem().is(Wares.Items.DELIVERY_AGREEMENT.get()))
                return false;
        }

        Brain<Villager> brain = villager.getBrain();
        Optional<Long> lastWorkedMemory = brain.getMemory(MemoryModuleType.LAST_WORKED_AT_POI);
        return lastWorkedMemory.isPresent() && level.getGameTime() - lastWorkedMemory.get() <= LAST_WORKED_TIME_LIMIT;
    }

    @Override
    protected boolean canStillUse(@NotNull ServerLevel level, @NotNull Villager villager, long pGameTime) {
        return checkExtraStartConditions(level, villager) ;
    }

    @Override
    protected void start(@NotNull ServerLevel level, @NotNull Villager villager, long gameTime) {
        super.start(level, villager, gameTime);
        villager.setItemSlot(EquipmentSlot.MAINHAND, item);
        villager.setDropChance(EquipmentSlot.MAINHAND, 0f);
        level.playSound(null, villager, Wares.SoundEvents.VILLAGER_WORK_PACKAGER.get(), SoundSource.NEUTRAL,
                0.8f, level.getRandom().nextFloat() * 0.15f + 1f);
    }

    @Override
    protected void stop(@NotNull ServerLevel level, @NotNull Villager villager, long gameTime) {
        super.stop(level, villager, gameTime);
        villager.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        villager.setDropChance(EquipmentSlot.MAINHAND, 0.085f);
        level.playSound(null, villager, Wares.SoundEvents.VILLAGER_WORK_PACKAGER.get(), SoundSource.NEUTRAL,
                0.8f, level.getRandom().nextFloat() * 0.15f + 0.75f);
        showCooldownTimestamp = gameTime + level.getRandom().nextInt(SHOW_COOLDOWN_MIN, SHOW_COOLDOWN_MAX);
    }
}
