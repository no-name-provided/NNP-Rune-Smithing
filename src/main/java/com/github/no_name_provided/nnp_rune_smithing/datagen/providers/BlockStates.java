package com.github.no_name_provided.nnp_rune_smithing.datagen.providers;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import static com.github.no_name_provided.nnp_rune_smithing.common.blocks.RSBlocks.*;
import static com.github.no_name_provided.nnp_rune_smithing.common.fluids.FluidHelper.FLUID_SETS;

public class BlockStates extends BlockStateProvider {
    final ExistingFileHelper HELPER;
    
    public BlockStates(PackOutput output, String modid, ExistingFileHelper exFileHelper) {
        super(output, modid, exFileHelper);
        HELPER = exFileHelper;
    }

    @Override
    protected void registerStatesAndModels() {
        FLUID_SETS.forEach(set ->
                // Make placeholder to suppress vacuous warnings on startup
                simpleBlock(set.block().value(), models().getExistingFile(mcLoc("lava")))
        );
        
        METAL_STORAGE_BLOCKS.getEntries().forEach(entry ->
                simpleBlock(entry.get(), models().getExistingFile(modLoc("block/generic_metal_block")))
        );
        ORE_BLOCKS.getEntries().forEach(oreBlock -> {
            ModelFile.ExistingModelFile model = switch(oreBlock.getId().getPath().split("_")[0]) {
                case ("deepslate") -> models().getExistingFile(modLoc("block/generic_deepslate_ore_block"));
                case ("netherrack") -> models().getExistingFile(modLoc("block/generic_netherrack_ore_block"));
                case ("endstone") -> models().getExistingFile(modLoc("block/generic_endstone_ore_block"));
                default -> models().getExistingFile(modLoc("block/generic_ore_block"));
            };
            simpleBlockWithItem(oreBlock.get(), model);
        });
        
        // Make placeholder to suppress vacuous warnings on startup
        simpleBlock(RUNE_BLOCK.get(), models().getExistingFile(mcLoc("dirt")));
        
        // Regular one-offs
        simpleBlock(CASTING_TABLE.get(), models().getExistingFile(modLoc("casting_table")));
        
        // Blocks with horizontal rotations handled by model files
        horizontalBlock(
                WHITTLING_TABLE.get(),
                models().getExistingFile(modLoc("whittling_table"))
        );
        horizontalBlock(
                MELTER.get(),
                models().getExistingFile(modLoc("melting_furnace"))
        );
        horizontalBlock(
                RUNE_ANVIL.get(),
                models().getExistingFile(modLoc("rune_anvil"))
        );
    }
}
