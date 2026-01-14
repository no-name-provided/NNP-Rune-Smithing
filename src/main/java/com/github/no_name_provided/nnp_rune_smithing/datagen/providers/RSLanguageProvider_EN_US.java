package com.github.no_name_provided.nnp_rune_smithing.datagen.providers;

import com.github.no_name_provided.nnp_rune_smithing.common.items.CastingTemplate;
import com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Arrays;

import static com.github.no_name_provided.nnp_rune_smithing.common.blocks.RSBlocks.RUNE_BLOCK;
import static com.github.no_name_provided.nnp_rune_smithing.common.fluids.FluidHelper.FLUID_SETS;
import static com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems.*;

@SuppressWarnings("CodeBlock2Expr")
public class RSLanguageProvider_EN_US extends LanguageProvider {
    public RSLanguageProvider_EN_US(PackOutput output, String modid, String locale) {
        super(output, modid, locale);
    }
    
    @Override
    protected void addTranslations() {
        FLUID_SETS.forEach(set -> {
            String name = extractAndFormatName(set.type().getId().getPath(), false);
            add(set.bucket().get(), "Molten " + name + " Bucket");
            add(set.block().get(), "Molten " + name + " Block");
            add("fluid_type." + set.type().getRegisteredName().replace(':', '.'),
                    "Molten " + name);
        });
        RSItems.NUGGETS.getEntries().forEach((nugget) -> {
            add(nugget.get(), extractAndFormatName(pathFromHolder(nugget), false));
        });
        RSItems.INGOTS.getEntries().forEach((ingot) -> {
            add(ingot.get(), extractAndFormatName(pathFromHolder(ingot), false));
        });
        RSItems.METAL_STORAGE_BLOCKS.getEntries().forEach((storageBlock) -> {
            add(storageBlock.get(), "Block of " + extractAndFormatName(pathFromHolder(storageBlock), true));
        });
        RAW_ORES.getEntries().forEach((rawOre) -> {
            add(rawOre.get(), extractAndFormatName(pathFromHolder(rawOre), false));
        });
        ORE_BLOCKS.getEntries().forEach((ore_block) -> {
            add(ore_block.get(), extractAndFormatName(pathFromHolder(ore_block), false));
        });
        WOODEN_CHARMS.getEntries().forEach(charm -> {
            add(charm.get(), extractAndFormatName(pathFromHolder(charm), false));
        });
        RSItems.RUNES.getEntries().forEach(rune -> {
            add(rune.get(), cFChar(rune.getId().getPath().split("_")[0]) + " Rune");
            add("item.nnp_rune_smithing." + rune.getId().getPath().split("_")[0] + "_mold", cFChar(rune.getId().getPath().split("_")[0]) + " Mold");
        });
        ITEMS.getEntries().stream()
                .filter(holder -> holder.get() instanceof CastingTemplate)
                .forEach(templateHolder -> {
                    add(templateHolder.get(), extractAndFormatName(pathFromHolder(templateHolder), false));
                });
        
        // One offs
        
        add("itemGroup.nnp_rune_smithing", "Rune Smithing");
        add(WHITTLING_TABLE.get(), "Whittling Table");
        add(MELTER.get(), "Melting Furnace");
        add("container.melter", "Melting Furnace");
        add(ALLOYER.get(), "Alloyer");
        add(CASTING_TABLE.get(), "Casting Table");
        add("container.casting_table", "Casting Table");
        add(RUNE_ANVIL.get(), "Rune Anvil");
        add(RUNE_BLOCK.get(), "Runes");
        
        add(FIRE_CLAY_BALL.get(), "Fire Clay");
        add(WHITTLING_KNIFE.get(), "Whittling Knife");
        add(RUNE_SMITH_HAMMER.get(), "Rune Smith's Hammer");
        add(PLACE_HOLDER_RUNE.get(), "Empty");
        
        add(BLANK_MOLD.get(), "Blank Mold");
        add(NUGGET_MOLD.get(), "Nugget Mold");
        add(NUGGET_MOLD_REUSABLE.get(), "Reusable Nugget Mold");
        add(INGOT_MOLD.get(), "Ingot Mold");
        add(INGOT_MOLD_REUSABLE.get(), "Reusable Ingot Mold");
        add(BLOCK_MOLD.get(), "Block Mold");
        add(BLOCK_MOLD_REUSABLE.get(), "Reusable Block Mold");
        
        // Advancements
        add("advancements.nnp_rune_smithing.give_guide_book.description", "Gives new players a guide");
        add("advancements.nnp_rune_smithing.give_guide_book.title", "Give Guide");
        
        // Tags
        
        // JEI
        add("jei.nnp_rune_smithing.tier", "Tier");
        
        // Wierd automatic undocumented Jade stuff that crashes reload listeners if it isn't defined
        add("config.jade.plugin_nnp_rune_smithing.rune_anvil", "Rune Anvil");
        add("config.jade.plugin_nnp_rune_smithing.inlaid_itemstack", "ItemStacks in World");
    }
    
    /**
     *
     */
    static String pathFromHolder(DeferredHolder<?, ?> holder) {
        
        return holder.getId().getPath();
    }
    
    /**
     *
     */
    static String extractAndFormatName(String rawName, boolean trimLastWord) {
        // Compiler says there's no possibility that we need to use StringBuffer.
        // Do it anyway if there are multithreading errors - FML does like to multithread, and we may reuse this
        StringBuilder name = new StringBuilder(rawName.length());
        Arrays.stream(rawName.split("_"))
                .map(RSLanguageProvider_EN_US::cFChar)
                .forEach(word -> {
                    name.append(word);
                    name.append(' ');
                });
        // We always need to run this, since trimLastWord only trims characters after the final space
        name.deleteCharAt(name.length() - 1);
        if (trimLastWord) {
            trimLastWord(name);
            // Delete trailing space
        }
        
        return name.toString();
    }
    
    /**
     * Removes the last word, which often refers to metadata and shouldn't be present in the "core" name.
     */
    static void trimLastWord(StringBuilder name) {
        int startIndex = name.lastIndexOf(" ");
        
        // For some reason, these indexes aren't zero indexed. The last character is actually at length.
        if (startIndex != -1) {
            name.delete(startIndex, name.length());
        }
    }
    
    /**
     * Capitalize first character. No ops on empty strings.
     * <p>
     * Might be faster with StringBuilder. Unsure, and unlikely to matter.
     * </p>
     */
    static String cFChar(String name) {
        
        return !name.isEmpty() ? name.substring(0, 1).toUpperCase() + name.substring(1) : name;
    }
}
