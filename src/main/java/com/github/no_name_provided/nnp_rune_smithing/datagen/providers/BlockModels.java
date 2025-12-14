package com.github.no_name_provided.nnp_rune_smithing.datagen.providers;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
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
        // which appear difficult to datagen (see BlockModelGenerators.java#5208 for the vanilla callstack
        // or the generic ore block code here for a modded implementation)
        withExistingParent("block/generic_metal_block", "leaves")
                .texture("all", "block/generic_metal_block")
                .renderType(mcLoc("solid"));

        BlockModelBuilder ore_builder = getBuilder("block/ore_block")
                .parent(getExistingFile(mcLoc("block/block")))
                .renderType(RenderType.CUTOUT.name)
                .texture("particle", "#background")
                .texture("particle", "#foreground")
                .element().allFaces( (direction, builder) -> {
                    // This line is ignored
                    builder.tintindex(-1).texture("#background")
                            // This line is ignored
                            .uvs(0, 0, 16, 16)
                            .cullface(direction)
                            .end();
                }).end()
                .element().allFaces( (direction, builder) -> {
                    builder.tintindex(1).texture("#foreground")
                            // This line is ignored
                            .uvs(0, 0, 16, 16)
                            .cullface(direction)
                            .end();
                }).end();
        
        withExistingParent("block/generic_ore_block", ore_builder.getLocation())
                .texture("background", "block/generic_ore_layer_0")
                .texture("foreground", "block/generic_ore_layer_1");
        withExistingParent("block/generic_deepslate_ore_block", ore_builder.getLocation())
                .texture("background", "block/generic_deepslate_ore_layer_0")
                .texture("foreground", "block/generic_ore_layer_1");
        withExistingParent("block/generic_netherrack_ore_block", ore_builder.getLocation())
                .texture("background", "block/generic_netherrack_ore_layer_0")
                .texture("foreground", "block/generic_ore_layer_1");
        withExistingParent("block/generic_endstone_ore_block", ore_builder.getLocation())
                .texture("background", "block/generic_endstone_ore_layer_0")
                .texture("foreground", "block/generic_ore_layer_1");
    }
}
