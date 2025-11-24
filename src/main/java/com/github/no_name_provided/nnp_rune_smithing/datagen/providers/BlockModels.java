package com.github.no_name_provided.nnp_rune_smithing.datagen.providers;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;

public class BlockModels extends BlockModelProvider {
    public BlockModels(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, MODID, existingFileHelper);
    }
    
    @Override
    protected void registerModels() {
        // We use leaves here because it's a convenient equivalent to cube that includes tint indexes,
        // which appear difficult to datagen (see BlockModelGenerators.java#5208 for the vanilla callstack)
        withExistingParent("block/generic_metal_block", "leaves")
                .texture("all", "block/generic_metal_block")
                .renderType(mcLoc("solid"));
//        new BlockModelBuilder(modLoc("block/generic_metal_block"), existingFileHelper).parent(getExistingFile(mcLoc("block/cube_all"))).texture("layer0", modLoc("block/generic_metal_block"));
    }
}
