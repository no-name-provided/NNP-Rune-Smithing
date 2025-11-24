package com.github.no_name_provided.nnp_rune_smithing.common.fluids;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.Supplier;

import static com.github.no_name_provided.nnp_rune_smithing.common.fluids.FluidHelper.FLUID_SETS;

public abstract class MoltenMetalFluid extends FlowingFluid {

    Supplier<? extends MoltenMetalFluidType> TYPE;
    MoltenMetalFluid FLOWING;
    MoltenMetalFluid SOURCE;
    BucketItem BUCKET;
    LiquidBlock BLOCK;
    
    MoltenMetalFluid(Supplier<? extends MoltenMetalFluidType> type) {
        TYPE = type;
    }

    @Override
    public FluidType getFluidType() {
        return TYPE.get();
    }

    @Override
    public Fluid getFlowing() {
        if (null == FLOWING) {
            //noinspection OptionalGetWithoutIsPresent // is programmatically added, and shouldn't ever be removed
            FLOWING = FLUID_SETS.stream().filter((set) -> set.type() == this.TYPE).findFirst().get().flowing().get();
        }
        return FLOWING;
    }

    @Override
    public Fluid getSource() {
        if (null == SOURCE) {
            //noinspection OptionalGetWithoutIsPresent // is programmatically added, and shouldn't ever be removed
            SOURCE = FLUID_SETS.stream().filter((set) -> set.type() == this.TYPE).findFirst().get().source().get();
        }
        return SOURCE;
    }

    @Override
    protected void beforeDestroyingBlock(LevelAccessor worldIn, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = state.hasBlockEntity() ? worldIn.getBlockEntity(pos) : null;
        Block.dropResources(state, worldIn, pos, blockEntity);
    }

    @Override
    public boolean isSame(Fluid fluidIn) {
        if (null == SOURCE) {
            //noinspection OptionalGetWithoutIsPresent // is programmatically added, and shouldn't ever be removed
            SOURCE = FLUID_SETS.stream().filter((set) -> set.type() == this.TYPE).findFirst().get().source().get();
        }
        if (null == FLOWING) {
            //noinspection OptionalGetWithoutIsPresent // is programmatically added, and shouldn't ever be removed
            FLOWING = FLUID_SETS.stream().filter((set) -> set.type() == this.TYPE).findFirst().get().flowing().get();
        }
        return fluidIn == SOURCE || fluidIn == FLOWING;
    }

    @Override
    protected BlockState createLegacyBlock(FluidState state) {
        if (BLOCK == null) {
            //noinspection OptionalGetWithoutIsPresent // is programmatically added, and should never be removed
            BLOCK = FLUID_SETS.stream().filter((set) -> set.type() == this.TYPE).findFirst().get().block().get();
        }
        return BLOCK.defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
    }

    @Override
    protected boolean canConvertToSource(Level level) {
        return false;
    }

    /**
     * Will check this distance plus 1. Looks wierd if the fluid level falls to 0 before it reaches the
     * fluid it detects.
     */
    @Override
    protected int getSlopeFindDistance(LevelReader level) {
        return 2;
    }

    @Override
    protected int getDropOff(LevelReader level) {
        return 2;
    }

    @Override
    public Item getBucket() {
        if (null == BUCKET) {
            //noinspection OptionalGetWithoutIsPresent // is programmatically added, and shouldn't ever be removed
            BUCKET = FLUID_SETS.stream().filter((set) -> set.type().get() == TYPE.get()).findFirst().get().bucket().get();
        }
        return BUCKET;
    }

    @Override
    protected boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluid, Direction direction) {
        return false;
    }

    @Override
    public int getTickDelay(LevelReader level) {
        return 12;
    }

    @Override
    protected float getExplosionResistance() {
        return 1000.0f;
    }
    
    public static class Flowing extends MoltenMetalFluid {
        public Flowing(DeferredHolder<FluidType, MoltenMetalFluidType> type) {
            super(type);
            registerDefaultState(getStateDefinition().any().setValue(LEVEL, 7));
        }

        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }

        public boolean isSource(FluidState state) {
            return false;
        }
    }

    public static class Source extends MoltenMetalFluid {
        public Source(DeferredHolder<FluidType, MoltenMetalFluidType> type) {
            super(type);
        }

        public int getAmount(FluidState state) {
            return 8;
        }

        public boolean isSource(FluidState state) {
            return true;
        }
    }
}
