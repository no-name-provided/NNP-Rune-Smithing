package com.github.no_name_provided.nnp_rune_smithing.datagen.providers.subproviders.worldgen.features;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.*;

import java.util.List;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;
import static net.minecraft.data.worldgen.placement.PlacementUtils.register;

public class RSPlacedFeatures {
    public static final ResourceKey<PlacedFeature> TIN_VEIN_STONE = registerKey("tin_vein_stone");
    public static final ResourceKey<PlacedFeature> TIN_VEIN_DEEPSLATE = registerKey("tin_vein_deepslate");
    public static final ResourceKey<PlacedFeature> SILVER_VEIN_NETHERRACK = registerKey("silver_vein_netherrack");
    public static final ResourceKey<PlacedFeature> ALUMINUM_VEIN_STONE = registerKey("aluminum_vein_stone");
    public static final ResourceKey<PlacedFeature> ALUMINUM_VEIN_DEEPSLATE = registerKey("aluminum_vein_deepslate");
    public static final ResourceKey<PlacedFeature> TITANIUM_VEIN_ENDSTONE = registerKey("titanium_vein_endstone");
    public static final ResourceKey<PlacedFeature> BISMUTH_VEIN_ENDSTONE = registerKey("bismuth_vein_endstone");
    
    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);
        
        register(context,
                TIN_VEIN_STONE,
                configuredFeatures.getOrThrow(RSConfiguredFeatures.TIN_VEIN_STONE),
                commonOrePlacement(8, HeightRangePlacement.uniform(VerticalAnchor.BOTTOM, VerticalAnchor.TOP)
                )
        );
        register(context,
                TIN_VEIN_DEEPSLATE,
                configuredFeatures.getOrThrow(RSConfiguredFeatures.TIN_VEIN_DEEPSLATE),
                rareOrePlacement(8, HeightRangePlacement.uniform(VerticalAnchor.BOTTOM, VerticalAnchor.TOP)
                )
        );
        register(context,
                ALUMINUM_VEIN_STONE,
                configuredFeatures.getOrThrow(RSConfiguredFeatures.ALUMINUM_VEIN_STONE),
                rareOrePlacement(8, HeightRangePlacement.uniform(VerticalAnchor.BOTTOM, VerticalAnchor.TOP)
                )
        );
        register(context,
                ALUMINUM_VEIN_DEEPSLATE,
                configuredFeatures.getOrThrow(RSConfiguredFeatures.ALUMINUM_VEIN_DEEPSLATE),
                commonOrePlacement(8, HeightRangePlacement.uniform(VerticalAnchor.BOTTOM, VerticalAnchor.TOP)
                )
        );
        register(context,
                SILVER_VEIN_NETHERRACK,
                configuredFeatures.getOrThrow(RSConfiguredFeatures.SILVER_VEIN_NETHERRACK),
                rareOrePlacement(8, HeightRangePlacement.uniform(VerticalAnchor.BOTTOM, VerticalAnchor.TOP)
                )
        );
        register(context,
                TITANIUM_VEIN_ENDSTONE,
                configuredFeatures.getOrThrow(RSConfiguredFeatures.TITANIUM_VEIN_ENDSTONE),
                rareOrePlacement(8, HeightRangePlacement.uniform(VerticalAnchor.BOTTOM, VerticalAnchor.TOP)
                )
        );
        register(context,
                BISMUTH_VEIN_ENDSTONE,
                configuredFeatures.getOrThrow(RSConfiguredFeatures.BISMUTH_VEIN_ENDSTONE),
                commonOrePlacement(8, HeightRangePlacement.uniform(VerticalAnchor.BOTTOM, VerticalAnchor.TOP)
                )
        );
    }
    
    /**
     * Edited from net.minecraft.data.worldgen.placement.OrePlacement
     */
    private static List<PlacementModifier> orePlacement(PlacementModifier countPlacement, PlacementModifier heightRange) {
        return List.of(countPlacement, InSquarePlacement.spread(), heightRange, BiomeFilter.biome());
    }
    
    public static List<PlacementModifier> commonOrePlacement(int count, PlacementModifier heightRange) {
        return orePlacement(CountPlacement.of(count), heightRange);
    }
    
    public static List<PlacementModifier> rareOrePlacement(int chance, PlacementModifier heightRange) {
        return orePlacement(RarityFilter.onAverageOnceEvery(chance), heightRange);
    }
    
    private static ResourceKey<PlacedFeature> registerKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(MODID, name));
    }
}
