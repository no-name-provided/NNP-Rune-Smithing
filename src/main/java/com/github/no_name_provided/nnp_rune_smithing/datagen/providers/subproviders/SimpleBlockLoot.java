package com.github.no_name_provided.nnp_rune_smithing.datagen.providers.subproviders;

import com.github.no_name_provided.nnp_rune_smithing.common.blocks.RuneBlock;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Set;

import static com.github.no_name_provided.nnp_rune_smithing.common.blocks.RSBlocks.*;

public class SimpleBlockLoot extends BlockLootSubProvider {
    public SimpleBlockLoot(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.DEFAULT_FLAGS, registries);
    }
    
    // For whatever reason, it is necessary to list the blocks we'll be handling here.
    // Behaves strangely for abnormal blocks (eg, fluid blocks), so they're kept in a different registry.
    // We return an Iterable over our block registry's values here. Anything we don't use throws an error.
    // Defaults to using the entire vanilla block registry, so you usually want to override it.
    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        ArrayList<Block> iterableBlocks = new ArrayList<>(); // define empty list
        BLOCKS.getEntries().stream().map(DeferredHolder::get).forEach(iterableBlocks::add);
        METAL_STORAGE_BLOCKS.getEntries().stream().map(DeferredHolder::get).forEach(iterableBlocks::add);
        return iterableBlocks;
    }
    
    @Override
    protected void generate() {
        METAL_STORAGE_BLOCKS.getEntries().forEach(entry -> {
            dropSelf(entry.get());
        });
        
        dropSelf(WHITTLING_TABLE.get());
        dropSelf(MELTER.get());
        dropSelf(CASTING_TABLE.get());
        dropSelf(RUNE_ANVIL.get());
        add(RUNE_BLOCK.get(), noDrop());
    }
}
