package com.github.no_name_provided.nnp_rune_smithing.common.blocks;

import com.github.no_name_provided.nnp_rune_smithing.common.entities.CastingTableBlockEntity;
import com.github.no_name_provided.nnp_rune_smithing.common.entities.RSEntities;
import com.github.no_name_provided.nnp_rune_smithing.common.items.interfaces.CastingMold;
import com.github.no_name_provided.nnp_rune_smithing.common.recipes.RSRecipes;
import com.github.no_name_provided.nnp_rune_smithing.common.recipes.inputs.MoldingInput;
import com.github.no_name_provided.nnp_rune_smithing.common.recipes.MoldingRecipe;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class CastingTableBlock extends BaseEntityBlock {
    protected CastingTableBlock(Properties properties) {
        super(properties.noOcclusion());
    }
    
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        // For some reason, this was running again with the off-hand even though the pipeline should have terminated
        if (!level.isClientSide && hand == InteractionHand.MAIN_HAND && level.getBlockEntity(pos) instanceof CastingTableBlockEntity be) {
            if (be.getItem(0).isEmpty() && stack.getItem() instanceof CastingMold) {
                be.setItem(0, stack.copyWithCount(1));
                stack.shrink(1);
                
                return ItemInteractionResult.SUCCESS;
            } else if (!stack.isEmpty()) {
                // Will cause (quite) error if input is empty
                ItemStack material = be.getItem(0).copy();
                ItemStack template = stack.copy();
                Optional<RecipeHolder<MoldingRecipe>> recipe = level.getRecipeManager().getRecipeFor(RSRecipes.MOLDING.get(), new MoldingInput(Ingredient.of(template), Ingredient.of(material)), level);
                if (recipe.isPresent()) {
                    ItemStack result = recipe.get().value().result();
                    be.setItem(0, result);
                    
                    return ItemInteractionResult.SUCCESS;
                }
            }
        }
        
        return stack.isEmpty() ? ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION : ItemInteractionResult.FAIL;
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
