package com.github.no_name_provided.nnp_rune_smithing.common.blocks;

import com.github.no_name_provided.nnp_rune_smithing.common.entities.AlloyerBlockEntity;
import com.github.no_name_provided.nnp_rune_smithing.common.entities.RSEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.Nullable;

public class AlloyerBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    
    protected AlloyerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
    
    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }
    
    @Override
    protected MapCodec<AlloyerBlock> codec() {
        return simpleCodec(AlloyerBlock::new);
    }
    
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            IFluidHandler entityCap = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, hitResult.getDirection());
            IFluidHandlerItem stackCap = stack.getCapability(Capabilities.FluidHandler.ITEM);
            if (null != entityCap && null != stackCap) {
                FluidActionResult result = FluidUtil.tryFillContainerAndStow(
                        stack,
                        entityCap,
                        null,
                        Integer.MAX_VALUE,
                        player,
                        true
                );
                if (result.success) {
                    player.setItemInHand(hand, result.getResult());
                } else {
                    FluidActionResult newResult = FluidUtil.tryEmptyContainerAndStow(
                            stack,
                            entityCap,
                            null,
                            Integer.MAX_VALUE,
                            player,
                            true
                    );
                    if (newResult.success) {
                        player.setItemInHand(hand, newResult.getResult());
                    }
                }
                
                return ItemInteractionResult.SUCCESS;
            }
        }
        
        return ItemInteractionResult.CONSUME;
    }
    
    @Nullable @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createAlloyerTicker(level, blockEntityType, RSEntities.ALLOYER_BLOCK_ENTITY.get());
    }
    
    @javax.annotation.Nullable
    protected static <T extends BlockEntity> BlockEntityTicker<T> createAlloyerTicker(
            Level level,
            BlockEntityType<T> serverType,
            BlockEntityType<? extends AlloyerBlockEntity> clientType
    ) {
        return level.isClientSide ? null : createTickerHelper(serverType, clientType, AlloyerBlockEntity::serverTick);
    }
    
    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AlloyerBlockEntity(pos, state);
    }
    
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
    
}
