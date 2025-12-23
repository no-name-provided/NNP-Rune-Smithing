package com.github.no_name_provided.nnp_rune_smithing.datagen;

import com.github.no_name_provided.nnp_rune_smithing.datagen.providers.*;
import com.github.no_name_provided.nnp_rune_smithing.datagen.providers.advancements.RSGuideBookAdvancementSubProvider;
import com.github.no_name_provided.nnp_rune_smithing.datagen.providers.subproviders.GenericLootTables;
import com.github.no_name_provided.nnp_rune_smithing.datagen.providers.subproviders.SimpleBlockLoot;
import com.github.no_name_provided.nnp_rune_smithing.datagen.providers.subproviders.global_loot_modifiers.SingleItemPools;
import com.github.no_name_provided.nnp_rune_smithing.datagen.providers.subproviders.worldgen.RSWorldGen;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;

@EventBusSubscriber
public class Generators {
    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        event.addProvider(new RSBlockModelProvider(packOutput, existingFileHelper));
        event.addProvider(new RSItemModelProvider(packOutput, MODID, existingFileHelper));
        event.addProvider(new RSBlockStateProvider(packOutput, MODID, existingFileHelper));
        BlockTagsProvider blockTags = event.addProvider(new RSBlockTagProvider(packOutput, lookupProvider, MODID, existingFileHelper));
        event.addProvider(new RSItemTagProvider(packOutput, lookupProvider, blockTags.contentsGetter(), existingFileHelper));
        event.addProvider(new RSFluidTagProvider(packOutput, lookupProvider, existingFileHelper));
        event.addProvider(new RSAdvancementProvider(packOutput, lookupProvider, existingFileHelper, List.of(
                new RSGuideBookAdvancementSubProvider()
        )));
        event.addProvider(new RSDataMapProvider(packOutput, lookupProvider));
        event.addProvider(new RSLanguageProvider_EN_US(packOutput, MODID, Locale.US.toString().toLowerCase()));
        event.addProvider(new RSLootProvider(
                packOutput,
                Set.of(),//Required tables. Guess you'd use this if you plan to reference tables that you aren't creating
                List.of(
                        new LootTableProvider.SubProviderEntry(SimpleBlockLoot::new, LootContextParamSets.BLOCK),
                        new LootTableProvider.SubProviderEntry(GenericLootTables::new, LootContextParamSets.ENTITY),
                        // Global loot modifier tables
                        new LootTableProvider.SubProviderEntry(SingleItemPools::new, LootContextParamSets.ALL_PARAMS)
                ),
                lookupProvider
        ));
        event.addProvider(new RSGlobalLootModifierProvider(packOutput, lookupProvider));
        event.addProvider(new RSParticleDescriptionProvider(packOutput, existingFileHelper));
        
        event.addProvider(new RSRecipeProvider(packOutput, lookupProvider));
        
        generator.addProvider(event.includeServer(), new RSWorldGen(packOutput, lookupProvider));
    }

}
