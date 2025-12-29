package com.github.no_name_provided.nnp_rune_smithing.common.blocks;

import com.github.no_name_provided.nnp_rune_smithing.client.particles.RSParticleTypes;
import com.github.no_name_provided.nnp_rune_smithing.common.entities.RuneBlockEntity;
import com.github.no_name_provided.nnp_rune_smithing.common.items.runes.AbstractRuneItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.CriteriaTriggers;
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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
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
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.world.AuxiliaryLightManager;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static com.github.no_name_provided.nnp_rune_smithing.common.entities.RSEntities.RUNE_BLOCK_ENTITY;
import static com.github.no_name_provided.nnp_rune_smithing.common.entities.RuneBlockEntity.*;
import static com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems.*;
import static net.minecraft.core.Direction.NORTH;
import static net.neoforged.neoforge.common.NeoForgeMod.WATER_TYPE;

public class RuneBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    
    int tickRate = 20;
    
    /**
     * [Red, Green, Blue]
     */
    public static HashMap<AbstractRuneItem, List<Integer>> effectToColor = HashMap.newHashMap(4);
    
    public RuneBlock(Properties properties) {
        super(properties.noCollission().noTerrainParticles().strength(1000));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.DOWN));
    }
    
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level instanceof ServerLevel sLevel) {
            // There's probably a more efficient way to update this
            tickRate = (int) level.tickRateManager().tickrate();
            if (stack.getItem() instanceof AbstractRuneItem item && sLevel.getBlockEntity(pos) instanceof RuneBlockEntity runes && runes.getItem(item.getType().ordinal()).isEmpty()) {
                runes.setItem(item.getType().ordinal(), stack.copyWithCount(1));
                CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer) player, pos, stack);
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
            boolean isInverted = runes.getItem(MODIFIER).is(INVERT_RUNE);
            if (runes.getItem(TARGET).is(SELF_RUNE)) {
                if (entity instanceof Monster monster && monster.mainSupportingBlockPos.isPresent() && monster.mainSupportingBlockPos.get().above().equals(pos) && runes.getItem(EFFECT).is(WARD_RUNE.get())) {
                    // TODO: think of a good inverse of warding. Maybe pulling through quickly, or empowering, or trapping in the middle of...
//                    Vec3 oldMovement = monster.getDeltaMovement();
                    // Switch to using the direction monster's looking, as this appears to be more reliable than checking its movement
                    Vec3 oldMovement = vectorFromDirection(monster.getNearestViewDirection());
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
                    if (runes.getTier() >= 3 && level instanceof ServerLevel sLevel) {
                        LightningBolt lightningbolt = EntityType.LIGHTNING_BOLT.create(sLevel);
                        if (lightningbolt != null) {
                            lightningbolt.moveTo(Vec3.atBottomCenterOf(pos));
                            lightningbolt.setVisualOnly(false);
                            sLevel.addFreshEntity(lightningbolt);
                        }
                    }
                    if (runes.getTier() >= 2) {
                        monster.setRemainingFireTicks(20 * 3 * runes.getTier());
                    }
                }
            } else if (runes.getItem(TARGET).is(COLLISION_RUNE) && entity instanceof LivingEntity lifeform) {
                if (runes.getItem(EFFECT).is(WARD_RUNE) && lifeform.isAffectedByPotions()) {
                    // TODO: think of a good inverse of warding. Maybe pulling through quickly, or empowering, or trapping in the middle of...
                    MobEffectInstance effect = lifeform.getEffect(MobEffects.ABSORPTION);
                    int duration = tickRate * 60;
                    if (null == effect || effect.getDuration() < duration / 2 && effect.getAmplifier() <= runes.getTier()) {
                        lifeform.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, duration, runes.getTier()));
                    }
                } else if (runes.getItem(EFFECT).is(SIGHT_RUNE) && lifeform.isAffectedByPotions()) {
                    MobEffectInstance effect = lifeform.getEffect(!isInverted ? MobEffects.NIGHT_VISION : MobEffects.DARKNESS);
                    int duration = tickRate * 20 * runes.getTier();
                    if (null == effect || effect.getDuration() < duration / 2 && effect.getAmplifier() <= runes.getTier()) {
                        lifeform.addEffect(
                                new MobEffectInstance(
                                        !isInverted ? MobEffects.NIGHT_VISION : MobEffects.DARKNESS,
                                        duration,
                                        runes.getTier()
                                )
                        );
                    }
                } else if (runes.getItem(EFFECT).is(EARTH_RUNE) && lifeform instanceof ServerPlayer player) {
                    FoodData foodData = player.getFoodData();
                    if (isInverted) {
                        foodData.setSaturation(foodData.getSaturationLevel() * (float) Math.pow(0.9, runes.getTier()));
                    } else if (foodData.needsFood()) {
                        foodData.eat(1, 0.5f * runes.getTier());
                    }
                } else if (runes.getItem(EFFECT).is(WATER_RUNE) && lifeform.isAffectedByPotions()) {
                    MobEffectInstance effect = lifeform.getEffect(MobEffects.WATER_BREATHING);
                    int duration = tickRate * 60;
                    if (null == effect || effect.getDuration() < duration / 2 && !isInverted) {
                        lifeform.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, duration));
                    } else if (level.random.nextInt(10) < runes.getTier() && lifeform.canDrownInFluidType(WATER_TYPE.value())) {
                        lifeform.setAirSupply(0);
                        lifeform.hurt(level.damageSources().drown(), 0.5f * runes.getTier());
                    }
                } else if (runes.getItem(EFFECT).is(AIR_RUNE) && lifeform.isAffectedByPotions()) {
                    MobEffectInstance effect = lifeform.getEffect(!isInverted ? MobEffects.MOVEMENT_SPEED : MobEffects.MOVEMENT_SLOWDOWN);
                    int duration = tickRate * 20 * 3;
                    if (null == effect || effect.getDuration() < duration / 2 && effect.getAmplifier() < runes.getTier()) {
                        lifeform.addEffect(new MobEffectInstance(isInverted ? MobEffects.MOVEMENT_SPEED : MobEffects.MOVEMENT_SLOWDOWN, duration, runes.getTier()));
                    }
                } else if (runes.getItem(EFFECT).is(FIRE_RUNE) && lifeform.isAffectedByPotions()) {
                    MobEffectInstance effect = lifeform.getEffect(!isInverted ? MobEffects.DAMAGE_BOOST : MobEffects.WEAKNESS);
                    int duration = tickRate * 20;
                    if (null == effect || effect.getDuration() < duration / 2 && effect.getAmplifier() < runes.getTier() - 1) {
                        lifeform.addEffect(new MobEffectInstance(!isInverted ? MobEffects.DAMAGE_BOOST : MobEffects.WEAKNESS, duration, runes.getTier() - 1));
                    }
                } else if (runes.getItem(EFFECT).is(LIGHT_RUNE) && lifeform.isAffectedByPotions()) {
                    MobEffectInstance effect = lifeform.getEffect(!isInverted ? MobEffects.HEALTH_BOOST : MobEffects.LEVITATION);
                    int duration = !isInverted ? tickRate * 20 * 60 * runes.getTier() : tickRate * 20 * 10 * runes.getTier();
                    if (null == effect || effect.getDuration() < duration / 2 && effect.getAmplifier() < runes.getTier() - 1) {
                        lifeform.addEffect(new MobEffectInstance(!isInverted ? MobEffects.HEALTH_BOOST : MobEffects.LEVITATION, duration));
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
        if (random.nextInt(5) < 4) {
            Optional<RuneBlockEntity> beOptional = level.getBlockEntity(pos, RUNE_BLOCK_ENTITY.get());
            if (beOptional.isPresent()) {
                RuneBlockEntity be = beOptional.get();
                if (be.inventory.getFirst().getItem() instanceof AbstractRuneItem target && be.inventory.get(1).getItem() instanceof AbstractRuneItem effect) {
                    if (target == SELF_RUNE.get()) {
                        BlockPos offset = BlockPos.ZERO;
                        if (be.inventory.get(MODIFIER).is(TUNNEL_RUNE)) {
                            offset = RuneBlockEntity.getTunnelingOffset(state);
                        }
                        List<Integer> colors = effectToColor(effect);
                        ParticleUtils.spawnParticleInBlock(
                                level,
                                pos.offset(offset),
                                5,
                                ColorParticleOption.create(
                                        RSParticleTypes.SELF_RUNE.get(),
                                        (float) colors.getFirst() / 255,
                                        (float) colors.get(1) / 255,
                                        (float) colors.getLast() / 255
                                )
                        );
                    } else if (target == COLLISION_RUNE.get()) {
                        List<Integer> colors = effectToColor(effect);
                        ParticleUtils.spawnParticleInBlock(
                                level,
                                pos,
                                4,
                                ColorParticleOption.create(
                                        RSParticleTypes.COLLISION_RUNE.get(),
                                        (float) colors.getFirst() / 255,
                                        (float) colors.get(1) / 255,
                                        (float) colors.getLast() / 255
                                )
                        );
                    }
                }
            }
        }
    }
    
    private List<Integer> effectToColor(AbstractRuneItem effect) {
        
        return effectToColor.get(effect);
    }
    
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        
        return switch (state.getValue(BlockStateProperties.FACING)) {
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

        return this.defaultBlockState().setValue(FACING, context.getClickedFace().getOpposite());
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.FACING);
    }
    
    @Override
    public boolean hasDynamicLightEmission(BlockState state) {
        
        return true;
    }
    
    @Override
    public int getLightEmission(BlockState state, BlockGetter blockGetter, BlockPos pos) {
        AuxiliaryLightManager manager = blockGetter.getAuxLightManager(pos);
        
        if (null != manager) {
            
            return manager.getLightAt(pos);
        } else {
            return blockGetter.getLightEmission(pos);
        }
    }
    
    Vec3 vectorFromDirection(Direction direction) {
        
        return new Vec3(direction.getNormal().getX(), direction.getNormal().getY(), direction.getNormal().getZ());
    }
    
    /**
     * Called if the nearest direction isn't viable for some reason.
     * @return The second-closest direction to the provided vector.
     */
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
                // Store new largest result, and overwrite old second-largest result
                result.set(0, result.get(1));
                largestSoFar = dotProduct;
                result.set(1, direction1);
            }
        }
        
        return result.get(0);
    }
    
    // The following are a series of attempts to stop fluid from replacing these.
    // They probably do nothing, since the actual problem was the small bounding box and lack of forcing solid on.
    @Override
    protected boolean canBeReplaced(BlockState state, Fluid fluid) {
        return false;
    }
    
    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
        return false;
    }
    
    @Override
    protected boolean isAir(BlockState state) {
        return false;
    }
}
