package com.github.no_name_provided.nnp_rune_smithing.common.events;

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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

import java.util.List;
import java.util.Map;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;
import static com.github.no_name_provided.nnp_rune_smithing.common.RSAttributeModifiers.*;
import static com.github.no_name_provided.nnp_rune_smithing.common.data_components.RSDataComponents.RUNES_ADDED;
import static com.github.no_name_provided.nnp_rune_smithing.common.datamaps.RSDataMaps.CASTABLE_FLUID_DATA;
import static com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems.*;

@EventBusSubscriber(modid = MODID)
public class MiscEvents {
    
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
                
                double XPMultChange = 0;
                float XPMultPerTier = RSServerConfig.XPMultPerTier;
                double lightChange = 0;
                float lightChangePerTier = 3;
                
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
                        } else if (rune == LIGHT_RUNE.get()) {
                            XPMultChange -= XPMultPerTier * oldRunes.effectiveTier();
                        }
                    } else if (oldRunes.target().rune() == SELF_RUNE.get()) {
                        if (oldRunes.effect().rune() == LIGHT_RUNE.get()) {
                            lightChange -= lightChangePerTier * oldRunes.effectiveTier();
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
                        } else if (rune == LIGHT_RUNE.get()) {
                            XPMultChange += XPMultPerTier * newRunes.effectiveTier();
                        }
                    } else if (newRunes.target().rune() == SELF_RUNE.get()) {
                        if (newRunes.effect().rune() == LIGHT_RUNE.get()) {
                            lightChange += lightChangePerTier * newRunes.effectiveTier();
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
                
                player.setData(RSAttachments.PLAYER_XP_MULTIPLIER, player.getData(RSAttachments.PLAYER_XP_MULTIPLIER) + (float) XPMultChange);
                player.setData(RSAttachments.LIGHT_FROM_ARMOR, (byte) (player.getData(RSAttachments.LIGHT_FROM_ARMOR) + lightChange));
            }
        }
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
    static void onRegisterDataMaps(RegisterDataMapTypesEvent event) {
        event.register(CASTABLE_FLUID_DATA);
    }
}
