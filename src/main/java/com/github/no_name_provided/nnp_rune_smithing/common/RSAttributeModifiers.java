package com.github.no_name_provided.nnp_rune_smithing.common;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;

public class RSAttributeModifiers {
    public static ResourceLocation WARD_RUNE_ABSORPTION = ResourceLocation.fromNamespaceAndPath(MODID, "ward_rune_absorption");
    
    public static AttributeModifier wardRuneAbsorption(double intensity) {
        return new AttributeModifier(
                WARD_RUNE_ABSORPTION,
                intensity,
                AttributeModifier.Operation.ADD_VALUE
        );
    }
    

}
