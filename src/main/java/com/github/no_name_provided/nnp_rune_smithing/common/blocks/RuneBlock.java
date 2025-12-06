package com.github.no_name_provided.nnp_rune_smithing.common.blocks;

import com.github.no_name_provided.nnp_rune_smithing.client.particles.RSParticleTypes;
import com.github.no_name_provided.nnp_rune_smithing.common.entities.RuneBlockEntity;
import com.github.no_name_provided.nnp_rune_smithing.common.items.runes.AbstractRuneItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static com.github.no_name_provided.nnp_rune_smithing.common.entities.RSEntities.RUNE_BLOCK_ENTITY;
import static com.github.no_name_provided.nnp_rune_smithing.common.entities.RuneBlockEntity.EFFECT;
import static com.github.no_name_provided.nnp_rune_smithing.common.entities.RuneBlockEntity.TARGET;
import static com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems.*;
import static net.minecraft.core.Direction.NORTH;

public class RuneBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    
    int tickRate = 20;
    
    public static HashMap<AbstractRuneItem, List<Integer>> effectToColor = HashMap.newHashMap(4);
    
    public RuneBlock(Properties properties) {
        super(properties.noOcclusion().noCollission().noTerrainParticles().strength(1000));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.DOWN));
    }
    
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level instanceof ServerLevel sLevel) {
            // There's probably a more efficient way to update this
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
                if (entity instanceof Monster monster && monster.mainSupportingBlockPos.isPresent() &&monster.mainSupportingBlockPos.get().above().equals(pos) && runes.getItem(EFFECT).is(WARD_RUNE.get())) {
                    Vec3 oldMovement = monster.getDeltaMovement();
                    Direction axisDirection = Direction.getNearest(oldMovement);
                    Optional<RuneBlockEntity> otherBE = level.getBlockEntity(pos.relative(axisDirection), RUNE_BLOCK_ENTITY.get());
                    // Catch mobs that angle in and enter between two ward runes, such that they would bounce
                    if (otherBE.isPresent() && otherBE.get().getItem(TARGET).is(SELF_RUNE) && otherBE.get().getItem(EFFECT).is(WARD_RUNE)) {
                        axisDirection = getSecondNearest(oldMovement);
                        // Mostly works, but randomly fails?
//                        axisDirection = vectorFromDirection(axisDirection).dot(oldMovement) > 0 ? axisDirection.getClockWise() : axisDirection.getCounterClockWise();
                    }
                    
                    Vec3 newMovement = vectorFromDirection(axisDirection.getOpposite()).scale(10 * oldMovement.length());
                    monster.setDeltaMovement(newMovement);
                    // For client synchronization, may be unnecessary
                    monster.hasImpulse = true;
                    monster.moveRelative(2, monster.getDeltaMovement());
                }
            } else if (runes.getItem(TARGET).is(COLLISION_RUNE) && entity instanceof LivingEntity lifeForm ) {
                if (runes.getItem(EFFECT).is(WARD_RUNE) && lifeForm.isAffectedByPotions()) {
                    MobEffectInstance effect = lifeForm.getEffect(MobEffects.ABSORPTION);
                    int duration = tickRate * 60;
                    if (null == effect || effect.getDuration() < duration / 2 && effect.getAmplifier() <= runes.getTier()) {
                        lifeForm.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, duration, runes.getTier()));
                    }
                } else if (runes.getItem(EFFECT).is(EARTH_RUNE) && lifeForm instanceof ServerPlayer player) {
                    FoodData foodData = player.getFoodData();
                    if (foodData.needsFood()) {
                        foodData.eat(1, 0.5f * runes.getTier());
                    }
                } else if (runes.getItem(EFFECT).is(WATER_RUNE) && lifeForm.isAffectedByPotions()) {
                    MobEffectInstance effect = lifeForm.getEffect(MobEffects.WATER_BREATHING);
                    int duration = tickRate * 60;
                    if (null == effect || effect.getDuration() < duration / 2) {
                        lifeForm.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, duration));
                    }
                } else if (runes.getItem(EFFECT).is(AIR_RUNE) && lifeForm.isAffectedByPotions()) {
                    MobEffectInstance effect = lifeForm.getEffect(MobEffects.MOVEMENT_SPEED);
                    int duration = tickRate * 60;
                    if (null == effect || effect.getDuration() < duration / 2 && effect.getAmplifier() < runes.getTier()) {
                        lifeForm.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, runes.getTier()));
                    }
                } else if (runes.getItem(EFFECT).is(FIRE_RUNE) && lifeForm.isAffectedByPotions()) {
                    MobEffectInstance effect = lifeForm.getEffect(MobEffects.DAMAGE_BOOST);
                    int duration = tickRate * 20;
                    if (null == effect || effect.getDuration() < duration / 2 && effect.getAmplifier() < runes.getTier() - 1) {
                        lifeForm.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, duration, runes.getTier() - 1));
                    }
                }
            }
        }
    }
    
    /**
     * Called periodically clientside on blocks near the player to show effects (like furnace fire particles).
     */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        Optional<RuneBlockEntity> beOptional = level.getBlockEntity(pos, RUNE_BLOCK_ENTITY.get());
        if (beOptional.isPresent()) {
            RuneBlockEntity be = beOptional.get();
            if (be.inventory.getFirst().getItem() instanceof AbstractRuneItem target && be.inventory.get(1).getItem() instanceof AbstractRuneItem effect) {
                if (target == SELF_RUNE.get()) {
                    List<Integer> colors = effectToColor(effect);
                    ParticleUtils.spawnParticleInBlock(
                            level,
                            pos,
                            10,
                            ColorParticleOption.create(RSParticleTypes.SELF_RUNE.get(), colors.getFirst(), colors.get(1), colors.getLast())
//                            RSParticleTypes.SELF_RUNE.get()
                    );
//                    level.addParticle(
//                            new ColoredParticleType(false, colors.getFirst(), colors.get(1), colors.getLast()),
//                            pos.getX(),
//                            pos.getY(),
//                            pos.getZ(),
//                            random.nextFloat(),
//                            random.nextFloat(),
//                            random.nextFloat()
//                    );
                } else if (target == WIELD_RUNE.get()) {
                
                } else if (target == COLLISION_RUNE.get()) {
                
                }
            }
        }
    }
    
    private List<Integer> effectToColor(AbstractRuneItem effect) {
        
        return effectToColor.get(effect);
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
        return createTickerHelper(blockEntityType, RUNE_BLOCK_ENTITY.get(), RuneBlockEntity::serverTick);
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
    
    Vec3 vectorFromDirection(Direction direction) {
        return new Vec3(direction.getNormal().getX(), direction.getNormal().getY(), direction.getNormal().getZ());
    }
    
    private Direction getSecondNearest(Vec3 vector) {
        Direction direction = NORTH;
        float largestSoFar = Float.MIN_VALUE;
        
        float x = (float) vector.x();
        float y = (float) vector.y();
        float z = (float) vector.z();
        
        List<Direction> result = new ArrayList<>(List.of(direction, direction));
        
        for (Direction direction1 : Direction.values()) {
            float dotProduct = x * (float) direction1.getNormal().getX() + y * (float) direction1.getNormal().getY() + z * (float) direction1.getNormal().getZ();
            if (dotProduct > largestSoFar) {
                // store new largest result, and overwrite old second largest result
                result.set(0, result.get(1));
                largestSoFar = dotProduct;
                result.set(1, direction1);
            }
        }
        
        return result.get(0);
    }
}
