package com.github.no_name_provided.nnp_rune_smithing.common.fluids;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;

public class RSFluidTags {
    private static final Map<String, TagKey<Fluid>> fluidTags = new LinkedHashMap<>();
    
    public static void createFluidTagKeys() {
        addFluidTagKey("earth_affinity");
        addFluidTagKey("air_affinity");
        addFluidTagKey("fire_affinity");
        addFluidTagKey("water_affinity");
    }
    
    public static void addFluidTagKey(String name) {
        fluidTags.put(name, TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath(MODID, name)));
    }
    
    public static TagKey<Fluid> getFluidTag(String name) {
        return fluidTags.get(name);
    }
}
