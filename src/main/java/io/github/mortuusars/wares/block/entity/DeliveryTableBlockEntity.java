package io.github.mortuusars.wares.block.entity;

import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.block.DeliveryTableBlock;
import io.github.mortuusars.wares.config.Config;
import io.github.mortuusars.wares.data.Lang;
import io.github.mortuusars.wares.data.agreement.Agreement;
import io.github.mortuusars.wares.data.agreement.AgreementType;
import io.github.mortuusars.wares.item.AgreementItem;
import io.github.mortuusars.wares.menu.DeliveryTableMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings({"SameParameterValue", "BooleanMethodIsAlwaysInverted", "unused"})
public class DeliveryTableBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    public static final int SLOTS = 14;
    public static final int AGREEMENT_SLOT = 0;
    public static final int PACKAGES_SLOT = 1;
    public static final int[] AGREEMENT_SLOTS = new int[] {0};
    public static final int[] AGREEMENT_PLUS_PACKAGES_SLOTS = new int[] {0,1};
    public static final int[] INPUT_PLUS_AGREEMENT_PLUS_PACKAGES_SLOTS = new int[] {0,1,2,3,4,5,6,7};
    public static final int[] INPUT_PLUS_PACKAGES_SLOTS = new int[] {1,2,3,4,5,6,7};
    public static final int[] INPUT_SLOTS = new int[] {2,3,4,5,6,7};
    public static final int[] OUTPUT_SLOTS = new int[] {8,9,10,11,12,13};

    public static final int PACKAGER_WORK_RADIUS = 3;
    public static final int PACKAGER_LAST_WORK_THRESHOLD = 20 * 40; // 40 seconds = 800 ticks

    public static final int CONTAINER_DATA_SIZE = 3;
    public static final int CONTAINER_DATA_PROGRESS = 0;
    public static final int CONTAINER_DATA_DURATION = 1;
    public static final int CONTAINER_DATA_CAN_DELIVER_MANUALLY = 2;

    protected final ContainerData containerData = new ContainerData() {
        public int get(int id) {
            return switch (id) {
                case CONTAINER_DATA_PROGRESS -> DeliveryTableBlockEntity.this.progress;
                case CONTAINER_DATA_DURATION -> DeliveryTableBlockEntity.this.getDeliveryTime();
                case CONTAINER_DATA_CAN_DELIVER_MANUALLY -> Config.MANUAL_DELIVERY_ALLOWED.get() && DeliveryTableBlockEntity.this.canDeliverManually ? 1 : 0;
                default -> 0;
            };
        }

        public void set(int id, int value) {
            if (id == CONTAINER_DATA_PROGRESS)
                DeliveryTableBlockEntity.this.progress = value;
            else if (id == CONTAINER_DATA_CAN_DELIVER_MANUALLY)
                DeliveryTableBlockEntity.this.canDeliverManually = Config.MANUAL_DELIVERY_ALLOWED.get() && value == 1;
        }

        public int getCount() {
            return CONTAINER_DATA_SIZE;
        }
    };

    protected final ItemStackHandler inventory;
    protected LazyOptional<IItemHandlerModifiable>[] inventoryHandlers;
    protected int progress = 0;
    protected boolean canDeliverManually = false;
    protected boolean deliveringManually = false;

    protected Agreement agreement = Agreement.EMPTY;

    public DeliveryTableBlockEntity(BlockPos pos, BlockState blockState) {
        super(Wares.BlockEntities.DELIVERY_TABLE.get(), pos, blockState);
        inventory = createInventory(SLOTS);
        inventoryHandlers = SidedInvWrapper.create(this, Direction.DOWN, Direction.UP, Direction.NORTH);
    }

    public void serverTick() {
        if (level == null)
            return;

        convertAgreementStackIfNeeded();

        if (!getAgreementItem().is(Wares.Items.DELIVERY_AGREEMENT.get())) {
            if (progress > 0){
                resetProgress();
                setChanged();
            }
            return;
        }

        int prevProgress = progress;
        Deliverability deliverability = getDeliverability();

        if (deliverability == Deliverability.CAN_DELIVER) {
            boolean packagerWorkingAtTable = isPackagerWorkingAtTable();
            if (!deliveringManually && Config.PACKAGER_REQUIRED.get() && !packagerWorkingAtTable) {
                canDeliverManually = Config.MANUAL_DELIVERY_ALLOWED.get();
                return;
            }
            else
                canDeliverManually = false;

            if (deliveringManually && packagerWorkingAtTable) {
                // Adjusting progress to not complete instantly when worker arrives.
                double completion = (progress / (double)getDeliveryTime());
                progress = Math.round((float) (agreement.getDeliveryTimeOrDefault() * completion));

                deliveringManually = false;
            }

            progress++;
        }
        else if (deliverability != Deliverability.NO_SPACE_FOR_OUTPUT)
            resetProgress();

        if (progress >= getDeliveryTime()) {
            int deliveredPackages = deliver(getBatchSize());
            if (deliveredPackages > 0)
                onBatchDelivered(deliveredPackages);
        }

        if (prevProgress != progress)
            setChanged();
    }

    private void onBatchDelivered(final int deliveredBatches) {
        deliveringManually = false;
        assert level != null;
        level.playSound(null, getBlockPos(), Wares.SoundEvents.CARDBOARD_FALL.get(), SoundSource.BLOCKS,
                0.85f, level.getRandom().nextFloat() * 0.1f + 0.95f);
        if (!getAgreement().isInfinite())
            level.playSound(null, getBlockPos(), Wares.SoundEvents.WRITING.get(), SoundSource.BLOCKS,
                    0.5f, level.getRandom().nextFloat() * 0.1f + 0.95f);

        Optional<Villager> worker = getPackagerWorker(16);
        if (worker.isPresent()) {
            Villager packager = worker.get();
            int xp = packager.getVillagerXp() + deliveredBatches;
            packager.setVillagerXp(xp);

            int villagerLevel = packager.getVillagerData().getLevel();
            if (VillagerData.canLevelUp(villagerLevel) && xp >= Config.getMaxXpPerLevel(villagerLevel)) {
                level.playSound(null, packager, SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 0.75f, 1);
                packager.increaseProfessionLevelOnUpdate = true;
                packager.updateMerchantTimer = 30;
            }
        }
    }

    protected boolean isPackagerWorkingAtTable() {
        Optional<Villager> worker = getPackagerWorker(PACKAGER_WORK_RADIUS);
        if (worker.isEmpty())
            return false;

        if (!Config.PACKAGER_SHOULD_BE_WORKING.get())
            return true;

        Villager packager = worker.get();
        final long lastWorkedAt = packager.getBrain().getMemory(MemoryModuleType.LAST_WORKED_AT_POI).orElse(-1L);

        if (lastWorkedAt < 0L)
            return false;

        assert level != null;
        final int timeSinceLastWork = (int)(level.getGameTime() - lastWorkedAt);
        return timeSinceLastWork < PACKAGER_LAST_WORK_THRESHOLD;
    }

    public Optional<Villager> getPackagerWorker(final int radius) {
        assert level != null;
        if (level.isClientSide)
            throw new IllegalStateException("Should not be called client-side. Only server has info about villager job site.");

        List<Villager> villagersInRadius = level.getEntitiesOfClass(Villager.class, new AABB(getBlockPos()).inflate(radius));

        for (Villager villager : villagersInRadius) {
            if (villager.getVillagerData().getProfession() == Wares.Villagers.PACKAGER.get()) {
                Optional<GlobalPos> jobSiteMemory = villager.getBrain().getMemory(MemoryModuleType.JOB_SITE);
                if (jobSiteMemory.isPresent() && jobSiteMemory.get().pos().equals(getBlockPos()))
                    return Optional.of(villager);
            }
        }

        return Optional.empty();
    }

    public Agreement getAgreement() {
        return agreement;
    }

    public int getBatchSize() {
        Optional<Villager> worker = getPackagerWorker(PACKAGER_WORK_RADIUS);
        int packages = getItem(PACKAGES_SLOT).getCount();
        int villagerLevel = worker.map(villager -> villager.getVillagerData().getLevel()).orElse(1);
        return Math.min(packages, Config.getBatchSizeForLevel(villagerLevel));
    }

    protected int getDeliveryTime() {
        int time = agreement.getDeliveryTimeOrDefault();
        return deliveringManually ? Math.round(time * Config.MANUAL_DELIVERY_TIME_MODIFIER.get().floatValue()) : time;
    }

    protected void resetProgress() {
        progress = 0;
        deliveringManually = false;
        canDeliverManually = false;
    }

    public void startManualDelivery() {
        if (Config.MANUAL_DELIVERY_ALLOWED.get() && canDeliverManually && !deliveringManually && !isPackagerWorkingAtTable() && getDeliverability() == Deliverability.CAN_DELIVER) {
            deliveringManually = true;
            canDeliverManually = false;

            // Adjusting progress to manual delivery time modifier:
            int duration = agreement.getDeliveryTimeOrDefault();
            double completion = progress / (double)duration;
            progress = (int) Math.round(getDeliveryTime() * completion);
        }
    }

    protected int deliver(final int batchCount) {
        int deliveredCount = 0;
        for (int i = 0; i < batchCount; i++) {
            // First check is just to be sure,
            // Afterward it is necessary to check because items have changed.
            if (getDeliverability() != Deliverability.CAN_DELIVER)
                return deliveredCount;

            consumePackage();
            consumeFromInputSlots(agreement.getRequestedItems());
            insertCopiesToOutputSlots(agreement.getPaymentItems());
            deliveredCount++;

            assert level != null;

            agreement.onDeliver();

            agreement.toItemStack(getAgreementItem());
            sendUpdateToNearbyClients();

            if (agreement.isCompleted()) {
                boolean almostExpired = getAgreement().canExpire() && getAgreement().getExpireTimestamp() - level.getGameTime() < 20 * 60; // 1 min
                if (almostExpired)
                    getAgreementItem().getOrCreateTag().putBoolean("almostExpired", true);
                int experience = getAgreement().getExperience();
                if (experience > 0 && level instanceof ServerLevel serverLevel)
                    ExperienceOrb.award(serverLevel, Vec3.atCenterOf(getBlockPos()).add(0, 0.5f, 0), experience);
                break;
            }
        }

        resetProgress();
        return deliveredCount;
    }

    private void consumePackage() {
        if (Config.DELIVERIES_REQUIRE_PACKAGES.get())
            removeItem(PACKAGES_SLOT, 1);
    }

    protected Deliverability getDeliverability() {
        if (agreement.isEmpty() || !agreement.canDeliver(level != null ? level.getGameTime() : 0))
            return Deliverability.AGREEMENT_INVALID;
        if (!hasPackage())
            return Deliverability.NO_PACKAGES;
        if (!hasRequestedItems())
            return Deliverability.NO_INPUT;
        if (!hasSpaceForPayment())
            return Deliverability.NO_SPACE_FOR_OUTPUT;

        return Deliverability.CAN_DELIVER;
    }

    protected boolean hasPackage() {
        return !getItem(PACKAGES_SLOT).isEmpty() || !Config.DELIVERIES_REQUIRE_PACKAGES.get();
    }

    protected boolean hasRequestedItems() {
        List<ItemStack> requestedItems = agreement.getRequestedItems();

        List<ItemStack> inputStacks = new ArrayList<>();
        for (int slotIndex : INPUT_SLOTS) {
            ItemStack stackInSlot = inventory.getStackInSlot(slotIndex);
            if (!stackInSlot.isEmpty())
                inputStacks.add(stackInSlot.copy());
        }

        for (ItemStack requestedItem : requestedItems) {
            int requiredCount = requestedItem.getCount();

            for (ItemStack stack : inputStacks) {
                if (!stack.isEmpty() && ItemStack.isSameItemSameTags(requestedItem, stack)) {
                    ItemStack split = stack.split(requiredCount);
                    requiredCount -= split.getCount();
                }

                if (requiredCount <= 0)
                    break;
            }

            if (requiredCount > 0)
                return false;
        }

        return true;
    }

    /**
     * This inventory is used to check if paymentItems would fit in the output slots by fake adding items to it.
     * There probably exists a simpler way.
     */
    private final SimpleContainer outputSpaceCheckContainer = new SimpleContainer(6);

    protected boolean hasSpaceForPayment() {
        outputSpaceCheckContainer.clearContent();

        int i = 0;
        for (int slotIndex : OUTPUT_SLOTS) {
            outputSpaceCheckContainer.setItem(i, inventory.getStackInSlot(slotIndex).copy());
            i++;
        }

        for (ItemStack stack : getAgreement().getPaymentItems()) {
            if (!outputSpaceCheckContainer.addItem(stack.copy()).isEmpty())
                return false;
        }

        return true;
    }

    protected void consumeFromInputSlots(List<ItemStack> requestedItems) {
        for (ItemStack requestedItem : requestedItems) {
            int requiredCount = requestedItem.getCount();

            for (int slotIndex : INPUT_SLOTS) {
                if (ItemStack.isSameItemSameTags(requestedItem, inventory.getStackInSlot(slotIndex))) {
                    ItemStack extractedStack = inventory.extractItem(slotIndex, requiredCount, false);
                    requiredCount -= extractedStack.getCount();

                    if (requiredCount <= 0)
                        break;
                }
            }
        }
    }

    protected void insertCopiesToOutputSlots(List<ItemStack> paymentItems) {
        for (ItemStack stack : paymentItems) {
            ItemStack insertedStack = stack.copy();
            for (int slotIndex : OUTPUT_SLOTS) {
                insertedStack = inventory.insertItem(slotIndex, insertedStack, false);
                if (insertedStack.isEmpty())
                    break;
            }
        }
    }


    // <Container>

    protected @NotNull ItemStackHandler createInventory(int slots) {
        return new ItemStackHandler(slots) {
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if (slot == AGREEMENT_SLOT)
                    return stack.getItem() instanceof AgreementItem;
                else if (slot == PACKAGES_SLOT)
                    return Config.DELIVERIES_REQUIRE_PACKAGES.get() && stack.is(Wares.Tags.Items.DELIVERY_BOXES);
                return super.isItemValid(slot, stack);
            }

            @NotNull
            @Override
            public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                if (slot == PACKAGES_SLOT && !Config.DELIVERIES_REQUIRE_PACKAGES.get())
                    return stack;
                return super.insertItem(slot, stack, simulate);
            }

            @Override
            protected void onContentsChanged(int slot) {
                if (slot == AGREEMENT_SLOT) {
                    updateBlockStateIfNeeded();
                    agreement = Agreement.fromItemStack(getItem(AGREEMENT_SLOT)).orElse(Agreement.EMPTY);
                    resetProgress();
                }
                setChanged();
            }
        };
    }

    public IItemHandlerModifiable getInventory() {
        return inventory;
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (!this.remove && cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (side == Direction.DOWN) return inventoryHandlers[0].cast();
            if (side == Direction.UP) return inventoryHandlers[1].cast();
            if (side != null) return inventoryHandlers[2].cast();
        }

        return super.getCapability(cap, side);
    }

    public int getContainerSize(){
        return inventory.getSlots();
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < getContainerSize(); i++) {
            if (!getItem(i).isEmpty())
                return false;
        }
        return true;
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        return inventory.getStackInSlot(slot);
    }

    public ItemStack getAgreementItem() {
        return getItem(AGREEMENT_SLOT);
    }

    public @NotNull ItemStack extractAgreementItem() {
        return removeItem(AGREEMENT_SLOT, 1);
    }

    public void setAgreementItem(@NotNull ItemStack stack) {
        setItem(AGREEMENT_SLOT, stack);
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        return inventory.extractItem(slot, amount, false);
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = inventory.getStackInSlot(slot);
        inventory.setStackInSlot(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, @NotNull ItemStack stack) {
        inventory.setStackInSlot(slot, stack);
    }

    @Override
    public int @NotNull [] getSlotsForFace(Direction side) {
        return switch (side) {
            case DOWN -> OUTPUT_SLOTS;
            case UP -> Config.DELIVERIES_REQUIRE_PACKAGES.get() ? AGREEMENT_PLUS_PACKAGES_SLOTS : AGREEMENT_SLOTS;
            case NORTH, SOUTH, WEST, EAST -> INPUT_SLOTS;
        };
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, @NotNull ItemStack itemStack, @Nullable Direction direction) {
        return canPlaceItem(index, itemStack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, @NotNull ItemStack pStack, @NotNull Direction direction) {
        return index >= OUTPUT_SLOTS[0];
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < inventory.getSlots(); i++) {
            inventory.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    public boolean canPlaceItem(int slotIndex, @NotNull ItemStack stack) {
        return (slotIndex == AGREEMENT_SLOT && stack.getItem() instanceof AgreementItem)
                || (slotIndex == PACKAGES_SLOT && stack.is(Wares.Tags.Items.DELIVERY_BOXES))
                || (slotIndex >= INPUT_SLOTS[0] && slotIndex < OUTPUT_SLOTS[0]);
    }

    @Override
    public boolean stillValid(@NotNull Player pPlayer) {
        assert this.level != null;
        if (this.level.getBlockEntity(this.worldPosition) != this)
            return false;
        else
            return pPlayer.distanceToSqr(this.worldPosition.getX() + 0.5D,
                    this.worldPosition.getY() + 0.5D,
                    this.worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    protected @NotNull Component getDefaultName() {
        return Lang.BLOCK_DELIVERY_TABLE.translate();
    }

    @Override
    protected @NotNull AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory) {
        return new DeliveryTableMenu(containerId, inventory, this, containerData);
    }


    // <Load/Save>

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        this.inventory.deserializeNBT(tag.getCompound("Inventory"));
        this.progress = tag.getInt("Progress");
        this.deliveringManually = tag.getBoolean("DeliveringManually");

        agreement = Agreement.fromItemStack(getAgreementItem()).orElse(Agreement.EMPTY);
        updateBlockStateIfNeeded();
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", this.inventory.serializeNBT());
        tag.putInt("Progress", progress);
        tag.putBoolean("DeliveringManually", deliveringManually);
    }

    @Override
    public void setChanged() {
        super.setChanged();
    }

    // <Updating>

    protected void convertAgreementStackIfNeeded() {
        if (!getAgreementItem().is(Wares.Items.DELIVERY_AGREEMENT.get()))
            return;

        if (getAgreement().isCompleted())
            setAgreementItem(AgreementItem.convertToCompleted(getAgreementItem()));
        else {
            assert level != null;
            if (getAgreement().isExpired(level.getGameTime()))
                setAgreementItem(AgreementItem.convertToExpired(getAgreementItem()));
        }
    }

    protected void updateBlockStateIfNeeded() {
        AgreementType type = AgreementType.fromItemStack(getAgreementItem());
        BlockState currentBlockState = getBlockState();
        if (level != null && currentBlockState.getValue(DeliveryTableBlock.AGREEMENT) != type)
            level.setBlockAndUpdate(worldPosition, currentBlockState.setValue(DeliveryTableBlock.AGREEMENT, type));
    }


    protected void sendUpdateToNearbyClients() {
        assert level != null;
        List<ServerPlayer> nearbyPlayers = level.getEntitiesOfClass(ServerPlayer.class, new AABB(getBlockPos()).inflate(32));
        for (ServerPlayer player : nearbyPlayers) {
            player.connection.send(this.getUpdatePacket());
        }
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        super.onDataPacket(net, pkt);
        handleUpdateTag(pkt.getTag());
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        return serializeNBT();
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        load(tag);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        for (LazyOptional<IItemHandlerModifiable> inventoryHandler : inventoryHandlers) {
            inventoryHandler.invalidate();
        }
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        inventoryHandlers = net.minecraftforge.items.wrapper.SidedInvWrapper.create(this, Direction.DOWN, Direction.UP, Direction.NORTH);
    }
}
