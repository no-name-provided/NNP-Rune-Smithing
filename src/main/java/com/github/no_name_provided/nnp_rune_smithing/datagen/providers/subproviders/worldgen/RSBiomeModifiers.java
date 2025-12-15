package com.github.no_name_provided.nnp_rune_smithing.datagen.providers.subproviders.worldgen;

import com.github.no_name_provided.nnp_rune_smithing.datagen.providers.subproviders.worldgen.features.RSPlacedFeatures;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;

public class RSBiomeModifiers {
    public static final ResourceKey<BiomeModifier> ADD_OVERWORLD_FEATURES = registerKey("add_overworld_features");
    public static final ResourceKey<BiomeModifier> ADD_NETHER_FEATURES = registerKey("add_nether_features");
    public static final ResourceKey<BiomeModifier> ADD_END_FEATURES = registerKey("add_end_features");
    
    public static void bootstrap(BootstrapContext<BiomeModifier> context) {
        HolderGetter<PlacedFeature> placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        
        context.register(ADD_OVERWORLD_FEATURES, new BiomeModifiers.AddFeaturesBiomeModifier(
                        biomes.getOrThrow(Tags.Biomes.IS_OVERWORLD),
                        HolderSet.direct(
                                placedFeatures.getOrThrow(RSPlacedFeatures.TIN_VEIN_STONE),
                                placedFeatures.getOrThrow(RSPlacedFeatures.TIN_VEIN_DEEPSLATE),
                                placedFeatures.getOrThrow(RSPlacedFeatures.ALUMINUM_VEIN_STONE),
                                placedFeatures.getOrThrow(RSPlacedFeatures.ALUMINUM_VEIN_DEEPSLATE)
                        ),
                        GenerationStep.Decoration.UNDERGROUND_ORES
                )
        );
        context.register(ADD_NETHER_FEATURES, new BiomeModifiers.AddFeaturesBiomeModifier(
                        biomes.getOrThrow(Tags.Biomes.IS_NETHER),
                        HolderSet.direct(
                                placedFeatures.getOrThrow(RSPlacedFeatures.SILVER_VEIN_NETHERRACK)
                        ),
                        GenerationStep.Decoration.UNDERGROUND_ORES
                )
        );
        context.register(ADD_END_FEATURES, new BiomeModifiers.AddFeaturesBiomeModifier(
                        biomes.getOrThrow(Tags.Biomes.IS_END),
                        HolderSet.direct(
                                placedFeatures.getOrThrow(RSPlacedFeatures.TITANIUM_VEIN_ENDSTONE),
                                placedFeatures.getOrThrow(RSPlacedFeatures.BISMUTH_VEIN_ENDSTONE)
                        ),
                        GenerationStep.Decoration.UNDERGROUND_ORES
                )
        );
    }
    
    private static ResourceKey<BiomeModifier> registerKey(String name) {
        return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ResourceLocation.fromNamespaceAndPath(MODID, name));
    }
}
