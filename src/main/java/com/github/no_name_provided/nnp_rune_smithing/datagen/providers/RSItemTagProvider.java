package com.github.no_name_provided.nnp_rune_smithing.datagen.providers;

import com.github.no_name_provided.nnp_rune_smithing.common.items.CastingTemplate;
import com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosTags;

import java.util.concurrent.CompletableFuture;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;

public class RSItemTagProvider extends ItemTagsProvider {
    public static TagKey<Item> NO_RUNES = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MODID, "no_runes"));
    public static TagKey<Item> WHITTLING_MATERIALS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MODID, "whittling_materials"));
    public static TagKey<Item> WHITTLING_TOOLS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MODID, "whittling_tools"));
    public static TagKey<Item> WHITTLING_TEMPLATES = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MODID, "whittling_templates"));
    
    public RSItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTags, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, MODID, existingFileHelper);
    }
    
    @Override
    protected void addTags(HolderLookup.Provider provider) {
        RSItems.WOODEN_CHARMS.getEntries().forEach(charm -> {
            tag(CuriosTags.NECKLACE).add(charm.get());
            tag(NO_RUNES).add(charm.get());
        });
        RSItems.NUGGETS.getEntries().forEach((nugget) -> {
            TagKey<Item> key = TagKey.create(
                    Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath("c", "nuggets/" + getTagPathFromHolder(nugget))
            );
            tag(key).add(nugget.get());
            tag(Tags.Items.NUGGETS).addTag(key);
        });
        RSItems.INGOTS.getEntries().forEach((ingot) -> {
            TagKey<Item> key = TagKey.create(
                    Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath("c", "ingots/" + getTagPathFromHolder(ingot))
            );
            tag(key).add(ingot.get());
            tag(Tags.Items.INGOTS).addTag(key);
        });
        RSItems.METAL_STORAGE_BLOCKS.getEntries().forEach((storage_block) -> {
            TagKey<Item> key = TagKey.create(
                    Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath("c", "storage_blocks/" + getTagPathFromHolder(storage_block))
            );
            tag(key).add(storage_block.get());
            tag(Tags.Items.STORAGE_BLOCKS).addTag(key);
        });
        RSItems.RAW_ORES.getEntries().forEach((ore) -> {
            String name = extractNameFromOre(ore);
            TagKey<Item> key = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "raw_materials/" + name));
            tag(key).add(ore.get());
            tag(Tags.Items.RAW_MATERIALS).addTag(key);
        });
        // Whittling templates
        RSItems.WOODEN_CHARMS.getEntries().forEach((ore) -> {
            tag(WHITTLING_TEMPLATES).add(ore.get());
        });
        RSItems.RUNES.getEntries().forEach((ore) -> {
            tag(WHITTLING_TEMPLATES).add(ore.get());
        });
        RSItems.ITEMS.getEntries().stream()
                .filter(item -> item.get() instanceof CastingTemplate)
                .forEach((ore) -> {
                    tag(WHITTLING_TEMPLATES).add(ore.get());
                });
        
        
        // One offs
        tag(NO_RUNES).
                add(
                        Items.ELYTRA,
                        Items.LEATHER_BOOTS,
                        Items.LEATHER_LEGGINGS,
                        Items.LEATHER_CHESTPLATE,
                        Items.LEATHER_HELMET);
        tag(WHITTLING_MATERIALS).
                addTag(ItemTags.LOGS).
                addTag(Tags.Items.STRIPPED_LOGS);
        tag(WHITTLING_TOOLS).
                add(RSItems.WHITTLING_KNIFE.get());
    }
    
    /**
     * Extracts the material identifier, without trailing metadata. Usually correct string for tag (sub)path.
     *
     * <p>
     * Throws error if string has no '_' character.
     * </p>
     */
    private String getTagPathFromHolder(DeferredHolder<?, ?> holder) {
        
        return holder.getId().getPath().substring(0, holder.getId().getPath().lastIndexOf('_'));
    }
    
    /**
     * Extracts the base name of this ore item.
     * <p>
     * Throws error if string has no '_' character.
     * </p>
     *
     * @param ore The holder for the ore we're parsing.
     */
    private String extractNameFromOre(DeferredHolder<?, ?> ore) {
        
        return ore.getId().getPath().substring(ore.getId().getPath().indexOf('_') + 1);
    }
}
