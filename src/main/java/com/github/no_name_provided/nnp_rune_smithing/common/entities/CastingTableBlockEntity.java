package com.github.no_name_provided.nnp_rune_smithing.common.entities;

import com.github.no_name_provided.nnp_rune_smithing.common.items.IngotMold;
import com.github.no_name_provided.nnp_rune_smithing.common.items.interfaces.CastingMold;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.security.InvalidParameterException;

import static com.github.no_name_provided.nnp_rune_smithing.common.entities.RSEntities.CASTING_TABLE_BLOCK_ENTITY;
import static com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems.INGOT_MOLD_REUSABLE;

public class CastingTableBlockEntity extends BaseContainerBlockEntity {
    public static final int MAX_COOLING_TIME = 2000;
    public NonNullList<ItemStack> inventory = NonNullList.withSize(2, ItemStack.EMPTY);
    public int coolingTime = 0;
    public int coolingTotalTime;
    public FluidStack tank = FluidStack.EMPTY;
    
    public CastingTableBlockEntity(BlockPos pos, BlockState blockState) {
        super(CASTING_TABLE_BLOCK_ENTITY.get(), pos, blockState);
    }
    
    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.casting_table");
    }
    
    @Override
    protected NonNullList<ItemStack> getItems() {
        return inventory;
    }
    
    
    
    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        if (items.size() != getContainerSize()) {
            throw new InvalidParameterException("Casting Table inventory must have room for exactly " + getContainerSize() + " stacks.");
        }
        inventory = items;
        setChanged();
    }
    
    /**
     * Mandatory override. No menu provided. Do not call this.
     */
    @SuppressWarnings("DataFlowIssue") @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return null;
    }
    
    @Override
    public int getContainerSize() {
        
        // Real size is 2. This is a quick hack to force vanilla to use capability, not interface
        return 0;
    }
    
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, inventory, registries);
        coolingTime = tag.getInt("coolingTime");
        coolingTotalTime = tag.getInt("coolingTotalTime");
        tank = new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse(tag.getString("fluidStack"))), tag.getInt("fluidAmount"));
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        addClientData(tag, registries);
        tag.putInt("fluidAmount", tank.getAmount());
    }
    
    CompoundTag addClientData(CompoundTag tag, HolderLookup.Provider registries) {
        ContainerHelper.saveAllItems(tag, inventory, registries);
        tag.putString("fluidStack", tank.getFluidHolder().getRegisteredName());
        tag.putInt("coolingTime", coolingTime);
        tag.putInt("coolingTotalTime", coolingTotalTime);
        // Inefficient (?) workaround for a bug in ContainerHelper#saveAllItems where empty slots aren't synced
        tag.putBoolean("clearSlot0", getItem(0).isEmpty());
        tag.putBoolean("clearSlot1", getItem(1).isEmpty());
        
        return tag;
    }
    
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        
        return addClientData(tag, registries);
    }
    
    /**
     * Called when the chunk's TE update tag, gotten from {@link BlockEntity#getUpdateTag(HolderLookup.Provider)}, is received on the client.
     * <p>
     * Used to handle this tag in a special way. By default, this simply calls {@link BlockEntity#loadWithComponents(CompoundTag, HolderLookup.Provider)}.
     *
     * @param tag            The {@link CompoundTag} sent from {@link BlockEntity#getUpdateTag(HolderLookup.Provider)}
     */
    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        ContainerHelper.loadAllItems(tag, inventory, registries);
        coolingTime = tag.getInt("coolingTime");
        coolingTotalTime = tag.getInt("coolingTotalTime");
        tank = new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse(tag.getString("fluidStack"))), 1000);
        if (tag.getBoolean("clearSlot0")) {
            setItem(0, ItemStack.EMPTY);
        }
        if (tag.getBoolean("clearSlot1")) {
            setItem(1, ItemStack.EMPTY);
        }
    }
    
    public static void serverTick(Level level, BlockPos pos, BlockState state, CastingTableBlockEntity table) {
        // Handle craft
        if (table.coolingTime > 0) {
            if (--table.coolingTime == 0) {
                CastingMold mold = table.getMold();
                table.setItem(1, mold.getResult(table.tank));
                table.tank = FluidStack.EMPTY;
                if (mold.consumed()) {
                    table.setItem(0, ItemStack.EMPTY);
                }
                level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
            }
        }
    }
    
    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null) {
            // Force a block update
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL_IMMEDIATE);
        }
    }
    
    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        
        return ClientboundBlockEntityDataPacket.create(this);
    }
    
    /**
     * Called when you receive a TileEntityData packet for the location this
     * TileEntity is currently in. On the client, the NetworkManager will always
     * be the remote server. On the server, it will be whoever is responsible for
     * sending the packet.
     *
     * @param net            The NetworkManager the packet originated from
     * @param pkt            The data packet
     */
    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
        CompoundTag tag = pkt.getTag();
        if (!tag.isEmpty()) {
            handleUpdateTag(tag, registries);
        }
    }
    
    /**
     * Fluid required by mold.
     * @return The amount of fluid consumed per cast.
     */
    public int getFluidCost() {
        if (getItem(0).getItem() instanceof CastingMold mold) {
            
            return mold.amountRequired();
        } else {
            
            return 0;
        }
    }
    
    /**
     * @param toAdd Max amount of fluid to add.
     * @return The amount of fluid that will actually be accepted (and used).
     */
    public int canAddFluid(FluidStack toAdd) {
        
        return getItem(0).getItem() instanceof CastingMold mold && mold.amountRequired() <= toAdd.getAmount() && getItem(1).isEmpty() && coolingTime == 0 && mold.validateFluid(toAdd) ?
                mold.amountRequired() : 0;
    }
    
    /**
     * Kick off a craft. Must not be called while a craft is ongoing, or the inventory slots aren't set up.
     * Use #canAddFluid first, to validate.
     *
     * @param toAdd Max amount of fluid to add.
     */
    public void startRecipe(FluidStack toAdd) {
        tank = new FluidStack(toAdd.getFluid(), getFluidCost());
        // Takes between 1 and 100 seconds, depending on initial temperature
        coolingTime = Mth.clamp(20 * (toAdd.getFluidType().getTemperature() - 50) / 50, 20, MAX_COOLING_TIME);
        coolingTotalTime = coolingTime;
        setChanged();
    }
    
    /**
     * Only call on server. Safely casts first item to a mold, so I can use all its methods and fields.
     * <p>Level must not be null.</p>
     * @return Mold version of item in mold slot.
     */
    private CastingMold getMold() {
        if (getItem(0).getItem() instanceof CastingMold mold) {
            
            return mold;
        } else {
            assert level != null;
            level.addFreshEntity(new ItemEntity(level, getBlockPos().getX(), getBlockPos().getY() + 1, getBlockPos().getZ(), getItem(0)));
            setItem(0, INGOT_MOLD_REUSABLE.get().getDefaultInstance());
            LogUtils.getLogger().warn("The first item in the Casting Table inventory must be a valid CastingMold when #getMold is called. Existing item dropped in world and replaced with INGOT_MOLD, to prevent crash.");
            
            return (IngotMold) getItem(0).getItem();
        }
    }
    
    /**
     * Client only. Unused params required by createTickerHelper, and I'm not making a lambda just to avoid 'em.
     */
    public static void clientTick(Level ignoredLevel, BlockPos ignoredPos, BlockState ignoredState, CastingTableBlockEntity table) {
        if (table.coolingTime > 0) {
            table.coolingTime--;
        }
    }
}
