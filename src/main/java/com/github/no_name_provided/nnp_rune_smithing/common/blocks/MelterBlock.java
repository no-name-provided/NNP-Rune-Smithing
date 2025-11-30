package com.github.no_name_provided.nnp_rune_smithing.common.blocks;

import com.github.no_name_provided.nnp_rune_smithing.common.entities.MelterBlockEntity;
import com.github.no_name_provided.nnp_rune_smithing.common.entities.RSEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.Nullable;

public class MelterBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    
    protected MelterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }
    
    @Override
    protected MapCodec<MelterBlock> codec() {
        return simpleCodec(MelterBlock::new);
    }
    
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            this.openMenu(level, pos, player);
            return InteractionResult.CONSUME;
        }
    }
    
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        // Note to self: the undocumented FluidUtil class has a convenience method for this
        if (!level.isClientSide) {
            IFluidHandler entityCap = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, Direction.NORTH);
            IFluidHandlerItem stackCap = stack.getCapability(Capabilities.FluidHandler.ITEM);
            if (null != entityCap && null != stackCap) {
                // Try to drain
                FluidStack available = entityCap.getFluidInTank(0);
                FluidStack toTransfer = entityCap.drain(available.copy(), IFluidHandler.FluidAction.SIMULATE);
                int canTransfer = stackCap.fill(toTransfer, IFluidHandler.FluidAction.SIMULATE);
                toTransfer = entityCap.drain(canTransfer, IFluidHandler.FluidAction.EXECUTE);
                stackCap.fill(toTransfer, IFluidHandler.FluidAction.EXECUTE);
                if (toTransfer.isEmpty()) {
                    // Try to fill
                    available = stackCap.getFluidInTank(0);
                    toTransfer = stackCap.drain(available.copy(), IFluidHandler.FluidAction.SIMULATE);
                    canTransfer = entityCap.fill(toTransfer, IFluidHandler.FluidAction.SIMULATE);
                    toTransfer = stackCap.drain(canTransfer, IFluidHandler.FluidAction.EXECUTE);
                    entityCap.fill(toTransfer, IFluidHandler.FluidAction.EXECUTE);
                }
                // For some reason, the stack cap only has a copy of the itemstack, not a reference to it. This causes a desync that must be manually fixed
                player.setItemInHand(hand, stackCap.getContainer());
                
                return ItemInteractionResult.SUCCESS;
            }
        }
        
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }
    
    protected void openMenu(Level level, BlockPos pos, Player player) {
        BlockEntity blockentity = level.getBlockEntity(pos);
        if (blockentity instanceof MelterBlockEntity) {
            player.openMenu((MenuProvider) blockentity, buf -> buf.writeBlockPos(pos));
        }
    }
    
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(LIT)) {
            double d0 = (double) pos.getX() + 0.5;
            double d1 = (double) pos.getY() + 0.7;
            double d2 = (double) pos.getZ() + 0.5;
            if (random.nextDouble() < 0.1) {
                level.playLocalSound(d0, d1, d2, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
            }
            
            Direction direction = state.getValue(FACING);
            Direction.Axis directionAxis = direction.getAxis();
            double d4 = random.nextDouble() * 0.6 - 0.3;
            double d5 = directionAxis == Direction.Axis.X ? (double) direction.getStepX() * 0.52 : d4;
            double d6 = random.nextDouble() * 6.0 / 16.0;
            double d7 = directionAxis == Direction.Axis.Z ? (double) direction.getStepZ() * 0.52 : d4;
            level.addParticle(ParticleTypes.SMOKE, d0 + d5, d1 + d6, d2 + d7, 0.0, 0.0, 0.0);
            level.addParticle(ParticleTypes.FLAME, d0 + d5, d1 + d6, d2 + d7, 0.0, 0.0, 0.0);
        }
    }
    
    @Nullable @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createMelterTicker(level, blockEntityType, RSEntities.MELTER_BLOCK_ENTITY.get());
    }
    
    @javax.annotation.Nullable
    protected static <T extends BlockEntity> BlockEntityTicker<T> createMelterTicker(
            Level level,
            BlockEntityType<T> serverType,
            BlockEntityType<? extends MelterBlockEntity> clientType
    ) {
        return level.isClientSide ? null : createTickerHelper(serverType, clientType, MelterBlockEntity::serverTick);
    }
    
    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MelterBlockEntity(pos, state);
    }
    
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
    
}
