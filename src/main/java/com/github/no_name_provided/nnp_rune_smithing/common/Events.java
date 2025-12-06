package com.github.no_name_provided.nnp_rune_smithing.common;

import com.github.no_name_provided.nnp_rune_smithing.common.blocks.RuneBlock;
import com.github.no_name_provided.nnp_rune_smithing.common.capabilities.CastingTableCapability;
import com.github.no_name_provided.nnp_rune_smithing.common.capabilities.MelterCapability;
import com.github.no_name_provided.nnp_rune_smithing.common.data_components.RunesAdded;
import com.github.no_name_provided.nnp_rune_smithing.common.entities.RSEntities;
import com.github.no_name_provided.nnp_rune_smithing.common.items.runes.AbstractRuneItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.entity.living.ArmorHurtEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;

import java.util.List;
import java.util.Map;

import static com.github.no_name_provided.nnp_rune_smithing.common.RSAttributeModifiers.*;
import static com.github.no_name_provided.nnp_rune_smithing.common.data_components.RSDataComponents.RUNES_ADDED;
import static com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems.*;
import static net.minecraft.world.entity.projectile.windcharge.AbstractWindCharge.EXPLOSION_DAMAGE_CALCULATOR;

@EventBusSubscriber
public class Events {
    
    @SubscribeEvent
    static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(
                () -> RuneBlock.effectToColor.putAll(Map.of(
                                WARD_RUNE.get(), List.of(100, 200, 50),
                                AIR_RUNE.get(), List.of(100, 200, 50),
                                WATER_RUNE.get(), List.of(200, 100, 50),
                                FIRE_RUNE.get(), List.of(100, 200, 50),
                                EARTH_RUNE.get(), List.of(100, 200, 50)
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
                float absorptionPerTier = 1.0f;
                double speedChange = 0;
                float speedPerTier = 0.05f;
                double underwaterMiningSpeedChange = 0;
                float underwaterMiningSpeedPerTier = 1.0f;
                double extraAirChange = 0;
                float extraAirPerTier = 1.0f;
                double waterSpeedChange = 0;
                float extraWaterSpeedPerTier = 0.5f;
                double healthChange = 0;
                float healthPerTier = 1.0f;
                double burnTimeMultChange = 0;
                float burnTimeMultPerTier = -0.2f;
                if (!oldRunes.equals(RunesAdded.DEFAULT.get())) {
                    if (oldRunes.target().rune() == WIELD_RUNE.get()) {
                        AbstractRuneItem rune = oldRunes.effect().rune();
                        if (rune == WARD_RUNE.get()) {
                            absorptionChange -= absorptionPerTier * oldRunes.effectiveTier();
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
    static void onLivingDamagePre(LivingDamageEvent.Pre event) {
        DamageSource source = event.getSource();
        if (source.isDirect()) {
            // Handle attacker using runic weapon
            ItemStack weapon = source.getWeaponItem();
            if (null != weapon) {
                RunesAdded runesAdded = weapon.get(RUNES_ADDED);
                if (null != runesAdded && runesAdded.effectiveTier() > 0) {
                    DamageContainer container = event.getContainer();
                    
                    // Collisions
                    if (runesAdded.target().rune() == COLLISION_RUNE.get()) {
                        AbstractRuneItem effect = runesAdded.effect().rune();
                        if (effect == EARTH_RUNE.get()) {
                            container.addModifier(
                                    DamageContainer.Reduction.ARMOR,
                                    (con, old) -> old / runesAdded.effectiveTier()
                            );
                        }
                    }
                    
                    // Wield effects
                    if (runesAdded.target().rune() == WIELD_RUNE.get()) {
                        AbstractRuneItem effect = runesAdded.effect().rune();
                        if (effect == FIRE_RUNE.get()) {
                            // Consider moving this to incoming damage, so armor effects apply
                            container.setNewDamage(container.getNewDamage() + runesAdded.effectiveTier());
                        }
                    }
                }
            }
        }
    }
    
    @SubscribeEvent
    static void onLivingDamagePost(LivingDamageEvent.Pre event) {
        DamageSource source = event.getSource();
        if (source.isDirect()) {
            LivingEntity attacked = event.getEntity();
            
            // Handle attacker using runic weapon
            ItemStack weapon = source.getWeaponItem();
            if (null != weapon) {
                RunesAdded runesAdded = weapon.get(RUNES_ADDED);
                if (null != runesAdded && runesAdded.effectiveTier() > 0) {
                    
                    // Collisions
                    if (runesAdded.target().rune() == COLLISION_RUNE.get()) {
                        AbstractRuneItem effect = runesAdded.effect().rune();
                        if (effect == WARD_RUNE.get()) {
                            attacked.addEffect(
                                    new MobEffectInstance(
                                            MobEffects.ABSORPTION,
                                            20 * 60 * runesAdded.effectiveTier(),
                                            runesAdded.effectiveTier()
                                    )
                            );
                        } else if (effect == FIRE_RUNE.get() && !attacked.fireImmune()) {
                            attacked.setRemainingFireTicks(20 * 10 * runesAdded.effectiveTier());
                        } else if (effect == WATER_RUNE.get()) {
                            attacked.addEffect(
                                    new MobEffectInstance(
                                            MobEffects.WATER_BREATHING,
                                            20 * 60 * runesAdded.effectiveTier(),
                                            runesAdded.effectiveTier()
                                    )
                            );
                        }
                    }
                }
            }
            
            // Handle attacked having runic armor
            attacked.getArmorSlots().forEach(armor -> {
                RunesAdded runesAdded = armor.get(RUNES_ADDED);
                if (null != runesAdded) {
                    int tier = runesAdded.effectiveTier();
                    if (runesAdded.target().rune() == COLLISION_RUNE.get()) {
                        Entity attacker = source.getEntity();
                        if (null != attacker) {
                            AbstractRuneItem effect = runesAdded.effect().rune();
                            if (effect == WARD_RUNE.get() && attacker instanceof LivingEntity livingAttacker) {
                                livingAttacker.addEffect(
                                        new MobEffectInstance(
                                                MobEffects.ABSORPTION,
                                                20 * 60 * runesAdded.effectiveTier(),
                                                runesAdded.effectiveTier()
                                        )
                                );
                            } else if (effect == AIR_RUNE.get() && attacked.level() instanceof Level level) {
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
                            } else if (effect == WATER_RUNE.get() && attacker.canFreeze()) {
                                attacker.setTicksFrozen(attacker.getTicksRequiredToFreeze());
                            } else if (effect == FIRE_RUNE.get() && !attacker.fireImmune()) {
                                attacker.setRemainingFireTicks(20 * 10 * runesAdded.effectiveTier());
                            } else if (effect == EARTH_RUNE.get() && attacker instanceof LivingEntity livingAttacker) {
                                livingAttacker.addEffect(
                                        new MobEffectInstance(
                                                MobEffects.POISON,
                                                10,
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
}
