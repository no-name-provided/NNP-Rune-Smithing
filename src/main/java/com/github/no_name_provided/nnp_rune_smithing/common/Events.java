package com.github.no_name_provided.nnp_rune_smithing.common;

import com.github.no_name_provided.nnp_rune_smithing.common.attachments.RSAttachments;
import com.github.no_name_provided.nnp_rune_smithing.common.blocks.RuneBlock;
import com.github.no_name_provided.nnp_rune_smithing.common.capabilities.AlloyerCapability;
import com.github.no_name_provided.nnp_rune_smithing.common.capabilities.CastingTableCapability;
import com.github.no_name_provided.nnp_rune_smithing.common.capabilities.MelterCapability;
import com.github.no_name_provided.nnp_rune_smithing.common.data_components.RunesAdded;
import com.github.no_name_provided.nnp_rune_smithing.common.entities.RSEntities;
import com.github.no_name_provided.nnp_rune_smithing.common.items.runes.AbstractRuneItem;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.windcharge.BreezeWindCharge;
import net.minecraft.world.entity.projectile.windcharge.WindCharge;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;
import static com.github.no_name_provided.nnp_rune_smithing.common.RSAttributeModifiers.*;
import static com.github.no_name_provided.nnp_rune_smithing.common.attachments.RSAttachments.*;
import static com.github.no_name_provided.nnp_rune_smithing.common.data_components.RSDataComponents.RUNES_ADDED;
import static com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems.*;
import static net.minecraft.SharedConstants.TICKS_PER_SECOND;
import static net.minecraft.world.entity.EntityType.*;
import static net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.*;
import static net.minecraft.world.entity.projectile.windcharge.AbstractWindCharge.EXPLOSION_DAMAGE_CALCULATOR;

@EventBusSubscriber
public class Events {
    // Unsynced client only constants - distinct values for each player
    static int miningDuration;
    static BlockPos targetedForBreaking;
    
    @SubscribeEvent
    static void onCommonSetup(FMLCommonSetupEvent event) {
        // Set the particle color for each effect rune
        event.enqueueWork(
                () -> RuneBlock.effectToColor.putAll(Map.of(
                                WARD_RUNE.get(), List.of(140, 173, 171),
                                AIR_RUNE.get(), List.of(196, 236, 255),
                                WATER_RUNE.get(), List.of(66, 170, 217),
                                FIRE_RUNE.get(), List.of(236, 24, 35),
                                EARTH_RUNE.get(), List.of(175, 88, 47),
                                SIGHT_RUNE.get(), List.of(220, 243, 255),
                                VOID_RUNE.get(), List.of(189, 135, 255),
                                LIGHT_RUNE.get(), List.of(238, 255, 61)
                        )
                )
        );
    }
    
