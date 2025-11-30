package com.github.no_name_provided.nnp_rune_smithing.common.blocks;

import com.github.no_name_provided.nnp_rune_smithing.common.entities.RSEntities;
import com.github.no_name_provided.nnp_rune_smithing.common.entities.RuneAnvilBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

import static com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems.RUNE_SMITH_HAMMER;

public class RuneAnvilBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    
    public RuneAnvilBlock(Properties properties) {
        super(properties.noOcclusion().strength(10));
        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }
    
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!player.isCrouching() && level.getBlockEntity(pos) instanceof RuneAnvilBlockEntity be) {
            if (stack.is(RUNE_SMITH_HAMMER)) {
                
                return be.tryCreateResult(stack) ? ItemInteractionResult.SUCCESS : ItemInteractionResult.FAIL;
            }
            if (be.seeImmutableResult().isEmpty()) {
                if (be.seeImmutableBase().isEmpty()) {
                    stack.setCount(be.setBase(stack.copy()).getCount());
                    
                    return ItemInteractionResult.SUCCESS;
                } else if (be.seeImmutableAddition().isEmpty()) {
                    stack.setCount(be.setAddition(stack.copy()).getCount());
                    
                    return ItemInteractionResult.SUCCESS;
                }
            }
        }
        
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }
    
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof RuneAnvilBlockEntity be) {
            if (!be.seeImmutableResult().isEmpty()) {
                ItemStack result = be.extractResult();
                ItemHandlerHelper.giveItemToPlayer(player, result);
                
                return InteractionResult.SUCCESS;
            }
            if (!be.seeImmutableAddition().isEmpty()) {
                ItemStack addition = be.extractAddition();
                ItemHandlerHelper.giveItemToPlayer(player, addition);
                
                return InteractionResult.SUCCESS;
            }
            if (!be.seeImmutableBase().isEmpty()) {
                ItemStack base = be.extractBase();
                ItemHandlerHelper.giveItemToPlayer(player, base);
                
                return InteractionResult.SUCCESS;
            }
        }
        
        return InteractionResult.FAIL;
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
    
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(RuneAnvilBlock::new);
    }
    
    
    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RuneAnvilBlockEntity(pos, state);
    }
    
    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(blockEntityType, RSEntities.RUNE_ANVIL.get(), RuneAnvilBlockEntity::serverTick);
    }
    
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
