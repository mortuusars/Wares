package io.github.mortuusars.wares.block.entity;

import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.block.DeliveryTableBlock;
import io.github.mortuusars.wares.data.Lang;
import io.github.mortuusars.wares.data.agreement.AgreementType;
import io.github.mortuusars.wares.data.agreement.Agreement;
import io.github.mortuusars.wares.item.AgreementItem;
import io.github.mortuusars.wares.menu.DeliveryTableMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.npc.Villager;
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

public class DeliveryTableBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    public static final int SLOTS = 13;
    public static final int AGREEMENT_SLOT = 0;
    public static final int[] INPUT_PLUS_AGREEMENT_SLOTS = new int[] {0,1,2,3,4,5,6};
    public static final int[] INPUT_SLOTS = new int[] {1,2,3,4,5,6};
    public static final int[] OUTPUT_SLOTS = new int[] {7,8,9,10,11,12};

    public static final int CONTAINER_DATA_PROGRESS = 0;
    public static final int CONTAINER_DATA_DURATION = 1;

    protected final ContainerData containerData = new ContainerData() {
        public int get(int id) {
            return switch (id) {
                case CONTAINER_DATA_PROGRESS -> DeliveryTableBlockEntity.this.progress;
                case CONTAINER_DATA_DURATION -> DeliveryTableBlockEntity.this.getDeliveryTime();
                default -> 0;
            };
        }

        public void set(int id, int value) {
            if (id == 0) {
                DeliveryTableBlockEntity.this.progress = value;
            }

        }

        public int getCount() {
            return 2;
        }
    };

    protected final ItemStackHandler inventory;
    protected LazyOptional<IItemHandlerModifiable>[] inventoryHandlers;

    protected Agreement agreement = Agreement.EMPTY;

    protected int progress = 0;

    public DeliveryTableBlockEntity(BlockPos pos, BlockState blockState) {
        super(Wares.BlockEntities.DELIVERY_TABLE.get(), pos, blockState);
        inventory = createInventory(SLOTS);
        inventoryHandlers = SidedInvWrapper.create(this, Direction.DOWN, Direction.UP, Direction.NORTH);
    }

    public void serverTick() {
        if (level == null)
            return;

        convertAgreementStackIfNeeded();

        int prevProgress = progress;
        int deliveryTime = getDeliveryTime();
        if (deliveryTime > progress)
            progress++;

        if (!canDeliver())
            resetProgress();
        else if (progress >= deliveryTime)
            deliver(getBatchSize());

        if (progress != prevProgress)
            setChanged();
    }

    public Agreement getAgreement() {
        return agreement;
    }

    public int getBatchSize() {
        return 1;
    }

    protected int getDeliveryTime() {
        // TODO: Do not check for villager on every tick.
        int ticks = agreement.getDeliveryTimeOrDefault();
        List<Villager> villagers = level.getEntitiesOfClass(Villager.class, new AABB(getBlockPos()).inflate(1));

        //TODO: Packager profession

        if (!villagers.isEmpty()) {
            ticks = Math.max(5, (int) (ticks * 0.25f));
        }

        return ticks;
//        return 3;
    }

    protected void resetProgress() {
        progress = 0;
    }

    protected void sendUpdateToNearbyClients() {
        List<ServerPlayer> nearbyPlayers = level.getEntitiesOfClass(ServerPlayer.class, new AABB(getBlockPos()).inflate(16));
        for (ServerPlayer player : nearbyPlayers) {
            player.connection.send(this.getUpdatePacket());
        }
    }

    protected boolean deliver(int count) {
        for (int i = 0; i < count; i++) {
            // First check is just to be sure,
            // Afterwards it is necessary to check because items have changed.
            if (!canDeliver())
                return false;

            consumeFromInputSlots(agreement.getRequestedItems());
            insertCopiesToOutputSlots(agreement.getPaymentItems());

            level.playSound(null, getBlockPos(), SoundEvents.LEVER_CLICK, SoundSource.BLOCKS,
                    0.25f, level.getRandom().nextFloat() * 0.1f + 0.9f);

            agreement.onDeliver();

            agreement.toItemStack(getAgreementItem());
            sendUpdateToNearbyClients();

            if (agreement.isCompleted()) {
                resetProgress();

                int experience = getAgreement().getExperience();
                if (experience > 0 && level instanceof ServerLevel serverLevel)
                    ExperienceOrb.award(serverLevel, Vec3.atCenterOf(getBlockPos()).add(0, 0.5f, 0), experience);

                return true;
            }
        }

        resetProgress();
        return true; // All delivered.
    }

    protected boolean canDeliver() {
        return level != null
                && agreement != Agreement.EMPTY
                && agreement.canDeliver(level.getGameTime())
                && hasRequestedItems()
                && hasSpaceForPayment();
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

    protected boolean hasSpaceForPayment() {
        for (ItemStack stack : getAgreement().getPaymentItems()) {
            for (int slotIndex : OUTPUT_SLOTS) {
                stack = inventory.insertItem(slotIndex, stack, true);
                if (stack.isEmpty())
                    break;
            }

            if (!stack.isEmpty())
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
            case UP -> INPUT_PLUS_AGREEMENT_SLOTS;
            case NORTH, SOUTH, WEST, EAST -> INPUT_SLOTS;
        };
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, @NotNull ItemStack itemStack, @Nullable Direction direction) {
        return canPlaceItem(index, itemStack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, @NotNull ItemStack pStack, @NotNull Direction direction) {
        return index >= 7;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < inventory.getSlots(); i++) {
            inventory.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    public boolean canPlaceItem(int slotIndex, @NotNull ItemStack stack) {
        if (slotIndex >= 7)
            return false;
        return slotIndex != AGREEMENT_SLOT || stack.getItem() instanceof AgreementItem;
    }

    @Override
    public boolean stillValid(@NotNull Player pPlayer) {
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

        agreement = Agreement.fromItemStack(getAgreementItem()).orElse(Agreement.EMPTY);
        updateBlockStateIfNeeded();
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", this.inventory.serializeNBT());
        tag.putInt("Progress", progress);
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
        else if (getAgreement().isExpired(level.getGameTime()))
            setAgreementItem(AgreementItem.convertToExpired(getAgreementItem()));
    }

    protected void updateBlockStateIfNeeded() {
        AgreementType type = AgreementType.fromItemStack(getAgreementItem());
        BlockState currentBlockState = getBlockState();
        if (level != null && currentBlockState.getValue(DeliveryTableBlock.AGREEMENT) != type)
            level.setBlockAndUpdate(worldPosition, currentBlockState.setValue(DeliveryTableBlock.AGREEMENT, type));
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        super.onDataPacket(net, pkt);
        handleUpdateTag(pkt.getTag());
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
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
