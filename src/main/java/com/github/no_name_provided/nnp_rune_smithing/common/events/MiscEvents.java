package com.github.no_name_provided.nnp_rune_smithing.common.events;

import com.github.no_name_provided.nnp_rune_smithing.client.particles.RSParticleTypes;
import com.github.no_name_provided.nnp_rune_smithing.common.RSAttributeModifiers;
import com.github.no_name_provided.nnp_rune_smithing.common.RSServerConfig;
import com.github.no_name_provided.nnp_rune_smithing.common.attachments.RSAttachments;
import com.github.no_name_provided.nnp_rune_smithing.common.blocks.RuneBlock;
import com.github.no_name_provided.nnp_rune_smithing.common.capabilities.AlloyerCapability;
import com.github.no_name_provided.nnp_rune_smithing.common.capabilities.CastingTableCapability;
import com.github.no_name_provided.nnp_rune_smithing.common.capabilities.MelterCapability;
import com.github.no_name_provided.nnp_rune_smithing.common.data_components.RunesAdded;
import com.github.no_name_provided.nnp_rune_smithing.common.entities.RSEntities;
import com.github.no_name_provided.nnp_rune_smithing.common.items.runes.AbstractRuneItem;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.enchanting.GetEnchantmentLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;
import static com.github.no_name_provided.nnp_rune_smithing.common.RSAttributeModifiers.*;
import static com.github.no_name_provided.nnp_rune_smithing.common.attachments.RSAttachments.VOID_CONSUMES_DEBUFFS;
import static com.github.no_name_provided.nnp_rune_smithing.common.data_components.RSDataComponents.RUNES_ADDED;
import static com.github.no_name_provided.nnp_rune_smithing.common.datamaps.RSDataMaps.CASTABLE_FLUID_DATA;
import static com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems.*;

@EventBusSubscriber(modid = MODID)
public class MiscEvents {
    
    /**
     * Miscellaneous code that needs to run once, fairly early in startup but after most thing are defined.
     */
    @SubscribeEvent
    static void onCommonSetup(FMLCommonSetupEvent event) {
        // Set the particle color for each effect rune
        // Why not initialize during declaration? That's bizarrely hard (and strangely inconsistent) with Java collections
        event.enqueueWork(
                () -> RuneBlock.effectToColor.putAll(Map.of(
                                WARD_RUNE.get(), List.of(140, 173, 171),
                                SIGHT_RUNE.get(), List.of(220, 243, 255),
                                SERENDIPITY_RUNE.get(), List.of(30, 66, 133),
                                
                                AIR_RUNE.get(), List.of(196, 236, 255),
                                WATER_RUNE.get(), List.of(66, 170, 217),
                                FIRE_RUNE.get(), List.of(236, 24, 35),
                                EARTH_RUNE.get(), List.of(175, 88, 47),
                                
                                VOID_RUNE.get(), List.of(189, 135, 255),
                                LIGHT_RUNE.get(), List.of(238, 255, 61)
                        )
                )
        );
    }
    
    @SubscribeEvent
    static void onPlayerTickPre(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        if (player.getExistingData(RSAttachments.MAGNETIC).orElse((byte) 0) > (byte) 0) {
            Level level = player.level();
            level.getEntities(
                    EntityTypeTest.forClass(ItemEntity.class),
                    player.getBoundingBox().inflate(5),
                    a -> !a.hasPickUpDelay()).forEach(entity -> {
                        entity.playerTouch(player);
                        if (!level.isClientSide()) {
                            ((ServerLevel) level).sendParticles(
                                    ColorParticleOption.create(RSParticleTypes.SELF_RUNE.get(), FastColor.ARGB32.color(255, MapColor.TERRACOTTA_LIGHT_BLUE.col)),
                                    entity.position().x(),
                                    entity.position().y() + 0.8,
                                    entity.position().z(),
                                    30,
                                    0.15,
                                    0.15,
                                    0.15,
                                    0.02
                            );
                        }
                    }
            );
        }
    }
    
