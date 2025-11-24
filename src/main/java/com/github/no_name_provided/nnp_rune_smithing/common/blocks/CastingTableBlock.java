package com.github.no_name_provided.nnp_rune_smithing.common.blocks;

import com.github.no_name_provided.nnp_rune_smithing.common.entities.CastingTableBlockEntity;
import com.github.no_name_provided.nnp_rune_smithing.common.entities.RSEntities;
import com.github.no_name_provided.nnp_rune_smithing.common.items.interfaces.CastingMold;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class CastingTableBlock extends BaseEntityBlock {
    protected CastingTableBlock(Properties properties) {
        super(properties.noOcclusion());
    }
    
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof CastingTableBlockEntity be && be.getItem(0).isEmpty() && stack.getItem() instanceof CastingMold) {
            be.setItem(0, stack.copyWithCount(1));
            stack.shrink(1);
            
            return ItemInteractionResult.SUCCESS;
        } else {
            
            return stack.isEmpty() ? ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION : ItemInteractionResult.FAIL;
        }
    }
    
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof CastingTableBlockEntity be) {
            if (!be.getItem(1).isEmpty()) {
                level.addFreshEntity(new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), be.getItem(1).copy()));
                be.setItem(1, ItemStack.EMPTY);
            } else if (!be.getItem(0).isEmpty() && be.coolingTime == 0) {
                level.addFreshEntity(new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), be.getItem(0).copy()));
                be.setItem(0, ItemStack.EMPTY);
            }
            
            return InteractionResult.SUCCESS;
        } else {
            
            return super.useWithoutItem(state, level, pos, player, hitResult);
        }
    }
    
    @Override
    protected MapCodec<CastingTableBlock> codec() {
        return simpleCodec(CastingTableBlock::new);
    }
    
    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CastingTableBlockEntity(pos, state);
    }
    
    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) {
            return null;
        } else {
            return createTickerHelper(blockEntityType, RSEntities.CASTING_TABLE_BLOCK_ENTITY.get(), CastingTableBlockEntity::serverTick);
        }
    }
    
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
