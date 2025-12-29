package com.github.no_name_provided.nnp_rune_smithing.common.entities;

import com.github.no_name_provided.nnp_rune_smithing.common.blocks.RSBlocks;
import com.github.no_name_provided.nnp_rune_smithing.common.capabilities.MelterCapability;
import com.github.no_name_provided.nnp_rune_smithing.common.gui.menus.MelterMenu;
import com.github.no_name_provided.nnp_rune_smithing.common.recipes.MeltRecipe;
import com.github.no_name_provided.nnp_rune_smithing.common.recipes.RSRecipes;
import com.github.no_name_provided.nnp_rune_smithing.common.recipes.inputs.MeltInput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

import static com.github.no_name_provided.nnp_rune_smithing.common.entities.RSEntities.MELTER_BLOCK_ENTITY;
import static com.github.no_name_provided.nnp_rune_smithing.common.gui.menus.MelterMenu.DATA_COUNT;

public class MelterBlockEntity extends BaseContainerBlockEntity {
    int litTime = 0;
    int litDuration = 0;
    int meltingProgress = 0;
    int meltingTotalTime = 0;
    int fluidID = 0;
    int fluidAmount = 0;
    int fluidTint = 0;
    int CONTAINER_SIZE = 2;
    public static final int DEFAULT_TICKS_PER_ITEM = 200;
    public FluidStack output = new FluidStack(Fluids.EMPTY, 0);
    private static volatile Map<Item, Integer> fuelCache;
    private NonNullList<ItemStack> INVENTORY = NonNullList.withSize(2, ItemStack.EMPTY);
    protected final MelterContainerData dataAccess = new MelterContainerData(DATA_COUNT, this);
    
    public MelterBlockEntity(BlockPos pos, BlockState state) {
        super(MELTER_BLOCK_ENTITY.get(), pos, state);
    }
    
    @Override
    protected Component getDefaultName() {
        
        return Component.translatable("container.melter");
    }
    
