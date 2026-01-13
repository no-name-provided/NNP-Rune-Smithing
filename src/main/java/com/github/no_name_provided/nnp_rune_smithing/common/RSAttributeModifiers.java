package com.github.no_name_provided.nnp_rune_smithing.common;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.*;

import java.util.function.Function;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;

public class RSAttributeModifiers {
    public static ResourceLocation WARRIOR_CHARM_STRENGTH = ResourceLocation.fromNamespaceAndPath(MODID, "warrior_charm_strength");
    public static ResourceLocation WARRIOR_CHARM_HEALTH = ResourceLocation.fromNamespaceAndPath(MODID, "warrior_charm_health");
    public static ResourceLocation LUCK_CHARM_LUCK = ResourceLocation.fromNamespaceAndPath(MODID, "luck_charm_luck");
    
    public static ResourceLocation WARD_RUNE_ABSORPTION = ResourceLocation.fromNamespaceAndPath(MODID, "ward_rune_absorption");
    public static ResourceLocation SERENDIPITY_RUNE_LUCK = ResourceLocation.fromNamespaceAndPath(MODID, "serendipity_rune_luck");
    public static ResourceLocation AIR_RUNE_SPEED = ResourceLocation.fromNamespaceAndPath(MODID, "air_rune_speed");
    public static ResourceLocation AIR_RUNE_SAFE_HEIGHT = ResourceLocation.fromNamespaceAndPath(MODID, "air_safe_height");
    public static ResourceLocation AIR_RUNE_JUMP_STRENGTH = ResourceLocation.fromNamespaceAndPath(MODID, "air_jump_strength");
    public static ResourceLocation WATER_RUNE_UW_MINING_SPEED = ResourceLocation.fromNamespaceAndPath(MODID, "water_rune_uw_mining_speed");
    public static ResourceLocation WATER_RUNE_EXTRA_AIR = ResourceLocation.fromNamespaceAndPath(MODID, "water_rune_extra_air");
    public static ResourceLocation WATER_RUNE_EXTRA_SWIM_SPEED = ResourceLocation.fromNamespaceAndPath(MODID, "water_rune_extra_swim_speed");
    public static ResourceLocation EARTH_RUNE_HEALTH = ResourceLocation.fromNamespaceAndPath(MODID, "earth_rune_health");
    public static ResourceLocation FIRE_RUNE_STRENGTH = ResourceLocation.fromNamespaceAndPath(MODID, "fire_rune_strength");
    public static ResourceLocation FIRE_RUNE_BURNING_TIME = ResourceLocation.fromNamespaceAndPath(MODID, "fire_rune_burning_time");
    
    public static ResourceLocation AIR_RUNE_ATTACK_SPEED = ResourceLocation.fromNamespaceAndPath(MODID, "air_rune_attack_speed");
    
    public static AttributeModifier wardRuneAbsorption(double intensity) {
        return new AttributeModifier(
                WARD_RUNE_ABSORPTION,
                intensity,
                AttributeModifier.Operation.ADD_VALUE
        );
    }
    public static AttributeModifier serendipityRuneSpeed(double intensity) {
        return new AttributeModifier(
                SERENDIPITY_RUNE_LUCK,
                intensity,
                AttributeModifier.Operation.ADD_VALUE
        );
    }
    public static AttributeModifier airRuneSpeed(double intensity) {
        return new AttributeModifier(
                AIR_RUNE_SPEED,
                intensity,
                AttributeModifier.Operation.ADD_VALUE
        );
    }
    public static AttributeModifier airRuneSafeHeight(double intensity) {
        return new AttributeModifier(
                AIR_RUNE_SAFE_HEIGHT,
                intensity,
                AttributeModifier.Operation.ADD_VALUE
        );
    }
    public static AttributeModifier airRuneJumpStrength(double intensity) {
        return new AttributeModifier(
                AIR_RUNE_JUMP_STRENGTH,
                intensity,
                AttributeModifier.Operation.ADD_VALUE
        );
    }
    public static AttributeModifier waterRuneUWMiningSpeed(double intensity) {
        return new AttributeModifier(
                WATER_RUNE_UW_MINING_SPEED,
                intensity,
                AttributeModifier.Operation.ADD_VALUE
        );
    }
    public static AttributeModifier waterRuneExtraAir(double intensity) {
        return new AttributeModifier(
                WATER_RUNE_EXTRA_AIR,
                intensity,
                AttributeModifier.Operation.ADD_VALUE
        );
    }
    
    public static AttributeModifier waterRuneExtraSwimSpeed(double intensity) {
        return new AttributeModifier(
                WATER_RUNE_EXTRA_SWIM_SPEED,
                intensity,
                AttributeModifier.Operation.ADD_VALUE
        );
    }
    public static AttributeModifier earthRuneHealthChange(Double intensity) {
        return new AttributeModifier(
                EARTH_RUNE_HEALTH,
                intensity,
                AttributeModifier.Operation.ADD_VALUE
        );
    }
    public static AttributeModifier fireRuneStrengthChange(Double intensity) {
        return new AttributeModifier(
                FIRE_RUNE_STRENGTH,
                intensity,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }
    public static AttributeModifier fireRuneBurnTimeMultChange(Double intensity) {
        return new AttributeModifier(
                FIRE_RUNE_BURNING_TIME,
                intensity,
                AttributeModifier.Operation.ADD_VALUE
        );
    }
    
    public static AttributeModifier airRuneAttackSpeedChange(Double intensity) {
        return new AttributeModifier(
                AIR_RUNE_ATTACK_SPEED,
                intensity,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }
    
    public static void updateAttribute(double change, ServerPlayer player, Function<Double, AttributeModifier> getter, ResourceLocation modifierID, Holder<Attribute> attribute) {
        AttributeMap attributes = player.getAttributes();
        AttributeInstance attributesInstance = attributes.getInstance(attribute);
        
        if (null != attributesInstance) {
            // Not using convenience method here because IDE tells me it's unsafe, and Minecraft has a
            // remarkable way of nullifying things
            AttributeModifier oldModifier = attributesInstance.getModifier(modifierID);
            if (null == oldModifier) {
                attributesInstance.addTransientModifier(getter.apply(change));
                // Special case attributes that need to be increased to match new max
            } else {
                attributesInstance.addOrUpdateTransientModifier(getter.apply(oldModifier.amount() + (float) change));
                // Special case attributes that need to be increased to match new max
            }
            if (attribute == Attributes.MAX_ABSORPTION) {
                player.setAbsorptionAmount(player.getAbsorptionAmount() + (float) change);
            }
        }
    }
}
