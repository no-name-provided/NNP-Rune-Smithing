package com.github.no_name_provided.nnp_rune_smithing.common.entities;

import com.github.no_name_provided.nnp_rune_smithing.common.blocks.RSBlocks;
import com.github.no_name_provided.nnp_rune_smithing.common.recipes.AlloyRecipe;
import com.github.no_name_provided.nnp_rune_smithing.common.recipes.RSRecipes;
import com.github.no_name_provided.nnp_rune_smithing.common.recipes.inputs.AlloyInput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static com.github.no_name_provided.nnp_rune_smithing.common.entities.RSEntities.ALLOYER_BLOCK_ENTITY;

public class AlloyerBlockEntity extends BlockEntity {
    public static final int INPUT_0 = 0;
    public static final int INPUT_1 = 1;
    public static final int RESULT = 2;
    public static final int TANK_CAPACITY = 5000;
    
    FluidStack inTank0 = FluidStack.EMPTY;
    FluidStack inTank1 = FluidStack.EMPTY;
    FluidStack resultTank = FluidStack.EMPTY;
    
    public AlloyerBlockEntity(BlockPos pos, BlockState state) {
        super(ALLOYER_BLOCK_ENTITY.get(), pos, state);
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        saveClient(tag, registries);
    }
    
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inTank0 = FluidStack.OPTIONAL_CODEC.decode(NbtOps.INSTANCE, tag.get("tank1")).getOrThrow().getFirst();
        inTank1 = FluidStack.OPTIONAL_CODEC.decode(NbtOps.INSTANCE, tag.get("tank2")).getOrThrow().getFirst();
        resultTank = FluidStack.OPTIONAL_CODEC.decode(NbtOps.INSTANCE, tag.get("resultTank")).getOrThrow().getFirst();
        
    }
    
    private void saveClient(CompoundTag tag, HolderLookup.Provider ignoredRegistries) {
        tag.put("tank1", FluidStack.OPTIONAL_CODEC.encodeStart(NbtOps.INSTANCE, inTank0).getOrThrow());
        tag.put("tank2", FluidStack.OPTIONAL_CODEC.encodeStart(NbtOps.INSTANCE, inTank1).getOrThrow());
        tag.put("resultTank", FluidStack.OPTIONAL_CODEC.encodeStart(NbtOps.INSTANCE, resultTank).getOrThrow());
    }
    
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveClient(tag, registries);
        return tag;
    }
    
    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        loadAdditional(tag, lookupProvider);
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
    
    public Optional<RecipeHolder<AlloyRecipe>> getRecipe(Level level) {
        RecipeManager manager = level.getRecipeManager();
        AlloyInput input = new AlloyInput(inTank0.copy(), inTank1.copy());
        
        return manager.getRecipeFor(RSRecipes.ALLOY.get(), input, level);
    }
    
    /**
     * Returns true if the recipe is successfully processed. Otherwise, returns false.
     */
    public void processRecipe(AlloyRecipe recipe, Level level, BlockPos pos) {
        IFluidHandler cap = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, Direction.NORTH);
        if (null != cap && cap.fill(recipe.getResult().copy(), IFluidHandler.FluidAction.SIMULATE) == recipe.getResult().getAmount()) {
            cap.fill(recipe.getResult().copy(), IFluidHandler.FluidAction.EXECUTE);
            setChanged();
        }
    }
    
    public static void serverTick(Level level, BlockPos pos, BlockState state, AlloyerBlockEntity melter) {
        // Handle crafting
        
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
    
    @Override
    public void setChanged() {
        super.setChanged();
        if (null != level) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }
    
    /**
     * Returns a copy
     */
    public FluidStack getFluidInTank(int i) {
        return switch(i) {
            case INPUT_0 -> inTank0.copy();
            case INPUT_1 -> inTank1.copy();
            case RESULT -> resultTank.copy();
            default -> throw new IllegalStateException("Tank " + i + " does not exist!");
        };
    }
    
    public void setInTank0(FluidStack inTank0) {
        this.inTank0 = inTank0;
    }
    
    public void setInTank1(FluidStack inTank1) {
        this.inTank1 = inTank1;
    }
    
    public void setResultTank(FluidStack resultTank) {
        this.resultTank = resultTank;
    }
    
    public void setTank(int tank, FluidStack fluidStack) {
        switch(tank) {
            case INPUT_0 -> setInTank0(fluidStack);
            case INPUT_1 -> setInTank1(fluidStack);
            case RESULT -> setResultTank(fluidStack);
            default -> throw new IllegalStateException("Tank " + tank + " does not exist!");
        }
    }
}
