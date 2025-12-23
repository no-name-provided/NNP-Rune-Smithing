package com.github.no_name_provided.nnp_rune_smithing.common.datamaps;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;

public class RSDataMaps {
    public static final DataMapType<Fluid, CastableFluidData> CASTABLE_FLUID_DATA = DataMapType.builder(
            ResourceLocation.fromNamespaceAndPath(MODID, "castable_fluid"),
            Registries.FLUID,
            CastableFluidData.CODEC
    ).synced(CastableFluidData.CODEC, true)
            .build();
}
