package com.github.no_name_provided.nnp_rune_smithing.datagen.providers.subproviders.worldgen.features;

import com.github.no_name_provided.nnp_rune_smithing.common.blocks.RSBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.List;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;

public class RSConfiguredFeatures {
    public static ResourceKey<ConfiguredFeature<?, ?>> TIN_VEIN_STONE = registerKey("tin_vein_stone");
    public static ResourceKey<ConfiguredFeature<?, ?>> TIN_VEIN_DEEPSLATE = registerKey("tin_vein_deepslate");
    public static ResourceKey<ConfiguredFeature<?, ?>> SILVER_VEIN_NETHERRACK = registerKey("silver_vein_netherrack");
    public static ResourceKey<ConfiguredFeature<?, ?>> ALUMINUM_VEIN_STONE = registerKey("aluminum_vein_stone");
    public static ResourceKey<ConfiguredFeature<?, ?>> ALUMINUM_VEIN_DEEPSLATE = registerKey("aluminum_vein_deepslate");
    public static ResourceKey<ConfiguredFeature<?, ?>> TITANIUM_VEIN_ENDSTONE = registerKey("titanium_vein_endstone");
    public static ResourceKey<ConfiguredFeature<?, ?>> BISMUTH_VEIN_ENDSTONE = registerKey("bismuth_vein_endstone");
    
    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        RuleTest stoneReplaceable = new TagMatchTest(Tags.Blocks.ORE_BEARING_GROUND_STONE);
        RuleTest deepslateReplaceable = new TagMatchTest(Tags.Blocks.ORE_BEARING_GROUND_DEEPSLATE);
        RuleTest netherReplaceable = new TagMatchTest(Tags.Blocks.ORE_BEARING_GROUND_NETHERRACK);
        RuleTest endReplaceable = new TagMatchTest(Tags.Blocks.END_STONES);
        
        ArrayList<DeferredHolder<Block, ?>> ores = new ArrayList<>(RSBlocks.ORE_BLOCKS.getEntries());
        Block tin = getOreBlockByRegistryPath("tin_ore", ores);
        Block deepslateTin = getOreBlockByRegistryPath("deepslate_tin_ore", ores);
        Block aluminum = getOreBlockByRegistryPath("aluminum_ore", ores);
        Block deepslateAluminum = getOreBlockByRegistryPath("deepslate_aluminum_ore", ores);
        Block netherrackSilver = getOreBlockByRegistryPath("netherrack_silver_ore", ores);
        Block endTitanium = getOreBlockByRegistryPath("endstone_titanium_ore", ores);
        Block endBismuth = getOreBlockByRegistryPath("endstone_bismuth_ore", ores);
        
        // Overworld ores
        register(context, ALUMINUM_VEIN_STONE, Feature.ORE, new OreConfiguration(stoneReplaceable, aluminum.defaultBlockState(), 5));
        register(context, ALUMINUM_VEIN_DEEPSLATE, Feature.ORE, new OreConfiguration(deepslateReplaceable, deepslateAluminum.defaultBlockState(), 8));
        register(context, TIN_VEIN_STONE, Feature.ORE, new OreConfiguration(stoneReplaceable, tin.defaultBlockState(), 6));
        register(context, TIN_VEIN_DEEPSLATE, Feature.ORE, new OreConfiguration(deepslateReplaceable, deepslateTin.defaultBlockState(), 4));
        // Nether ores
        register(context, SILVER_VEIN_NETHERRACK, Feature.ORE, new OreConfiguration(netherReplaceable, netherrackSilver.defaultBlockState(), 8));
        // End ores
        register(context, TITANIUM_VEIN_ENDSTONE, Feature.ORE, new OreConfiguration(endReplaceable, endTitanium.defaultBlockState(), 10));
        register(context, BISMUTH_VEIN_ENDSTONE, Feature.ORE, new OreConfiguration(endReplaceable, endBismuth.defaultBlockState(), 6));
        
    }
    
    public static ResourceKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(MODID, name));
    }
    
    private static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(
            BootstrapContext<ConfiguredFeature<?, ?>> context,
            ResourceKey<ConfiguredFeature<?, ?>> key,
            F feature,
            FC configuration) {
        context.register(key, new ConfiguredFeature<>(feature, configuration));
    }
    
    // This is only executed as part of compilation, so I want a crash if there are any blocks missing
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public static Block getOreBlockByRegistryPath(String path, List<DeferredHolder<Block, ?>> holderList) {
        return holderList.stream().filter(holder -> holder.getId().getPath().equals(path)).findFirst().get().get();
    }
}
