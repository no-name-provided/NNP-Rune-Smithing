package com.github.no_name_provided.nnp_rune_smithing.common.blocks;

import com.github.no_name_provided.nnp_rune_smithing.common.entities.RSEntities;
import com.github.no_name_provided.nnp_rune_smithing.common.entities.RuneBlockEntity;
import com.github.no_name_provided.nnp_rune_smithing.common.items.runes.AbstractRuneItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import static com.github.no_name_provided.nnp_rune_smithing.common.entities.RuneBlockEntity.*;
import static com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems.*;

public class RuneBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    
    int tickRate = 20;
    
    public RuneBlock(Properties properties) {
        super(properties.noOcclusion().noCollission().noTerrainParticles());
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.DOWN));
    }
    
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level instanceof ServerLevel sLevel) {
            // Probably a more efficient way to update this
            tickRate = (int) level.tickRateManager().tickrate();
            if (stack.getItem() instanceof AbstractRuneItem item && sLevel.getBlockEntity(pos) instanceof RuneBlockEntity runes && runes.getItem(item.getType().ordinal()).isEmpty()) {
                runes.setItem(item.getType().ordinal(), stack.copyWithCount(1));
                stack.shrink(1);
                
                return ItemInteractionResult.SUCCESS;
            }
        }
        
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
    
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level instanceof ServerLevel sLevel) {
            if (player.isCrouching() && sLevel.getBlockEntity(pos) instanceof RuneBlockEntity be) {
                be.dropInventory();
                
                return InteractionResult.SUCCESS;
            }
        }
        
        return InteractionResult.PASS;
    }
    
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.getGameTime() % 2 == 0 && level.getBlockEntity(pos) instanceof RuneBlockEntity runes) {
            if (runes.getItem(TARGET).is(SELF_RUNE)) {
                if (entity instanceof Monster monster && runes.getItem(MODIFIER).is(WARD_RUNE.get())) {
                    monster.setDeltaMovement(monster.getDeltaMovement().scale(-10));
                    monster.moveRelative(2, monster.getDeltaMovement());
                }
            } else if (runes.getItem(TARGET).is(COLLISION_RUNE) && entity instanceof LivingEntity lifeForm ) {
                if (runes.getItem(EFFECT).is(WARD_RUNE) && lifeForm.isAffectedByPotions()) {
                    MobEffectInstance effect = lifeForm.getEffect(MobEffects.ABSORPTION);
                    int duration = tickRate * 60;
                    if (null == effect || effect.getDuration() < duration && effect.getAmplifier() < runes.getTier()) {
                        lifeForm.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, duration, runes.getTier()));
                    }
                } else if (runes.getItem(EFFECT).is(EARTH_RUNE) && lifeForm instanceof ServerPlayer player) {
                    FoodData foodData = player.getFoodData();
                    if (foodData.needsFood()) {
                        foodData.eat(1, 0.5f * runes.getTier());
                    }
                } else if (runes.getItem(EFFECT).is(WATER_RUNE) && lifeForm.isAffectedByPotions()) {
                    MobEffectInstance effect = lifeForm.getEffect(MobEffects.WATER_BREATHING);
                    int duration = tickRate * 60;
                    if (null == effect || effect.getDuration() < duration) {
                        lifeForm.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, duration));
                    }
                } else if (runes.getItem(EFFECT).is(AIR_RUNE) && lifeForm.isAffectedByPotions()) {
                    MobEffectInstance effect = lifeForm.getEffect(MobEffects.MOVEMENT_SPEED);
                    int duration = tickRate * 60;
                    if (null == effect || effect.getDuration() < duration && effect.getAmplifier() < runes.getTier()) {
                        lifeForm.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, runes.getTier()));
                    }
                } else if (runes.getItem(EFFECT).is(FIRE_RUNE) && lifeForm.isAffectedByPotions()) {
                    MobEffectInstance effect = lifeForm.getEffect(MobEffects.DAMAGE_BOOST);
                    int duration = tickRate * 20;
                    if (null == effect || effect.getDuration() < duration && effect.getAmplifier() < runes.getTier() - 1) {
                        lifeForm.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, duration, runes.getTier() - 1));
                    }
                }
            }
        }
    }
    
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch(state.getValue(BlockStateProperties.FACING)) {
            case DOWN -> Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
            case UP -> Block.box(0.0, 15.0, 0.0, 16.0, 16.0, 16.0);
            case NORTH -> Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 1.0);
            case SOUTH -> Block.box(0.0, 0.0, 15.0, 16.0, 16.0, 16.0);
            case WEST -> Block.box(0.0, 0.0, 0.0, 1.0, 16.0, 16.0);
            case EAST -> Block.box(15.0, 0.0, 0.0, 16.0, 16.0, 16.0);
        };
    }
    
    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, RSEntities.RUNE_BLOCK_ENTITY.get(), RuneBlockEntity::serverTick);
    }
    
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(RuneBlock::new);
    }
    
    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RuneBlockEntity(pos, state);
    }
    
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
//        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite().getOpposite());
        return this.defaultBlockState().setValue(FACING, context.getClickedFace().getOpposite());
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.FACING);
    }
}
