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
        RSItems.NUGGETS.getEntries().forEach((nugget) -> {
            withExistingParent(nugget.getRegisteredName(), BuiltInRegistries.ITEM.getKey(Items.IRON_NUGGET));
        });
        RSItems.INGOTS.getEntries().forEach((ingot) -> {
            withExistingParent(ingot.getRegisteredName(), BuiltInRegistries.ITEM.getKey(Items.IRON_INGOT));
        });
        RSItems.METAL_STORAGE_BLOCKS.getEntries().forEach((storage_block) -> {
            withExistingParent(storage_block.getId().toString(), ResourceLocation.fromNamespaceAndPath(MODID, "block/generic_metal_block"));
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
        
        withExistingParent(RSItems.BLANK_MOLD.getRegisteredName(), ResourceLocation.withDefaultNamespace("item/generated")).texture("layer0", ResourceLocation.fromNamespaceAndPath(MODID, "item/mold_base_32"));
        withExistingParent(RSItems.NUGGET_MOLD.getRegisteredName(), ResourceLocation.withDefaultNamespace("item/generated")).texture("layer0", ResourceLocation.fromNamespaceAndPath(MODID, "item/nugget_mold"));
        withExistingParent(RSItems.INGOT_MOLD.getRegisteredName(), ResourceLocation.withDefaultNamespace("item/generated")).texture("layer0", ResourceLocation.fromNamespaceAndPath(MODID, "item/ingot_mold"));
        withExistingParent(RSItems.BLOCK_MOLD.getRegisteredName(), ResourceLocation.withDefaultNamespace("item/generated")).texture("layer0", ResourceLocation.fromNamespaceAndPath(MODID, "item/block_mold"));
    }
}