    /**
     * May assume server side.
     */
    @SubscribeEvent
    static void onEquipmentChanged(LivingEquipmentChangeEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (event.getSlot().isArmor()) {
                RunesAdded oldRunes = event.getFrom().getOrDefault(RUNES_ADDED, RunesAdded.DEFAULT.get());
                RunesAdded newRunes = event.getTo().getOrDefault(RUNES_ADDED, RunesAdded.DEFAULT.get());
                if (newRunes.equals(oldRunes)) {
                    
                    return;
                }
                // Docs say double, instance constructor takes double, value retrieved/set is float?
                double absorptionChange = 0;
                float absorptionPerTier = RSServerConfig.absorptionPerTier;
                double speedChange = 0;
                float speedPerTier = RSServerConfig.speedPerTier;
                double underwaterMiningSpeedChange = 0;
                float underwaterMiningSpeedPerTier = RSServerConfig.underwaterMiningSpeedPerTier;
                double extraAirChange = 0;
                float extraAirPerTier = RSServerConfig.extraAirPerTier;
                double waterSpeedChange = 0;
                float extraWaterSpeedPerTier = RSServerConfig.extraWaterSpeedPerTier;
                double healthChange = 0;
                float healthPerTier = RSServerConfig.healthPerTier;
                double burnTimeMultChange = 0;
                float burnTimeMultPerTier = RSServerConfig.burnTimeMultPerTier;
                if (!oldRunes.equals(RunesAdded.DEFAULT.get())) {
                    if (oldRunes.target().rune() == WIELD_RUNE.get()) {
                        AbstractRuneItem rune = oldRunes.effect().rune();
                        if (rune == WARD_RUNE.get()) {
                            absorptionChange -= absorptionPerTier * oldRunes.effectiveTier();
                        } else if (rune == SIGHT_RUNE.get()) {
                            player.setData(RSAttachments.SHOW_RUNE_BLOCK_BOUNDING_BOXES, false);
                            //noinspection StatementWithEmptyBody
                            if (oldRunes.amplifier().rune() == AMPLIFY_RUNE.get()) {
                                // pass
                            }
                        } else if (rune == AIR_RUNE.get()) {
                            speedChange -= speedPerTier * oldRunes.effectiveTier();
                        } else if (rune == WATER_RUNE.get()) {
                            underwaterMiningSpeedChange -= underwaterMiningSpeedPerTier * oldRunes.effectiveTier();
                            extraAirChange -= extraAirPerTier * oldRunes.effectiveTier();
                            waterSpeedChange -= extraWaterSpeedPerTier * oldRunes.effectiveTier();
                        } else if (rune == EARTH_RUNE.get()) {
                            healthChange -= healthPerTier * oldRunes.effectiveTier();
                        } else if (rune == FIRE_RUNE.get()) {
                            burnTimeMultChange -= burnTimeMultPerTier * oldRunes.effectiveTier();
                        }
                    }
                }
                if (!newRunes.equals(RunesAdded.DEFAULT.get())) {
                    if (newRunes.target().rune() == WIELD_RUNE.get()) {
                        AbstractRuneItem rune = newRunes.effect().rune();
                        if (rune == WARD_RUNE.get()) {
                            absorptionChange += absorptionPerTier * newRunes.effectiveTier();
                        } else if (rune == SIGHT_RUNE.get()) {
                            player.setData(RSAttachments.SHOW_RUNE_BLOCK_BOUNDING_BOXES, true);
                            //noinspection StatementWithEmptyBody
                            if (oldRunes.amplifier().rune() == AMPLIFY_RUNE.get()) {
                                // pass
                            }
                        } else if (rune == AIR_RUNE.get()) {
                            speedChange += speedPerTier * newRunes.effectiveTier();
                        } else if (rune == WATER_RUNE.get()) {
                            underwaterMiningSpeedChange += underwaterMiningSpeedPerTier * newRunes.effectiveTier();
                            extraAirChange += extraAirPerTier * newRunes.effectiveTier();
                            waterSpeedChange += extraWaterSpeedPerTier * newRunes.effectiveTier();
                        } else if (rune == EARTH_RUNE.get()) {
                            healthChange += healthPerTier * newRunes.effectiveTier();
                        } else if (rune == FIRE_RUNE.get()) {
                            burnTimeMultChange += burnTimeMultPerTier * newRunes.effectiveTier();
                        }
                    }
                }
                updateAttribute(absorptionChange, player, RSAttributeModifiers::wardRuneAbsorption, WARD_RUNE_ABSORPTION, Attributes.MAX_ABSORPTION);
                updateAttribute(speedChange, player, RSAttributeModifiers::airRuneSpeed, AIR_RUNE_SPEED, Attributes.MOVEMENT_SPEED);
                updateAttribute(underwaterMiningSpeedChange, player, RSAttributeModifiers::waterRuneUWMiningSpeed, WATER_RUNE_UW_MINING_SPEED, Attributes.SUBMERGED_MINING_SPEED);
                updateAttribute(extraAirChange, player, RSAttributeModifiers::waterRuneExtraAir, WATER_RUNE_EXTRA_AIR, Attributes.OXYGEN_BONUS);
                updateAttribute(waterSpeedChange, player, RSAttributeModifiers::waterRuneExtraSwimSpeed, WATER_RUNE_EXTRA_SWIM_SPEED, Attributes.WATER_MOVEMENT_EFFICIENCY);
                updateAttribute(healthChange, player, RSAttributeModifiers::earthRuneHealthChange, EARTH_RUNE_HEALTH, Attributes.MAX_HEALTH);
                updateAttribute(burnTimeMultChange, player, RSAttributeModifiers::fireRuneBurnTimeMultChange, FIRE_RUNE_BURNING_TIME, Attributes.BURNING_TIME);
            }
        }
    }
    
    @SubscribeEvent
    static void onArmorHurt(ArmorHurtEvent event) {
        // Flat reductions of even 1/tier are too powerful
        float damageReductionPerTier = Mth.randomBetween(event.getEntity().getRandom(), 0f, 1.0f);
        Map<EquipmentSlot, ArmorHurtEvent.ArmorEntry> map = event.getArmorMap();
        map.keySet().forEach(slot -> {
            RunesAdded runes = map.get(slot).armorItemStack.get(RUNES_ADDED);
            if (null != slot && null != runes && runes.target().rune() == SELF_RUNE.get() && runes.effect().rune() == WARD_RUNE.get()) {
                event.setNewDamage(slot, Mth.clamp(map.get(slot).originalDamage, 0, Math.max(0, map.get(slot).originalDamage - runes.effectiveTier() * damageReductionPerTier)));
            }
        });
    }
    
    /**
     * Item stacks aren't editable in the anvil repair event.
     */
    @SubscribeEvent
    static void onAnvilUpdate(AnvilUpdateEvent event) {
        int damageReductionPerTier = 10;
        ItemStack input = event.getLeft();
        ItemStack oldOutput = event.getOutput();
        // Verify it's a successful repair recipe
        if (input.getItem() == oldOutput.getItem() && oldOutput.getDamageValue() < input.getDamageValue()) {
            RunesAdded runes = input.get(RUNES_ADDED);
            if (null != runes && runes.target().rune() == SELF_RUNE.get() && runes.effect().rune() == EARTH_RUNE.get()) {
                ItemStack newOutput = oldOutput.copy();
                newOutput.setDamageValue(Mth.clamp(oldOutput.getDamageValue() - damageReductionPerTier * runes.effectiveTier(), 0, oldOutput.getDamageValue()));
                event.setOutput(newOutput);
            }
        }
    }
    
    @SubscribeEvent
    static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                RSEntities.MELTER_BLOCK_ENTITY.get(),
                (entity, context) -> new MelterCapability.MelterFluidHandler(entity)
        );
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                RSEntities.ALLOYER_BLOCK_ENTITY.get(),
                AlloyerCapability.FluidHandler::new
        );
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                RSEntities.CASTING_TABLE_BLOCK_ENTITY.get(),
                (entity, context) -> new CastingTableCapability.CastingTableFluidCapability(entity)
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                RSEntities.CASTING_TABLE_BLOCK_ENTITY.get(),
                (entity, context) -> new CastingTableCapability.CastingTableItemCapability(entity)
        );
    }
    
    @SubscribeEvent
    static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity attacked = event.getEntity();
        if (attacked.getExistingData(LUCKY.get()).orElse(false) && attacked.level().random.nextInt(2) < 1) {
            Vec3 sourcePosition = event.getSource().getSourcePosition();
            if (null != sourcePosition) {
                attacked.knockback(0.6, sourcePosition.x() - attacked.getX(), sourcePosition.z() - attacked.getZ());
            }
            
            event.setCanceled(true);
        }
        if (attacked.getExistingData(INVERTED.get()).orElse(false)) {
            if (!(attacked instanceof Player) && event.getSource().getDirectEntity() instanceof Player player) {
                // Reverse damage direction
                player.hurt(new DamageSource(event.getSource().typeHolder(), attacked, player, attacked.position()), event.getAmount());
                // Reverse knockback direction (lazy implementation that doesn't throw event or check player (knockback) strength
                Vec3 sourcePosition = event.getSource().getSourcePosition();
                if (null != sourcePosition) {
                    attacked.knockback(0.6, attacked.getX() - sourcePosition.x(), attacked.getZ() - sourcePosition.z());
                }
                
                event.setCanceled(true);
            }
        }
    }
    
    @SubscribeEvent
    static void onLivingDamagePre(LivingDamageEvent.Pre event) {
        DamageSource source = event.getSource();
        if (source.isDirect()) {
            // Handle attacker using runic weapon
            ItemStack weapon = source.getWeaponItem();
            if (null != weapon) {
                RunesAdded runesAdded = weapon.get(RUNES_ADDED);
                if (null != runesAdded && runesAdded.effectiveTier() > 0) {
                    boolean isInverted = runesAdded.modifier().rune() == INVERT_RUNE.get();
                    DamageContainer container = event.getContainer();
                    
                    // Collisions
                    if (runesAdded.target().rune() == COLLISION_RUNE.get()) {
                        AbstractRuneItem effect = runesAdded.effect().rune();
                        if (effect == EARTH_RUNE.get()) {
                            container.addModifier(
                                    DamageContainer.Reduction.ARMOR,
                                    (con, old) ->
                                            !isInverted ? old / runesAdded.effectiveTier() : old * runesAdded.effectiveTier()
                            );
                        }
                    }
                    
                    // Wield effects
                    if (runesAdded.target().rune() == WIELD_RUNE.get()) {
                        AbstractRuneItem effect = runesAdded.effect().rune();
                        if (effect == FIRE_RUNE.get()) {
                            // Consider moving this to incoming damage, so armor effects apply
                            container.setNewDamage(
                                    isInverted ? container.getNewDamage() + runesAdded.effectiveTier() :
                                            Mth.clamp(
                                                    container.getNewDamage() - runesAdded.effectiveTier(),
                                                    0, container.getNewDamage()
                                            )
                            );
                        }
                    }
                }
            }
        }
    }
    
    @SubscribeEvent
    static void onLivingDamagePost(LivingDamageEvent.Post event) {
        DamageSource source = event.getSource();
        if (source.isDirect()) {
            LivingEntity attacked = event.getEntity();
            
            // Handle runic mobs as attackers
            if (source.getEntity() instanceof Mob attacker) {
                if (attacker.getExistingData(RAVENOUS).orElse(false)) {
                    attacker.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 5, 3));
                    attacker.level().playLocalSound(attacker, SoundEvents.PANDA_BITE, SoundSource.HOSTILE, 1.0f, 1.0f);
                } else if (attacker.getExistingData(POISONOUS).orElse(false)) {
                    attacked.addEffect(new MobEffectInstance(MobEffects.POISON, 20 * 3, 2));
                } else if (attacker.getExistingData(VOID_FUSED).orElse(false)) {
                    if (event.getNewDamage() > 1) {
                        attacker.addEffect(new MobEffectInstance(MobEffects.HEAL, 1, 2));
                    }
                    attacker.level().playLocalSound(attacker, SoundEvents.WARDEN_ATTACK_IMPACT, SoundSource.HOSTILE, 5.0f, (attacker.getRandom().nextFloat() - attacker.getRandom().nextFloat()) * 0.2f + 1.0f);
                    attacked.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20 * 10, 2));
                    attacked.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 20 * 10, 2, false, false));
                    attacked.addEffect(new MobEffectInstance(MobEffects.WITHER, 20 * 5));
                    attacked.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20 * 10, 2));
                }
            }
            
            
            // Handle attacker using runic weapon
            ItemStack weapon = source.getWeaponItem();
            if (null != weapon) {
                RunesAdded runesAdded = weapon.get(RUNES_ADDED);
                if (null != runesAdded && runesAdded.effectiveTier() > 0) {
                    boolean isInverted = runesAdded.modifier().rune() == INVERT_RUNE.get();
                    // Collisions
                    if (runesAdded.target().rune() == COLLISION_RUNE.get()) {
                        AbstractRuneItem effect = runesAdded.effect().rune();
                        if (effect == WARD_RUNE.get()) {
                            attacked.addEffect(
                                    new MobEffectInstance(
                                            MobEffects.ABSORPTION,
                                            TICKS_PER_SECOND * 60 * runesAdded.effectiveTier(),
                                            runesAdded.effectiveTier()
                                    )
                            );
                        } else if (effect == SIGHT_RUNE.get() && attacked instanceof LivingEntity livingAttacked) {
                            livingAttacked.addEffect(
                                    new MobEffectInstance(
                                            !isInverted ? MobEffects.BLINDNESS : MobEffects.GLOWING,
                                            TICKS_PER_SECOND * 3 * runesAdded.effectiveTier(),
                                            runesAdded.effectiveTier()
                                    )
                            );
                        } else if (effect == WATER_RUNE.get()) {
                            attacked.addEffect(
                                    new MobEffectInstance(
                                            !isInverted ? MobEffects.WATER_BREATHING : MobEffects.FIRE_RESISTANCE,
                                            TICKS_PER_SECOND * 60 * runesAdded.effectiveTier(),
                                            runesAdded.effectiveTier()
                                    )
                            );
                        } else if (effect == FIRE_RUNE.get() && !attacked.fireImmune()) {
                            if (!isInverted) {
                                attacked.setRemainingFireTicks(20 * 10 * runesAdded.effectiveTier());
                            } else {
                                attacked.setRemainingFireTicks(0);
                            }
                        }
                    }
                }
            }
            
            // Handle attacked having runic armor
            attacked.getArmorSlots().forEach(armor -> {
                RunesAdded runesAdded = armor.get(RUNES_ADDED);
                if (null != runesAdded) {
                    boolean isInverted = runesAdded.modifier().rune() == INVERT_RUNE.get();
                    int tier = runesAdded.effectiveTier();
                    if (runesAdded.target().rune() == COLLISION_RUNE.get()) {
                        Entity attacker = source.getEntity();
                        if (null != attacker) {
                            AbstractRuneItem effect = runesAdded.effect().rune();
                            if (effect == WARD_RUNE.get() && attacker instanceof LivingEntity livingAttacker) {
                                livingAttacker.addEffect(
                                        new MobEffectInstance(
                                                MobEffects.ABSORPTION,
                                                TICKS_PER_SECOND * 60 * runesAdded.effectiveTier(),
                                                runesAdded.effectiveTier()
                                        )
                                );
                            } else if (effect == SIGHT_RUNE.get() && attacker instanceof LivingEntity livingAttacker) {
                                livingAttacker.addEffect(
                                        new MobEffectInstance(
                                                !isInverted ? MobEffects.GLOWING : MobEffects.DARKNESS,
                                                TICKS_PER_SECOND * 10 * tier
                                        )
                                );
                            } else if (effect == AIR_RUNE.get() && attacked.level() instanceof Level level) {
                                //noinspection StatementWithEmptyBody
                                if (!isInverted) {
                                    level.explode(
                                            attacked,
                                            null,
                                            EXPLOSION_DAMAGE_CALCULATOR,
                                            attacked.getX(),
                                            attacked.getY(),
                                            attacked.getZ(),
                                            tier,
                                            false,
                                            Level.ExplosionInteraction.TRIGGER,
                                            ParticleTypes.GUST_EMITTER_SMALL,
                                            ParticleTypes.GUST_EMITTER_LARGE,
                                            SoundEvents.BREEZE_WIND_CHARGE_BURST
                                    );
                                } else {
                                    // TODO: think of a good inversion effect
                                }
                            } else if (effect == WATER_RUNE.get() && attacker.canFreeze()) {
                                if (!isInverted) {
                                    attacker.setTicksFrozen(attacker.getTicksRequiredToFreeze());
                                } else {
                                    attacked.setRemainingFireTicks(20 * 10 * runesAdded.effectiveTier());
                                }
                            } else if (effect == FIRE_RUNE.get() && !attacker.fireImmune()) {
                                if (!isInverted) {
                                    attacker.setRemainingFireTicks(20 * 10 * runesAdded.effectiveTier());
                                } else {
                                    attacker.setTicksFrozen(attacker.getTicksRequiredToFreeze());
                                }
                            } else if (effect == EARTH_RUNE.get() && attacker instanceof LivingEntity livingAttacker) {
                                livingAttacker.addEffect(
                                        new MobEffectInstance(
                                                !isInverted ? MobEffects.POISON : MobEffects.REGENERATION,
                                                20 * 2 * tier,
                                                tier
                                        )
                                );
                            }
                        }
                    }
                }
            });
        }
    }
    
    @SubscribeEvent
    static void EntityInvulnerabilityCheckEvent(EntityInvulnerabilityCheckEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Mob mob) {
            if (mob.getExistingData(BLAST_PROOF).orElse(false) && (event.getSource().is(DamageTypes.EXPLOSION) || event.getSource().is(DamageTypes.PLAYER_EXPLOSION))) {
                event.setInvulnerable(true);
            }
        }
    }
    
    /**
     * Could stand to be reworked. Makes Runic Breeze wind charges fire secondary
     * charges on impact... technically. There aren't many, and they tend to disappear quickly.
     */
    @SubscribeEvent
    static void onProjectileImpact(ProjectileImpactEvent event) {
        if (event.getProjectile() instanceof BreezeWindCharge charge &&
                charge.getOwner() instanceof Breeze breeze &&
                breeze.getExistingData(GALE_BREEZE.get()).orElse(false)) {
            Entity pEntity = event.getEntity();
            Level level = pEntity.level();
            Direction.allShuffled(breeze.getRandom()).forEach(direction -> {
                WindCharge newCharge = new WindCharge(level, pEntity.getX(), pEntity.getY(), pEntity.getZ(), new Vec3(0, 0, 0));
                newCharge.shoot(direction.getStepX(), direction.getStepY(), direction.getStepZ(), 0.7f, (float) (5 - level.getDifficulty().getId() * 4));
                level.addFreshEntity(newCharge);
            });
        }
    }
    
    /**
     * Special breaking logic. Mostly reimplementing vanilla behavior for chain mined or hammered blocks.
     * <p></p>
     * As alternatives, consider calling ServerPlayer#gameMode#destroyBlock and passing it a version of the tool without
     * runes attached (to avoid recursion issues) or simply using an event earlier in the call chain.
     */
    @SubscribeEvent
    static void onBlockBreak(BlockEvent.BreakEvent event) {
        ItemStack tool = event.getPlayer().getMainHandItem();
        if (!event.isCanceled() && !tool.isEmpty() && event.getPlayer() instanceof ServerPlayer player) {
            RunesAdded runesAdded = tool.get(RUNES_ADDED);
            if (null != runesAdded) {
                int tier = runesAdded.effectiveTier();
                if (runesAdded.target().rune() == COLLISION_RUNE.get() && tier > 0) {
                    if (runesAdded.effect().rune() == EARTH_RUNE.get()) {
                        int radius = 1;
                        if (runesAdded.modifier().rune() == WIDEN_RUNE.get()) {
                            radius++;
                        } else if (runesAdded.modifier().rune() == NARROW_RUNE.get()) {
                            radius--;
                        }
                        // Event is only thrown on server and player is already an instance of server player...
                        ServerLevel level = (ServerLevel) player.level();
                        BlockPos pos = event.getPos();
                        Pair<BlockPos, BlockPos> posPair = getStartEndBreakPositions(pos, player, radius);
                        BlockPos.betweenClosed(posPair.getFirst(), posPair.getSecond()).forEach(position -> {
                            BlockState stateToHarvest = level.getBlockState(position);
                            Block blockToHarvest = stateToHarvest.getBlock();
                            BlockEntity entityToHarvest = level.getBlockEntity(position);
                            // Reference: net.minecraft.server.level.ServerPlayerGameMode.destroyBlock
                            // Not calling it directly via ServerPlayer#gameMode#destroyBlock, because then my event handler would call itself
                            if (stateToHarvest.canHarvestBlock(level, position, player) &&
                                    // Skip indestructible blocks, like bedrock
                                    blockToHarvest.defaultDestroyTime() >= 0 &&
                                    tool.isCorrectToolForDrops(stateToHarvest) &&
                                    // Account for spawn chunk protection and world border
                                    player.mayInteract(level, position) &&
                                    // Check game mode
                                    !player.blockActionRestricted(level, position, player.gameMode.getGameModeForPlayer()) &&
                                    !(blockToHarvest instanceof GameMasterBlock && !player.canUseGameMasterBlocks())) {
                                stateToHarvest = blockToHarvest.playerWillDestroy(level, position, stateToHarvest, player);
                                boolean wasDestroyed = stateToHarvest.onDestroyedByPlayer(level, position, player, false, level.getFluidState(position));
                                if (player.isCreative() && wasDestroyed) {
                                    stateToHarvest.getBlock().destroy(level, position, stateToHarvest);
                                    
                                    // In iterable foreach loops, return apparently functions as continue. Shrug, that's Java.
                                    return;
                                }
                                // Increment item use statistics
                                tool.mineBlock(level, stateToHarvest, pos, player);
                                if (wasDestroyed && stateToHarvest.canHarvestBlock(level, position, player)) {
                                    level.destroyBlock(position, true, player);
                                    // Handle block break statistics
                                    blockToHarvest.playerDestroy(level, player, position, stateToHarvest, entityToHarvest, tool);
                                    
                                    // We should never get here with an empty item stack (no components)
                                    if (tool.isEmpty()) {
                                        net.neoforged.neoforge.event.EventHooks.onPlayerDestroyItem(player, tool, InteractionHand.MAIN_HAND);
                                    }
                                }
                            }
                        });
                    }
                }
            }
        }
    }
    
    /**
     * Maps axis to corresponding block range. Would be better if I spun up a clip context and used the targeted face,
     * but oh well.
     */
    public static Pair<BlockPos, BlockPos> getStartEndBreakPositions(BlockPos pos, Player player, int radius) {
        
        // Can't get clicked face from this event?
        Direction direction = player.getNearestViewDirection();
        Direction.Axis orientation = direction.getAxis();
        return switch (orientation) {
            // East - West
            case X -> Pair.of(pos.above(radius).north(radius), pos.below(radius).south(radius));
            // Up - Down
            case Y -> Pair.of(pos.west(radius).south(radius), pos.east(radius).north(radius));
            // North - South
            case Z -> Pair.of(pos.above(radius).east(radius), pos.below(radius).west(radius));
        };
    }
    
    /**
     * References:
     * {@link
     * net.minecraft.world.level.block.state.BlockBehaviour#getDestroyProgress(net.minecraft.world.level.block.state.BlockState,
     * net.minecraft.world.entity.player.Player, net.minecraft.world.level.BlockGetter, net.minecraft.core.BlockPos)}
     * {@link ServerPlayerGameMode#incrementDestroyProgress(BlockState, BlockPos, int)}
     * {@link LevelRenderer#destroyBlockProgress(int, BlockPos, int)}
     */
    @SubscribeEvent
    static void onPlayerLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        ItemStack useItem = event.getItemStack();
        if (!useItem.isEmpty()) {
            RunesAdded runesAdded = useItem.get(RUNES_ADDED);
            if (null != runesAdded) {
                if (runesAdded.effect().rune() == EARTH_RUNE.get()) {
                    BlockPos pos = event.getPos();
                    Level level = event.getLevel();
                    int radius = 1;
                    if (runesAdded.modifier().rune() == WIDEN_RUNE.get()) {
                        radius++;
                    } else if (runesAdded.modifier().rune() == NARROW_RUNE.get()) {
                        radius--;
                    }
                    Player player = event.getEntity();
                    float destroyProgress = level.getBlockState(pos).getDestroyProgress(player, level, pos) * (float) (1 + miningDuration) * 10f;
                    Pair<BlockPos, BlockPos> posPair = getStartEndBreakPositions(pos, player, radius);
                    Iterable<BlockPos> breakingPositions = BlockPos.betweenClosed(posPair.getFirst(), posPair.getSecond());
                    
                    if (event.getAction().equals(PlayerInteractEvent.LeftClickBlock.Action.START)) {
                        // Both buses
                        onStartHammering(level, pos);
                    } else if (event.getAction().equals(PlayerInteractEvent.LeftClickBlock.Action.STOP) || (event.getAction().equals(PlayerInteractEvent.LeftClickBlock.Action.ABORT))) {
                        // Server only
                        onStopHammering(player, level, pos, breakingPositions);
                    } else if (event.getAction().equals(PlayerInteractEvent.LeftClickBlock.Action.CLIENT_HOLD)) {
                        // Client only
                        onContinueHammering(player, level, destroyProgress, pos, breakingPositions);
                    }
                }
            }
        }
    }
    
    /**
     * Fired on both buses when the player depresses the attack key while targeting a block.
     */
    private static void onStartHammering(Level level, BlockPos center) {
        if (level.isClientSide()) {
            miningDuration = 0;
            targetedForBreaking = center;
        }
    }
    
    /**
     * Fired on the server when the player stops attacking a block.
     */
    private static void onStopHammering(Player player, Level level, BlockPos center, Iterable<BlockPos> breakingPositions) {
        // These are secretly MutableBlockPos
        breakingPositions.forEach(position -> {
            if (!position.equals(center) && !level.getBlockState(position).isAir()) {
                player.level().destroyBlockProgress(player.getId() + position.hashCode(), position.immutable(), -1);
            }
        });
    }
    
    /**
     * Fired on the client when the player holds the attack key on a block.
     */
    private static void onContinueHammering(Player player, Level level, float destroyProgress, BlockPos targetPos, Iterable<BlockPos> breakingPositions) {
        // Handle player aim wandering
        if (targetedForBreaking.equals(targetPos)) {
            miningDuration++;
        } else {
            miningDuration = 0;
            targetedForBreaking = targetPos;
        }
        // Update breaking status
        // These are secretly MutableBlockPos
        breakingPositions.forEach(position -> {
            if (targetedForBreaking != position && !level.getBlockState(position).isAir()) {
                if (level.isClientSide()) {
                    level.destroyBlockProgress(player.getId() + position.hashCode(), position.immutable(), (int) destroyProgress);
                } else {
                    ((ServerPlayer) player).connection.send(new ClientboundBlockDestructionPacket(player.getId() + position.hashCode(), position.immutable(), -1));
                }
            }
        });
        
    }
    
    @SubscribeEvent
    static void onMobSpawn(FinalizeSpawnEvent event) {
        if (RSServerConfig.spawnRunicMobs) {
            Mob toSpawn = event.getEntity();
            ServerLevel level = event.getLevel().getLevel();
            if (level.random.nextInt(RSServerConfig.runicMobPeriod) < 1 && !RSServerConfig.runicMobBiomeBlacklist.contains(level.getBiome(toSpawn.blockPosition()).value())) {
                EntityType<?> type = toSpawn.getType();
                if (type == ZOMBIE) {
                    int random = level.random.nextInt(3);
                    if (random < 2) {
                        makeRobust(toSpawn, level, "Robust Zombie");
                    } else {
                        makePoisonous(toSpawn, level, "Poisonous Zombie");
                        toSpawn.setData(POISONOUS, true);
                    }
                } else if (type == SKELETON && level.dimension() == Level.OVERWORLD) {
                    if (level.random.nextInt(1) < 1) {
                        makeLucky(toSpawn, level, "Lucky Skeleton");
                    } else {
                        makeRapidlyFiring(toSpawn, level, "Skeletal Quickshot");
                    }
                }  else if (type == WITHER_SKELETON) {
                    if (level.random.nextInt(1) < 1) {
                        makeInverted(toSpawn, level, "Inverted Skeleton");
                    }
                } else if (type == BLAZE && level.dimension() == Level.NETHER) {
                    if (level.random.nextInt(1) < 2) {
                        makeInflamed(toSpawn, level, "Inflamed Blaze");
                    }
                } else if (type == BREEZE) {
                    if (level.random.nextInt(1) < 2) {
                        makeGale(toSpawn, level, "Gale Force Breeze");
                    }
                } else if (type == CREEPER) {
                    if (level.random.nextInt(1) < 2) {
                        makeBlastProof(toSpawn, level, "Blast Proof Creeper");
                    }
                } else if (type == DROWNED) {
                    if (level.random.nextInt(1) < 2) {
                        makeAquatic(toSpawn, level, "Aquatic Drowned");
                    }
                } else if (type == SLIME) {
                    if (level.random.nextInt(1) < 2) {
                        makeGiant(toSpawn, level, "Giant Slime");
                    }
                } else if (type == PIGLIN_BRUTE) {
                    if (level.random.nextInt(1) < 2) {
                        makeRavenous(toSpawn, level, "Ravenous Piglin");
                    }
                } else if (type == GHAST) {
                    if (level.random.nextInt(1) < 2) {
                        makeFarsighted(toSpawn, level, "Far Sighted Ghast");
                    }
                } else if (type == VILLAGER) {
                    if (level.random.nextInt(1) < 2) {
                        makeLucky(toSpawn, level, "Lucky Villager");
                    }
                } else if (type == VINDICATOR) {
                    if (level.random.nextInt(1) < 2) {
                        makeTiny(toSpawn, level, "Tiny Axeman");
                    }
                } else if (type == ENDERMAN && level.dimension() == Level.END) {
                    if (level.random.nextInt(1) < 2) {
                        makeVoidInfused(toSpawn, level, "Void Fused Enderman");
                    }
                    // Doesn't seem to work on mobs spawned from structures, like vanilla shulkers
                } else if (type == SHULKER && level.dimension() == Level.END) {
                    if (level.random.nextInt(1) < 2) {
                        makeRadiant(toSpawn, level, "Radiance In a Box");
                    }
                }
            }
        }
    }
    
    private static void makeInverted(Mob mob, ServerLevel level, String customName) {
        String name = prepareEnhancements(customName, INVERTED, mob);
        safeAddPermanentModifier(mob, Attributes.MAX_HEALTH, name, 3f, ADD_VALUE);
        mob.setHealth(mob.getMaxHealth());;
    }
    
    private static void makeTiny(Mob mob, ServerLevel ignoredLevel, String customName) {
        String name = prepareEnhancements(customName, TINY, mob);
        safeAddPermanentModifier(mob, Attributes.MAX_HEALTH, name, -1f / 3f, ADD_MULTIPLIED_TOTAL);
        safeAddPermanentModifier(mob, Attributes.MOVEMENT_SPEED, name, 0.1f, ADD_VALUE);
    }
    
    static void makeRobust(Mob mob, ServerLevel level, String customName) {
        String name = prepareEnhancements(customName, ROBUST, mob);
        safeAddPermanentModifier(mob, Attributes.ATTACK_DAMAGE, name, level.getDifficulty().getId() + 3, ADD_VALUE);
        safeAddPermanentModifier(mob, Attributes.ARMOR_TOUGHNESS, name, level.getDifficulty().getId() + 3, ADD_VALUE);
        safeAddPermanentModifier(mob, Attributes.ARMOR, name, 2 * level.getDifficulty().getId() + 3, ADD_VALUE);
        safeAddPermanentModifier(mob, Attributes.KNOCKBACK_RESISTANCE, name, level.getDifficulty().getId() + 3, ADD_VALUE);
        safeAddPermanentModifier(mob, Attributes.ATTACK_KNOCKBACK, name, level.getDifficulty().getId() + 3, ADD_VALUE);
        safeAddPermanentModifier(mob, Attributes.MAX_HEALTH, name, 20, ADD_VALUE);
        mob.setHealth(mob.getMaxHealth());
        safeAddPermanentModifier(mob, Attributes.SCALE, name, (float) level.getDifficulty().getId() * 0.1f + 0.5f, ADD_MULTIPLIED_BASE);
    }
    
    static void makePoisonous(Mob mob, ServerLevel ignoredLevel, String customName) {
        prepareEnhancements(customName, POISONOUS, mob);
        mob.addEffect(new MobEffectInstance(MobEffects.REGENERATION, -1));
        mob.addEffect(new MobEffectInstance(MobEffects.POISON, -1));
    }
    
    static void makeLucky(Mob mob, ServerLevel ignoredLevel, String customName) {
        prepareEnhancements(customName, LUCKY, mob);
    }
    
    static void makeInflamed(Mob mob, ServerLevel level, String customName) {
        String name = prepareEnhancements(customName, INFLAMED_BLAZE, mob);
        safeAddPermanentModifier(mob, Attributes.ATTACK_KNOCKBACK, name, level.getDifficulty().getId() + 1f, ADD_VALUE);
        safeAddPermanentModifier(mob, Attributes.SCALE, name, 1f, ADD_MULTIPLIED_TOTAL);
        safeAddPermanentModifier(mob, Attributes.MAX_HEALTH, name, level.getDifficulty().getId() * 3f + 1f, ADD_VALUE);
        mob.setHealth(mob.getMaxHealth());
    }
    
    static void makeGale(Mob mob, ServerLevel level, String customName) {
        String name = prepareEnhancements(customName, GALE_BREEZE, mob);
        safeAddPermanentModifier(mob, Attributes.ATTACK_KNOCKBACK, name, level.getDifficulty().getId() + 1f, ADD_VALUE);
        safeAddPermanentModifier(mob, Attributes.SCALE, name, 1f, ADD_MULTIPLIED_TOTAL);
        safeAddPermanentModifier(mob, Attributes.MAX_HEALTH, name, level.getDifficulty().getId() * 3f + 1f, ADD_VALUE);
        mob.setHealth(mob.getMaxHealth());
    }
    
    static void makeBlastProof(Mob mob, ServerLevel ignoredLevel, String customName) {
        prepareEnhancements(customName, BLAST_PROOF, mob);
    }
    
    static void makeAquatic(Mob mob, ServerLevel level, String customName) {
        String name = prepareEnhancements(customName, AQUATIC, mob);
        safeAddPermanentModifier(mob, Attributes.WATER_MOVEMENT_EFFICIENCY, name, level.getDifficulty().getId() * 0.01f + 0.03f, ADD_MULTIPLIED_BASE);
        safeAddPermanentModifier(mob, Attributes.OXYGEN_BONUS, name, level.getDifficulty().getId() + 1f, ADD_MULTIPLIED_BASE);
        safeAddPermanentModifier(mob, NeoForgeMod.SWIM_SPEED, name, level.getDifficulty().getId() * 0.1f + 0.03f, ADD_MULTIPLIED_BASE);
    }
    
    static void makeGiant(Mob mob, ServerLevel level, String customName) {
        String name = prepareEnhancements(customName, RSAttachments.GIANT, mob);
        safeAddPermanentModifier(mob, Attributes.MAX_HEALTH, name, 2 * level.getDifficulty().getId() + 3, ADD_MULTIPLIED_TOTAL);
        mob.setHealth(mob.getMaxHealth());
        safeAddPermanentModifier(mob, Attributes.SCALE, name, level.getDifficulty().getId() * 0.1f + 0.05f, ADD_MULTIPLIED_BASE);
    }
    
    private static void makeRapidlyFiring(Mob mob, ServerLevel level, String customName) {
        String name = prepareEnhancements(customName, RAPIDLY_FIRING, mob);
        // Probably doesn't do anything. See mixins for actual implementation
        safeAddPermanentModifier(mob, Attributes.ATTACK_SPEED, name, level.getDifficulty().getId() + 3, ADD_MULTIPLIED_TOTAL);
    }
    
    static void makeRavenous(Mob mob, ServerLevel ignoredLevel, String customName) {
        prepareEnhancements(customName, RAVENOUS, mob);
    }
    
    static void makeFarsighted(Mob mob, ServerLevel level, String customName) {
        String name = prepareEnhancements(customName, FAR_SIGHTED, mob);
        safeAddPermanentModifier(mob, Attributes.FOLLOW_RANGE, name, level.getDifficulty().getId() + 3, ADD_MULTIPLIED_BASE);
    }
    
    static void makeVoidInfused(Mob mob, ServerLevel level, String customName) {
        String name = prepareEnhancements(customName, VOID_FUSED, mob);
        safeAddPermanentModifier(mob, Attributes.SCALE, name, level.getDifficulty().getId() * 0.1f, ADD_MULTIPLIED_BASE);
        safeAddPermanentModifier(mob, Attributes.MAX_HEALTH, name, level.getDifficulty().getId() + 1, ADD_MULTIPLIED_BASE);
        mob.setHealth(mob.getMaxHealth());
        safeAddPermanentModifier(mob, Attributes.MOVEMENT_SPEED, name, level.getDifficulty().getId() * 0.01f + 0.03f, ADD_VALUE);
    }
    
    static void makeRadiant(Mob mob, ServerLevel ignoredLevel, String customName) {
        prepareEnhancements(customName, RADIANT, mob);
    }
    
    static String prepareEnhancements(String customName, Supplier<AttachmentType<Boolean>> attachment, Mob mob) {
        mob.setData(attachment, true);
        mob.setCustomName(Component.literal(customName));
        mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, -1));
        
        // Theoretically better to hardcode or buffer this, but who has time for that?
        return customName.toLowerCase().replace(' ', '_');
    }
    
    public static void safeAddPermanentModifier(LivingEntity entity, Holder<Attribute> attribute, String name, float amount, AttributeModifier.Operation operation) {
        AttributeInstance attributeInstance = entity.getAttribute(attribute);
        if (null != attributeInstance) {
            attributeInstance.addOrReplacePermanentModifier(new AttributeModifier(
                    ResourceLocation.fromNamespaceAndPath(MODID, name),
                    amount,
                    operation)
            );
        }
    }
    
    
}
