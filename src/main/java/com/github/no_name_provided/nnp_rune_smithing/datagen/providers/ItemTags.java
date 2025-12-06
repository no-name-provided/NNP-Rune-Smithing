package com.github.no_name_provided.nnp_rune_smithing.datagen.providers;

import com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosTags;

import java.util.concurrent.CompletableFuture;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;

@SuppressWarnings("CodeBlock2Expr") // Formatting preference
public class ItemTags extends ItemTagsProvider {
    public static TagKey<Item> NO_RUNES = TagKey.create( Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MODID, "no_runes"));
    
    public ItemTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTags, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, MODID, existingFileHelper);
    }
    
    @Override
    protected void addTags(HolderLookup.Provider provider) {
        RSItems.WOODEN_CHARMS.getEntries().forEach(charm -> {
            tag(CuriosTags.NECKLACE).add(charm.get());
            tag(NO_RUNES).add(charm.get());
        });
        RSItems.NUGGETS.getEntries().forEach((nugget) -> {
            tag(TagKey.create(
                    Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath("c", "nuggets/" + nugget.getKey().location().getPath().split("_")[0]))
            ).add(nugget.get());
        });
        RSItems.INGOTS.getEntries().forEach((ingot) -> {
            tag(TagKey.create(
                    Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath("c", "ingots/" + ingot.getKey().location().getPath().split("_")[0]))
            ).add(ingot.get());
        });
        RSItems.METAL_STORAGE_BLOCKS.getEntries().forEach((storage_block) -> {
            tag(TagKey.create(
                    Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath("c", "storage_blocks/" + storage_block.getKey().location().getPath().split("_")[0]))
            ).add(storage_block.get());
        });
        
        // One offs
        tag(NO_RUNES).add(
                Items.ELYTRA,
                Items.LEATHER_BOOTS,
                Items.LEATHER_LEGGINGS,
                Items.LEATHER_CHESTPLATE,
                Items.LEATHER_HELMET);
    }
}
