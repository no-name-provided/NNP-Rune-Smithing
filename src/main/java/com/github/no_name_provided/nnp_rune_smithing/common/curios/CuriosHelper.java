package com.github.no_name_provided.nnp_rune_smithing.common.curios;

import com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.EffectCure;
import net.neoforged.neoforge.common.EffectCures;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

import java.util.Optional;

import static com.github.no_name_provided.nnp_rune_smithing.common.RSAttributeModifiers.*;

/**
 * Helper class to silo curios logic.
 * Only use this class if curios is present (gate behind checks to the mod list).
 */
public class CuriosHelper {
    
    public CuriosHelper(IEventBus modBus) {
        modBus.addListener(this::registerCapabilities);
    }
    
    public void registerCapabilities(final RegisterCapabilitiesEvent event) {
        event.registerItem(
                CuriosCapability.ITEM,
                (stack, context) -> new ICurio() {
                    
                    @Override
                    public ItemStack getStack() {
                        return stack;
                    }
                    
                    @Override
                    public boolean canEquipFromUse(SlotContext slotContext) {
                        return true;
                    }
                    
                    @Override
                    public void onEquip(SlotContext slotContext, ItemStack prevStack) {
                        AttributeInstance strength = slotContext.entity().getAttribute(Attributes.ATTACK_DAMAGE);
                        if (null != strength) {
                            strength.addOrUpdateTransientModifier(
                                    new AttributeModifier(
                                            WARRIOR_CHARM_STRENGTH,
                                            0.1,
                                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                                    )
                            );
                        }
                        AttributeInstance health = slotContext.entity().getAttribute(Attributes.MAX_HEALTH);
                        if (null != health) {
                            health.addOrUpdateTransientModifier(
                                    new AttributeModifier(
                                            WARRIOR_CHARM_HEALTH,
                                            0.1,
                                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                                    )
                            );
                        }
                    }
                    
                    @Override
                    public void onUnequip(SlotContext slotContext, ItemStack newStack) {
                        AttributeInstance strength = slotContext.entity().getAttribute(Attributes.ATTACK_DAMAGE);
                        if (null != strength && strength.hasModifier(WARRIOR_CHARM_STRENGTH)) {
                            strength.removeModifier(WARRIOR_CHARM_STRENGTH);
                        }
                        AttributeInstance health = slotContext.entity().getAttribute(Attributes.MAX_HEALTH);
                        if (null != health && health.hasModifier(WARRIOR_CHARM_HEALTH)) {
                            health.removeModifier(WARRIOR_CHARM_HEALTH);
                        }
                    }
                },
                RSItems.WARRIOR_CHARM.get()
        );
        event.registerItem(
                CuriosCapability.ITEM,
                (stack, context) -> new ICurio() {
                    
                    @Override
                    public ItemStack getStack() {
                        return stack;
                    }
                    
                    @Override
                    public boolean canEquipFromUse(SlotContext slotContext) {
                        return true;
                    }
                    
                    @Override
                    public void onEquip(SlotContext slotContext, ItemStack prevStack) {
                        AttributeInstance luck = slotContext.entity().getAttribute(Attributes.LUCK);
                        if (null != luck) {
                            luck.addOrUpdateTransientModifier(
                                    new AttributeModifier(
                                            LUCK_CHARM_LUCK,
                                            1,
                                            AttributeModifier.Operation.ADD_VALUE
                                    )
                            );
                        }
                    }
                    
                    @Override
                    public void onUnequip(SlotContext slotContext, ItemStack newStack) {
                        AttributeInstance luck = slotContext.entity().getAttribute(Attributes.LUCK);
                        if (null != luck && luck.hasModifier(LUCK_CHARM_LUCK)) {
                            luck.removeModifier(LUCK_CHARM_LUCK);
                        }
                    }
                },
                RSItems.LUCK_CHARM.get()
        );
        event.registerItem(
                CuriosCapability.ITEM,
                (stack, context) -> new ICurio() {
                    
                    @Override
                    public ItemStack getStack() {
                        return stack;
                    }
                    
                    @Override
                    public boolean canEquipFromUse(SlotContext slotContext) {
                        return true;
                    }
                    
                    /**
                     * Called every tick on both client and server while the ItemStack is equipped.
                     *
                     * @param context Context about the slot that the ItemStack is in
                     */
                    @Override
                    public void curioTick(SlotContext context) {
                        LivingEntity wearer = context.entity();
                        Level level = wearer.level();
                        EffectCure cure = EffectCures.MILK;
                        if (!level.isClientSide() && ((level.getGameTime() % (20 * 40)) == 5)) {
                            Optional<MobEffectInstance> effectMaybe = wearer.getActiveEffectsMap().values().stream().filter(effect -> effect.getCures().contains(cure)).findFirst();
                            if (effectMaybe.isPresent()) {
                                MobEffectInstance effect = effectMaybe.get();
                                if (effect.getCures().contains(cure) && !net.neoforged.neoforge.event.EventHooks.onEffectRemoved(wearer, effect, cure)) {
                                    wearer.removeEffect(effect.getEffect());
                                }
                            }
                        }
                    }
                },
                RSItems.HEALTH_CHARM.get()
        );
    }
}
