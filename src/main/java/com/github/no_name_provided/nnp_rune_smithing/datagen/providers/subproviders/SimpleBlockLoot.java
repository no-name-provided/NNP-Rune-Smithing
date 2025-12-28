package com.github.no_name_provided.nnp_rune_smithing.datagen.providers.subproviders;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.Set;

import static com.github.no_name_provided.nnp_rune_smithing.common.blocks.RSBlocks.*;
import static com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems.RAW_ORES;

public class SimpleBlockLoot extends BlockLootSubProvider {
    public SimpleBlockLoot(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.DEFAULT_FLAGS, registries);
    }
    
    // For whatever reason, it is necessary to list the blocks we'll be handling here.
    // Behaves strangely for abnormal blocks (eg, fluid blocks), so they're kept in a different registry.
    // We return an Iterable over our block registry's values here. Anything we don't use throws an error.
    // Defaults to using the entire vanilla block registry, so you usually want to override it.
    @Override
    protected Iterable<Block> getKnownBlocks() {
        ArrayList<Block> iterableBlocks = new ArrayList<>(); // define empty list
        BLOCKS.getEntries().stream().map(DeferredHolder::get).forEach(iterableBlocks::add);
        METAL_STORAGE_BLOCKS.getEntries().stream().map(DeferredHolder::get).forEach(iterableBlocks::add);
        ORE_BLOCKS.getEntries().stream().map(DeferredHolder::get).forEach(iterableBlocks::add);
        return iterableBlocks;
    }
    
    @Override
    protected void generate() {
        METAL_STORAGE_BLOCKS.getEntries().forEach(entry -> {
            dropSelf(entry.get());
        });
        ORE_BLOCKS.getEntries().forEach(entry -> {
            String name = switch (entry.getId().getPath().split("_")[0]) {
                case "deepslate", "endstone", "netherrack" -> entry.getId().getPath().split("_")[1];
                default -> entry.getId().getPath().split("_")[0];
            };
            String rawOreName = "raw_" + name;
            RAW_ORES.getEntries().stream().filter(itemEntry -> itemEntry.getId().getPath().equals(rawOreName)).forEach(itemEntry ->
                    // Should be exactly one match
                    add(entry.get(), createOreDrop(entry.get(), itemEntry.get()))
            );
        });
        
        dropSelf(WHITTLING_TABLE.get());
        dropSelf(MELTER.get());
        dropSelf(ALLOYER.get());
        dropSelf(CASTING_TABLE.get());
        dropSelf(RUNE_ANVIL.get());
        add(RUNE_BLOCK.get(), noDrop());
    }
}