    /**
     * Updates player modifiers and abilities when they (un)equip armor. Can be expanded to include ItemStacks in other
     * equipment slots (shield, main hand, body).
     * <p>
     * May assume server side.
     * </p>
     */
    @SubscribeEvent
    static void onEquipmentChanged(LivingEquipmentChangeEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (event.getSlot().getType() == EquipmentSlot.Type.HAND) {
                RunesAdded oldRunes = event.getFrom().getOrDefault(RUNES_ADDED, RunesAdded.DEFAULT.get());
                RunesAdded newRunes = event.getTo().getOrDefault(RUNES_ADDED, RunesAdded.DEFAULT.get());
                if (newRunes.equals(oldRunes)) {
                    
                    return;
                }
                double attackSpeedChange = 0;
                float attackSpeedPerTier = RSServerConfig.attackSpeedPerTier;
                byte magneticCount = (byte) 0;
                if (!oldRunes.equals(RunesAdded.DEFAULT.get()) && oldRunes.amplifier().rune() != CONTAIN_RUNE.get()) {
                    if (oldRunes.target().rune() == WIELD_RUNE.get()) {
                        AbstractRuneItem rune = oldRunes.effect().rune();
                        if (rune == AIR_RUNE.get()) {
                            attackSpeedChange -= attackSpeedPerTier * oldRunes.effectiveTier();
                        }
                    } else if (oldRunes.target().rune() == SELF_RUNE.get()) {
                        if (oldRunes.effect().rune() == AIR_RUNE.get()) {
                            magneticCount--;
                        }
                    }
                    
                }
                if (!newRunes.equals(RunesAdded.DEFAULT.get()) && newRunes.amplifier().rune() != CONTAIN_RUNE.get()) {
                    if (newRunes.target().rune() == WIELD_RUNE.get()) {
                        AbstractRuneItem rune = newRunes.effect().rune();
                        if (rune == AIR_RUNE.get()) {
                            attackSpeedChange += attackSpeedPerTier * newRunes.effectiveTier();
                        }
                    } else if (newRunes.target().rune() == SELF_RUNE.get()) {
                        if (newRunes.effect().rune() == AIR_RUNE.get()) {
                            magneticCount++;
                        }
                    }
                }
                
                updateAttribute(attackSpeedChange, player, RSAttributeModifiers::airRuneAttackSpeedChange, AIR_RUNE_ATTACK_SPEED, Attributes.ATTACK_SPEED);
                player.setData(RSAttachments.MAGNETIC, magneticCount);
            } else if (event.getSlot().isArmor()) {
                RunesAdded oldRunes = event.getFrom().getOrDefault(RUNES_ADDED, RunesAdded.DEFAULT.get());
                RunesAdded newRunes = event.getTo().getOrDefault(RUNES_ADDED, RunesAdded.DEFAULT.get());
                if (newRunes.equals(oldRunes)) {
                    
                    return;
                }
                // Docs say double, instance constructor takes double, value retrieved/set is float?
                double luckChange = 0;
                float luckPerTier = RSServerConfig.luckPerTier;
                double absorptionChange = 0;
                float absorptionPerTier = RSServerConfig.absorptionPerTier;
                double speedChange = 0;
                float speedPerTier = RSServerConfig.speedPerTier;
                double safeFallDistanceChange = 0;
                float safeFallDistanceChangePerTier = RSServerConfig.safeFallDistancePerTier;
                double jumpStrengthChange = 0;
                float jumpStrengthChangePerTier = RSServerConfig.jumpStrengthPerTier;
                double underwaterMiningSpeedChange = 0;
                float underwaterMiningSpeedPerTier = RSServerConfig.underwaterMiningSpeedPerTier;
                double extraAirChange = 0;
                float extraAirPerTier = RSServerConfig.extraAirPerTier;
                double waterSpeedChange = 0;
                float extraWaterSpeedPerTier = RSServerConfig.extraWaterSpeedPerTier;
                double healthChange = 0;
                float healthPerTier = RSServerConfig.healthPerTier;
                double strengthChange = 0;
                float strengthMultPerTier = RSServerConfig.strengthMultPerTier;
                double burnTimeMultChange = 0;
                float burnTimeMultPerTier = RSServerConfig.burnTimeMultPerTier;
                
                byte serendipityCount = player.getExistingData(RSAttachments.SERENDIPITY_COUNT).orElse((byte) 0);
                byte glowingCount = player.getExistingData(RSAttachments.GLOWING_FROM_RUNIC_ARMOR).orElse((byte) 0);
                byte coldResistanceCount = player.getExistingData(RSAttachments.COLD_RESISTANCE).orElse((byte) 0);
                byte magnetismCount = player.getExistingData(RSAttachments.MAGNETIC).orElse((byte) 0);
                byte voidRuneConsumeCount = player.getExistingData(RSAttachments.VOID_CONSUME_COUNT).orElse((byte) 0);
                byte voidRuneHideCount = player.getExistingData(RSAttachments.HIDDEN_BY_VOID_COUNT).orElse((byte) 0);
                double XPMultChange = 0;
                float XPMultPerTier = RSServerConfig.XPMultPerTier;
                double lightChange = 0;
                float lightChangePerTier = 3;
                
                if (!oldRunes.equals(RunesAdded.DEFAULT.get()) && oldRunes.amplifier().rune() != CONTAIN_RUNE.get()) {
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
                        } else if (rune == SERENDIPITY_RUNE.get()) {
                            luckChange += luckPerTier * newRunes.effectiveTier();
                        } else if (rune == AIR_RUNE.get()) {
                            speedChange -= speedPerTier * oldRunes.effectiveTier();
                            if (oldRunes.amplifier().rune() == AMPLIFY_RUNE.get()) {
                                safeFallDistanceChange -= safeFallDistanceChangePerTier * oldRunes.effectiveTier();
                                jumpStrengthChange -= jumpStrengthChangePerTier * oldRunes.effectiveTier();
                            }
                        } else if (rune == WATER_RUNE.get()) {
                            underwaterMiningSpeedChange -= underwaterMiningSpeedPerTier * oldRunes.effectiveTier();
                            extraAirChange -= extraAirPerTier * oldRunes.effectiveTier();
                            waterSpeedChange -= extraWaterSpeedPerTier * oldRunes.effectiveTier();
                        } else if (rune == EARTH_RUNE.get()) {
                            healthChange -= healthPerTier * oldRunes.effectiveTier();
                        } else if (rune == FIRE_RUNE.get()) {
                            strengthChange -= strengthMultPerTier * oldRunes.effectiveTier();
                        } else if (rune == VOID_RUNE.get()) {
                            voidRuneConsumeCount--;
                        } else if (rune == LIGHT_RUNE.get()) {
                            XPMultChange -= XPMultPerTier * oldRunes.effectiveTier();
                        }
                    } else if (oldRunes.target().rune() == SELF_RUNE.get()) {
                        AbstractRuneItem rune = oldRunes.effect().rune();
                        if (rune == SERENDIPITY_RUNE.get()) {
                            serendipityCount--;
                        } else if (rune == SIGHT_RUNE.get()) {
                            glowingCount--;
                        } else if (rune == WATER_RUNE.get()) {
                            coldResistanceCount--;
                        } else if (rune == AIR_RUNE.get()) {
                            magnetismCount--;
                        } else if (rune == FIRE_RUNE.get()) {
                            burnTimeMultChange -= burnTimeMultPerTier * oldRunes.effectiveTier();
                        }
                        if (rune == VOID_RUNE.get()) {
                            voidRuneHideCount--;
                        } else if (rune == LIGHT_RUNE.get()) {
                            lightChange -= lightChangePerTier * oldRunes.effectiveTier();
                        }
                    }
                }
                if (!newRunes.equals(RunesAdded.DEFAULT.get()) && newRunes.amplifier().rune() != CONTAIN_RUNE.get()) {
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
                        } else if (rune == SERENDIPITY_RUNE.get()) {
                            luckChange += luckPerTier * newRunes.effectiveTier();
                        } else if (rune == AIR_RUNE.get()) {
                            speedChange += speedPerTier * newRunes.effectiveTier();
                            if (newRunes.amplifier().rune() == AMPLIFY_RUNE.get()) {
                                safeFallDistanceChange += safeFallDistanceChangePerTier * newRunes.effectiveTier();
                                jumpStrengthChange += jumpStrengthChangePerTier * newRunes.effectiveTier();
                            }
                        } else if (rune == WATER_RUNE.get()) {
                            underwaterMiningSpeedChange += underwaterMiningSpeedPerTier * newRunes.effectiveTier();
                            extraAirChange += extraAirPerTier * newRunes.effectiveTier();
                            waterSpeedChange += extraWaterSpeedPerTier * newRunes.effectiveTier();
                        } else if (rune == EARTH_RUNE.get()) {
                            healthChange += healthPerTier * newRunes.effectiveTier();
                        } else if (rune == FIRE_RUNE.get()) {
                            strengthChange += strengthMultPerTier * newRunes.effectiveTier();
                        } else if (rune == VOID_RUNE.get()) {
                            voidRuneConsumeCount++;
                        } else if (rune == LIGHT_RUNE.get()) {
                            XPMultChange += XPMultPerTier * newRunes.effectiveTier();
                        }
                    } else if (newRunes.target().rune() == SELF_RUNE.get()) {
                        AbstractRuneItem rune = newRunes.effect().rune();
                        if (rune == SERENDIPITY_RUNE.get()) {
                            serendipityCount++;
                        } else if (rune == SIGHT_RUNE.get()) {
                            glowingCount++;
                        } else if (rune == WATER_RUNE.get()) {
                            coldResistanceCount++;
                        } else if (rune == AIR_RUNE.get()) {
                            magnetismCount++;
                        } else if (rune == FIRE_RUNE.get()) {
                            burnTimeMultChange += burnTimeMultPerTier * newRunes.effectiveTier();
                        }
                        if (rune == VOID_RUNE.get()) {
                            voidRuneHideCount++;
                        } else if (rune == LIGHT_RUNE.get()) {
                            lightChange += lightChangePerTier * newRunes.effectiveTier();
                        }
                    }
                }
                updateAttribute(absorptionChange, player, RSAttributeModifiers::wardRuneAbsorption, WARD_RUNE_ABSORPTION, Attributes.MAX_ABSORPTION);
                updateAttribute(luckChange, player, RSAttributeModifiers::serendipityRuneSpeed, SERENDIPITY_RUNE_LUCK, Attributes.LUCK);
                updateAttribute(speedChange, player, RSAttributeModifiers::airRuneSpeed, AIR_RUNE_SPEED, Attributes.MOVEMENT_SPEED);
                updateAttribute(safeFallDistanceChange, player, RSAttributeModifiers::airRuneSafeHeight, AIR_RUNE_SAFE_HEIGHT, Attributes.SAFE_FALL_DISTANCE);
                updateAttribute(jumpStrengthChange, player, RSAttributeModifiers::airRuneJumpStrength, AIR_RUNE_JUMP_STRENGTH, Attributes.JUMP_STRENGTH);
                updateAttribute(underwaterMiningSpeedChange, player, RSAttributeModifiers::waterRuneUWMiningSpeed, WATER_RUNE_UW_MINING_SPEED, Attributes.SUBMERGED_MINING_SPEED);
                updateAttribute(extraAirChange, player, RSAttributeModifiers::waterRuneExtraAir, WATER_RUNE_EXTRA_AIR, Attributes.OXYGEN_BONUS);
                updateAttribute(waterSpeedChange, player, RSAttributeModifiers::waterRuneExtraSwimSpeed, WATER_RUNE_EXTRA_SWIM_SPEED, Attributes.WATER_MOVEMENT_EFFICIENCY);
                updateAttribute(healthChange, player, RSAttributeModifiers::earthRuneHealthChange, EARTH_RUNE_HEALTH, Attributes.MAX_HEALTH);
                player.setHealth((float) Mth.clamp(player.getHealth() + healthChange, 1, player.getMaxHealth()));
                updateAttribute(strengthChange, player, RSAttributeModifiers::fireRuneStrengthChange, FIRE_RUNE_STRENGTH, Attributes.ATTACK_DAMAGE);
                updateAttribute(burnTimeMultChange, player, RSAttributeModifiers::fireRuneBurnTimeMultChange, FIRE_RUNE_BURNING_TIME, Attributes.BURNING_TIME);
                
                
                player.setData(RSAttachments.SERENDIPITY_COUNT, serendipityCount);
                player.setData(RSAttachments.SERENDIPITOUS_BIPED, serendipityCount > 0);
                player.setData(RSAttachments.GLOWING_FROM_RUNIC_ARMOR, glowingCount);
                if (glowingCount > 0) {
                    player.addEffect(new MobEffectInstance(
                                    MobEffects.GLOWING,
                                    -1,
                                    1
                            )
                    );
                } else if (player.hasEffect(MobEffects.GLOWING)) {
                    player.removeEffect(MobEffects.GLOWING);
                }
                player.setData(RSAttachments.COLD_RESISTANCE, coldResistanceCount);
                player.setData(RSAttachments.MAGNETIC, magnetismCount);
                player.setData(RSAttachments.VOID_CONSUMES_DEBUFFS, voidRuneConsumeCount > 0);
                player.setData(RSAttachments.HIDDEN_BY_VOID, voidRuneHideCount > 0);
                player.setData(RSAttachments.PLAYER_XP_MULTIPLIER, player.getData(RSAttachments.PLAYER_XP_MULTIPLIER) + (float) XPMultChange);
                player.setData(RSAttachments.LIGHT_FROM_ARMOR, (byte) (player.getData(RSAttachments.LIGHT_FROM_ARMOR) + lightChange));
            }
        }
    }
    
    /**
     * Item stacks aren't editable in the anvil repair event, so we use this one.
     */
    @SubscribeEvent
    static void onAnvilUpdate(AnvilUpdateEvent event) {
        int damageReductionPerTier = 10;
        ItemStack input = event.getLeft();
        ItemStack oldOutput = event.getOutput();
        // Verify it's a successful repair recipe
        if (input.getItem() == oldOutput.getItem() && oldOutput.getDamageValue() < input.getDamageValue()) {
            RunesAdded runes = input.get(RUNES_ADDED);
            if (null != runes && runes.target().rune() == SELF_RUNE.get() && runes.effect().rune() == EARTH_RUNE.get() && runes.amplifier().rune() != CONTAIN_RUNE.get()) {
                ItemStack newOutput = oldOutput.copy();
                newOutput.setDamageValue(Mth.clamp(oldOutput.getDamageValue() - damageReductionPerTier * runes.effectiveTier(), 0, oldOutput.getDamageValue()));
                event.setOutput(newOutput);
            }
        }
    }
    
    /**
     * Handles things that need to be checked or updated each tick, but don't actually affect the tick itself. There may
     * be weird behavior if the tick is canceled. If so, move to pre.
     */
    @SubscribeEvent
    static void onEntityTickPost(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity entity && !entity.level().isClientSide() && (entity.level().getGameTime() % 100 == 4) && entity.isAffectedByPotions()) {
            if (entity.getExistingData(VOID_CONSUMES_DEBUFFS).orElse(false)) {
                Collection<MobEffectInstance> effects = entity.getActiveEffects();
                // Using iterator for extra safety when mutating collection - may be unnecessary
                //noinspection ForLoopReplaceableByForEach
                for (Iterator<MobEffectInstance> iterator = effects.iterator(); iterator.hasNext(); ) {
                    MobEffectInstance effect = iterator.next();
                    if (effect.getEffect().value().getCategory().equals(MobEffectCategory.HARMFUL)) {
                        entity.removeEffect(effect.getEffect());
                        break;
                    }
                }
            }
        }
    }
    
    @SubscribeEvent
    static void onGetEnchantmentLevel(GetEnchantmentLevelEvent event) {
        ItemStack tool = event.getStack();
        RunesAdded runes = tool.get(RUNES_ADDED);
        Holder<Enchantment> target = event.getTargetEnchant();
        if (null != runes && null != target) {
            if (runes.target().rune() == COLLISION_RUNE.get()) {
                if (runes.effect().rune() == SERENDIPITY_RUNE.get()) {
                    if (tool.supportsEnchantment(target) && (target.is(Enchantments.LOOTING) || target.is(Enchantments.FORTUNE))) {
                        int eLevel = event.getEnchantments().getLevel(target);
                        // Increase level (normal behavior) or decrease level (inverted behavior)
                        int newELevel = Mth.clamp(
                                runes.modifier().rune() != INVERT_RUNE.get() ? eLevel + runes.effectiveTier() : eLevel - runes.effectiveTier(),
                                0,
                                // Ignoring max levels appears to have no downside
                                100
                        );
                        event.getEnchantments().set(target, newELevel);
                    }
                }
            }
        }
    }
    
    /**
     * Tell block entity (types) which block(s) they're associated with.
     */
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
    
    /**
     * Register DataMap (types).
     */
    @SubscribeEvent
    static void onRegisterDataMaps(RegisterDataMapTypesEvent event) {
        event.register(CASTABLE_FLUID_DATA);
    }
}
