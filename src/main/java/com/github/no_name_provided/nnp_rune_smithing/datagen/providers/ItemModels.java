package com.github.no_name_provided.nnp_rune_smithing.datagen.providers;

import com.github.no_name_provided.nnp_rune_smithing.common.blocks.RSBlocks;
import com.github.no_name_provided.nnp_rune_smithing.common.fluids.FluidHelper;
import com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.loaders.DynamicFluidContainerModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;

@SuppressWarnings("CodeBlock2Expr") // Formatting preference
public class ItemModels extends ItemModelProvider {
    public ItemModels(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
        super(output, modid, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        FluidHelper.FLUID_SETS.forEach(set ->
                getBuilder(set.bucket().getRegisteredName())
                        .parent(getExistingFile(ResourceLocation.fromNamespaceAndPath("neoforge", "item/bucket")))
                        .customLoader(DynamicFluidContainerModelBuilder::begin)
                        .applyFluidLuminosity(true)
                        .coverIsMask(false)
                        .flipGas(false)
                        .fluid(set.source().get())
        );
        RSItems.RAW_ORES.getEntries().forEach((rawOre) -> {
            withExistingParent(rawOre.getRegisteredName(), mcLoc("item/generated"))
                    .texture("layer0", modLoc("item/raw_generic_metal"));
        });
        RSItems.NUGGETS.getEntries().forEach((nugget) -> {
            withExistingParent(nugget.getRegisteredName(), BuiltInRegistries.ITEM.getKey(Items.IRON_NUGGET));
        });
        RSItems.INGOTS.getEntries().forEach((ingot) -> {
            withExistingParent(ingot.getRegisteredName(), BuiltInRegistries.ITEM.getKey(Items.IRON_INGOT));
        });
        RSItems.METAL_STORAGE_BLOCKS.getEntries().forEach((storage_block) -> {
            withExistingParent(storage_block.getId().toString(), ResourceLocation.fromNamespaceAndPath(MODID, "block/generic_metal_block"));
        });
        RSItems.WOODEN_CHARMS.getEntries().forEach(charm -> {
            basicItem(charm.get());
        });
        RSItems.RUNES.getEntries().forEach(entry -> {
            withExistingParent(entry.getRegisteredName(), mcLoc("item/generated")).texture("layer0", modLoc("item/" + entry.getId().getPath()));
            withExistingParent(entry.getId().getPath().split("_r")[0] + "_mold", mcLoc("item/generated")).texture("layer0", modLoc("item/" + entry.getId().getPath() + "_mold"));
            withExistingParent(entry.getId().getPath().split("_r")[0] + "_template", mcLoc("item/generated")).texture("layer0", modLoc("item/" + entry.getId().getPath() + "_template"));
        });
        // Make placeholder to suppress vacuous warnings on startup
        withExistingParent(RSItems.PLACE_HOLDER_RUNE.getRegisteredName(), mcLoc("item/stick"));
        
        // Regular one-offs
        simpleBlockItem(RSBlocks.WHITTLING_TABLE.get());
        basicItem(RSItems.WHITTLING_KNIFE.get()).parent(getExistingFile(mcLoc("item/handheld")));
        withExistingParent(RSItems.MELTER.getRegisteredName(), modLoc("block/melting_furnace"));
        withExistingParent(RSItems.CASTING_TABLE.getRegisteredName(), ResourceLocation.fromNamespaceAndPath(MODID, "block/casting_table"));
        simpleBlockItem(RSBlocks.RUNE_ANVIL.get());
        
        withExistingParent(RSItems.RUNE_SMITH_HAMMER.getRegisteredName(), mcLoc("item/handheld")).texture("layer0", modLoc("item/rune_smith_hammer"));
        
        basicItem(RSItems.FIRE_CLAY_BALL.get());
        withExistingParent(RSItems.BLANK_MOLD.getRegisteredName(), ResourceLocation.withDefaultNamespace("item/generated")).texture("layer0", ResourceLocation.fromNamespaceAndPath(MODID, "item/mold_base_32"));
        basicItem(RSItems.NUGGET_MOLD.get());
        basicItem(RSItems.NUGGET_MOLD_REUSABLE.get());
        basicItem(RSItems.INGOT_MOLD.get());
        basicItem(RSItems.INGOT_MOLD_REUSABLE.get());
        basicItem(RSItems.BLOCK_MOLD.get());
        basicItem(RSItems.BLOCK_MOLD_REUSABLE.get());
    }
}