    @Override
    protected NonNullList<ItemStack> getItems() {
        
        return INVENTORY;
    }
    
    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        INVENTORY = items;
        setChanged();
    }
    
    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new MelterMenu(containerId, inventory, worldPosition, dataAccess, this);
    }
    
    @Override
    public int getContainerSize() {
        
        return CONTAINER_SIZE;
    }
    
    public boolean isLit() {
        
        return this.litTime > 0;
    }
    
    private boolean canBurn() {
        
        return INVENTORY.getLast().is(ItemTags.COALS);
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, INVENTORY, registries);
        saveClient(tag, registries);
        tag.putInt("meltingProgress", meltingProgress);
        tag.putInt("meltingTotalTime", meltingTotalTime);
        tag.putInt("litTime", litTime);
        tag.putInt("litDuration", litDuration);
    }
    
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, INVENTORY, registries);
        setOutput(FluidStack.OPTIONAL_CODEC.decode(NbtOps.INSTANCE, tag.get("output")).getOrThrow().getFirst());
        meltingProgress = tag.getInt("meltingProgress");
        meltingTotalTime = tag.getInt("meltingTotalTime");
        litTime = tag.getInt("litTime");
        litDuration = tag.getInt("litDuration");
    }
    
    private void saveClient(CompoundTag tag, HolderLookup.Provider ignoredRegistries) {
        tag.put("output", FluidStack.OPTIONAL_CODEC.encodeStart(NbtOps.INSTANCE, output).getOrThrow());
    }
    
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveClient(tag, registries);
        return tag;
    }
    
    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        setOutput(FluidStack.OPTIONAL_CODEC.decode(NbtOps.INSTANCE, tag.get("output")).getOrThrow().getFirst());
    }
    
    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    
    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
        CompoundTag tag = pkt.getTag();
        if (!tag.isEmpty()) {
            handleUpdateTag(tag, registries);
        }
    }
    
    public Optional<RecipeHolder<MeltRecipe>> getRecipe(Level level) {
//        if (!INVENTORY.get(0).isEmpty()) {
        RecipeManager manager = level.getRecipeManager();
        MeltInput input = new MeltInput(INVENTORY.getFirst().copyWithCount(1), 600);
        
        return manager.getRecipeFor(RSRecipes.MELT.get(), input, level);
//        }
    }
    
    public boolean isValidRecipe(MeltRecipe recipe) {
        
        return output.isEmpty() || FluidStack.isSameFluid(recipe.getRESULT(), output) && recipe.getRESULT().getAmount() + output.getAmount() <= MelterCapability.MelterFluidHandler.MELTER_CAPACITY;
    }
    
    /**
     * Returns true if the recipe is successfully processed. Otherwise, returns false.
     */
    public void processRecipe(MeltRecipe recipe, Level level, BlockPos pos) {
        IFluidHandler cap = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, Direction.NORTH);
        if (null != cap && cap.fill(recipe.getRESULT().copy(), IFluidHandler.FluidAction.SIMULATE) == recipe.getRESULT().getAmount()) {
            cap.fill(recipe.getRESULT().copy(), IFluidHandler.FluidAction.EXECUTE);
            INVENTORY.getFirst().shrink(1);
            setChanged();
        }
    }
    
    public static void serverTick(Level level, BlockPos pos, BlockState state, MelterBlockEntity melter) {
        // Handle crafting
        if (melter.isLit()) {
            if (!state.getValue(BlockStateProperties.LIT)) {
                level.setBlock(pos, state.setValue(BlockStateProperties.LIT, true), Block.UPDATE_ALL);
            }
            Optional<RecipeHolder<MeltRecipe>> holder = melter.getRecipe(level);
            if (holder.isPresent() && melter.isValidRecipe(holder.get().value())) {
                // Should probably rework melting time.
                melter.meltingTotalTime = 20 * holder.get().value().getMELTING_TEMP() / 50;
                if (melter.meltingProgress >= melter.meltingTotalTime) {
                    melter.processRecipe(holder.get().value(), level, pos);
                    melter.meltingProgress = 0;
                } else {
                    melter.meltingProgress++;
                    melter.litTime--;
                }
            }
        }
        // We decrement litTime above, so isLit is unknown
        if (!melter.isLit()) {
            if (melter.canBurn()) {
                melter.litDuration = 8 * DEFAULT_TICKS_PER_ITEM;
                melter.litTime = 8 * DEFAULT_TICKS_PER_ITEM;
                melter.INVENTORY.getLast().shrink(1);
                melter.setChanged();
            } else if (state.getValue(BlockStateProperties.LIT)) {
                level.setBlock(pos, state.setValue(BlockStateProperties.LIT, false), Block.UPDATE_ALL);
            }
        }
        // Transfer fluid when powered by redstone
        if (level.hasNeighborSignal(pos) && level.getGameTime() % 10 == 0) {
            IFluidHandler sourceCap = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, Direction.DOWN);
            if (null != sourceCap) {
                if (level.getBlockState(pos.below()).isAir() && level.getBlockState(pos.below(2)).is(RSBlocks.CASTING_TABLE)) {
                    IFluidHandler destCap = level.getCapability(Capabilities.FluidHandler.BLOCK, pos.below(2), Direction.NORTH);
                    if (null != destCap) {
                        FluidStack transferred = FluidUtil.tryFluidTransfer(
                                destCap,
                                sourceCap,
                                2000,
                                true
                        );
                        if (!transferred.isEmpty()) {
                            ((ServerLevel) level).sendParticles(ParticleTypes.DRIPPING_LAVA, melter.getBlockPos().getX() + 0.5, melter.getBlockPos().getY() - 0.1, melter.getBlockPos().getZ() + 0.5, 10, 0.2, 0.1, 0.2, 0.1);
                        }
                    }
                }
            }
        }
    }
    
    public static class MelterContainerData extends SimpleContainerData {
        
        public ResourceLocation outputTexture;
        private final MelterBlockEntity be;
        private final int SIZE;
        
        public MelterContainerData(int size, MelterBlockEntity entity) {
            super(size);
            be = entity;
            SIZE = size;
        }
        
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> {
                    if (be.litDuration > Short.MAX_VALUE) {
                        // Neo: preserve litTime / litDuration ratio on the client as data slots are synced as shorts.
                        yield Mth.floor(((double) be.litTime / be.litDuration) * Short.MAX_VALUE);
                    }
                    yield be.litTime;
                }
                case 1 -> Math.min(be.litDuration, Short.MAX_VALUE);
                case 2 -> be.meltingProgress;
                case 3 -> be.meltingTotalTime;
                case 4 -> be.fluidAmount;
                case 5 -> be.fluidID;
                case 6 -> be.fluidTint;
                default -> 0;
            };
        }
        
        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0:
                    be.litTime = value;
                    break;
                case 1:
                    be.litDuration = value;
                    break;
                case 2:
                    be.meltingProgress = value;
                    break;
                case 3:
                    be.meltingTotalTime = value;
                    break;
                case 4:
                    be.fluidAmount = value;
                    break;
                case 5:
                    // Menu uses IDs, because its transient and they're probably more efficient
                    be.fluidID = value;
                    Fluid fluid = BuiltInRegistries.FLUID.byId(value);
                    if (!fluid.isSame(Fluids.WATER) && !fluid.isSame(Fluids.LAVA) && !fluid.isSame(Fluids.EMPTY)) {
                        outputTexture = IClientFluidTypeExtensions.of(fluid).getStillTexture();
                    } else if (fluid.isSame(Fluids.WATER)) {
                        outputTexture = ResourceLocation.withDefaultNamespace("block/water_still");
                    } else if (fluid.isSame(Fluids.LAVA)) {
                        outputTexture = ResourceLocation.withDefaultNamespace("block/lava_still");
                    } else {
                        outputTexture = ResourceLocation.withDefaultNamespace("missingno");
                    }
                    break;
                case 6:
                    be.fluidTint = value;
            }
        }
        
        @Override
        public int getCount() {
            return SIZE;
        }
    }
    
    public void setOutput(FluidStack output) {
        this.output = output;
        // Menu uses this, because its transient and sending ints is probably better than strings
        fluidID = BuiltInRegistries.FLUID.getId(output.getFluid());
        fluidAmount = output.getAmount();
        fluidTint = IClientFluidTypeExtensions.of(output.getFluid()).getTintColor(output);
        this.setChanged();
        if (null != level) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }
}
