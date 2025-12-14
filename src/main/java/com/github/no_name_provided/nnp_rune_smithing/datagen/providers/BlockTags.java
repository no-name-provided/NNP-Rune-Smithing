package com.github.no_name_provided.nnp_rune_smithing.datagen.providers;


import com.github.no_name_provided.nnp_rune_smithing.common.blocks.RSBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;
import static com.github.no_name_provided.nnp_rune_smithing.common.blocks.RSBlocks.CASTING_TABLE;
import static com.github.no_name_provided.nnp_rune_smithing.common.blocks.RSBlocks.MELTER;

public class BlockTags extends BlockTagsProvider {
    public static TagKey<Block> INVENTORY_BLOCKS = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(MODID, "inventory_blocks"));
    
    public BlockTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, modId, existingFileHelper);
    }
    
    @Override
    protected void addTags(HolderLookup.Provider provider) {
        RSBlocks.METAL_STORAGE_BLOCKS.getEntries().forEach(entry -> {
            Block block = entry.get();
            tag(Tags.Blocks.STORAGE_BLOCKS).add(block);
            tag(net.minecraft.tags.BlockTags.NEEDS_IRON_TOOL).add(block);
            tag(net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE).add(block);
        });
        RSBlocks.ORE_BLOCKS.getEntries().forEach(entry -> {
            Block block = entry.get();
            // Not going to bother with all the crazy specific sub tagging.
            tag(Tags.Blocks.ORES).add(block);
            // Not currently bothering with gradiated mining requirements
            tag(net.minecraft.tags.BlockTags.NEEDS_IRON_TOOL).add(block);
            tag(net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE).add(block);
        });
        
        // One offs
        tag(net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE)
                .add(MELTER.get())
                .add(CASTING_TABLE.get());
        tag(INVENTORY_BLOCKS).addTag(Tags.Blocks.CHESTS).add(Blocks.BARREL);
    }
}
