package com.github.no_name_provided.nnp_rune_smithing.common;

import com.github.no_name_provided.nnp_rune_smithing.common.capabilities.CastingTableCapability;
import com.github.no_name_provided.nnp_rune_smithing.common.capabilities.MelterCapability;
import com.github.no_name_provided.nnp_rune_smithing.common.data_components.RuneData;
import com.github.no_name_provided.nnp_rune_smithing.common.data_components.RunesAdded;
import com.github.no_name_provided.nnp_rune_smithing.common.entities.RSEntities;
import com.github.no_name_provided.nnp_rune_smithing.common.items.runes.AbstractRuneItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;

import java.util.ArrayList;

import static com.github.no_name_provided.nnp_rune_smithing.common.RSAttributeModifiers.WARD_RUNE_ABSORPTION;
import static com.github.no_name_provided.nnp_rune_smithing.common.RSAttributeModifiers.wardRuneAbsorption;
import static com.github.no_name_provided.nnp_rune_smithing.common.data_components.RSDataComponents.RUNES_ADDED;
import static com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems.WARD_RUNE;
import static com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems.WIELD_RUNE;

@EventBusSubscriber
public class Events {
    
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
                if (!oldRunes.equals(RunesAdded.DEFAULT.get())) {
                    if (oldRunes.target().rune() == WIELD_RUNE.get()) {
                        AbstractRuneItem rune = oldRunes.effect().rune();
                        if (rune == WARD_RUNE.get()) {
                            absorptionChange -= absorptionPerTier * oldRunes.effectiveTier();
                        }
                    }
                }
                if (!newRunes.equals(RunesAdded.DEFAULT.get())) {
                    if (newRunes.target().rune() == WIELD_RUNE.get()) {
                        AbstractRuneItem rune = newRunes.effect().rune();
                        if (rune == WARD_RUNE.get()) {
                            absorptionChange += absorptionPerTier * newRunes.effectiveTier();
                        }
                    }
                }
                AttributeMap attributes = player.getAttributes();
                AttributeInstance absorption = attributes.getInstance(Attributes.MAX_ABSORPTION);
                if (null != absorption) {
                    // Not using convenience method here because IDE tells me it's unsafe, and Minecraft has a
                    // remarkable way of nullifying things
                    AttributeModifier oldModifier = absorption.getModifier(WARD_RUNE_ABSORPTION);
                    if (null == oldModifier) {
                        absorption.addTransientModifier(wardRuneAbsorption(absorptionChange));
                        player.setAbsorptionAmount(player.getAbsorptionAmount() + (float)absorptionChange);
                    } else if (!(oldModifier.amount() + (float) absorptionChange > 0)) {
                        absorption.removeModifier(WARD_RUNE_ABSORPTION);
                    } else {
                        absorption.addOrUpdateTransientModifier(wardRuneAbsorption(oldModifier.amount() + (float)absorptionChange));
                        player.setAbsorptionAmount(player.getAbsorptionAmount() + (float)absorptionChange);
                    }
                }
                
                // Works, but is inefficient and the absorption hearts setting can be cheesed
//                ArrayList<RunesAdded> runes = new ArrayList<>(4);
//                for (ItemStack armor : player.getArmorAndBodyArmorSlots()) {
//                    RunesAdded rune = armor.get(RUNES_ADDED);
//                    if (null != rune) {
//                        runes.add(rune);
//                    }
//                }
//                double absorptionBuff = 0;
//                for (RunesAdded rune : runes) {
//                    if (rune.target().rune() == WIELD_RUNE.get() && rune.effect().rune() == WARD_RUNE.get()) {
//                        absorptionBuff += rune.effectiveTier() * 1.0f;
//                    }
//                }
//                AttributeInstance absorptionAttribute = player.getAttributes().getInstance(Attributes.MAX_ABSORPTION);
//                if (null != absorptionAttribute) {
//                    if (absorptionBuff > 0) {
//                        absorptionAttribute.addOrUpdateTransientModifier(wardRuneAbsorption(absorptionBuff));
//                        // Placeholder, to avoid overwriting other modifiers effect. Really need to take a real
//                        // crack at calculating change (and not just total)
//                        player.setAbsorptionAmount(player.getMaxAbsorption());
//                    } else {
//                        absorptionAttribute.removeModifier(WARD_RUNE_ABSORPTION);
//                    }
//                }
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
}
