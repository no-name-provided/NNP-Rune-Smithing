package com.github.no_name_provided.nnp_rune_smithing.datagen.providers.subproviders.worldgen;

import com.github.no_name_provided.nnp_rune_smithing.datagen.providers.subproviders.worldgen.features.RSConfiguredFeatures;
import com.github.no_name_provided.nnp_rune_smithing.datagen.providers.subproviders.worldgen.features.RSPlacedFeatures;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;

public class RSWorldGen extends DatapackBuiltinEntriesProvider {
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.CONFIGURED_FEATURE, RSConfiguredFeatures::bootstrap)
            .add(Registries.PLACED_FEATURE, RSPlacedFeatures::bootstrap)
            
            .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, RSBiomeModifiers::bootstrap);
    
    public RSWorldGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(MODID));
    }
}
