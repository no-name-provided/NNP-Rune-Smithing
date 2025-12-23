package com.github.no_name_provided.nnp_rune_smithing.datagen.providers;

import com.github.no_name_provided.nnp_rune_smithing.common.items.CastingTemplate;
import com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

import java.util.Objects;

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
            add(set.bucket().get(), "Molten " + cFChar(set.type().getRegisteredName().split(":")[1]) + " Bucket");
            add(set.block().get(), "Molten " + cFChar(set.type().getRegisteredName().split(":")[1]) + " Block");
            add("fluid_type." + set.type().getRegisteredName().replace(':', '.'),
                    "Molten " + cFChar(set.type().getRegisteredName().split(":")[1]));
        });
        RSItems.NUGGETS.getEntries().forEach((nugget) -> {
            add(nugget.get(), cFChar(nugget.getKey().location().getPath().split("_")[0]) + " Nugget");
        });
        RSItems.INGOTS.getEntries().forEach((ingot) -> {
            add(ingot.get(), cFChar(ingot.getKey().location().getPath().split("_")[0]) + " Ingot");
        });
        RSItems.METAL_STORAGE_BLOCKS.getEntries().forEach((storageBlock) -> {
            add(storageBlock.get(), "Block of " + cFChar(storageBlock.getKey().location().getPath().split("_")[0]));
        });
        RAW_ORES.getEntries().forEach((rawOre) -> {
            add(rawOre.get(), "Raw " + cFChar(rawOre.getKey().location().getPath().split("_")[1]));
        });
        ORE_BLOCKS.getEntries().forEach((ore_block) -> {
            String name = ore_block.getKey().location().getPath().split("_")[0];
            String type = "";
            if (Objects.equals(name, "deepslate") || Objects.equals(name, "netherrack") || Objects.equals(name, "endstone")) {
                type = name + " ";
                name = ore_block.getKey().location().getPath().split("_")[1];
            }
            add(ore_block.get(), cFChar(type) + cFChar(name) + " Ore");
        });
        WOODEN_CHARMS.getEntries().forEach(charm -> {
            add(charm.get(), cFChar(charm.getId().getPath().split("_")[0]) + " Charm");
        });
        RSItems.RUNES.getEntries().forEach(rune -> {
            add(rune.get(), cFChar(rune.getId().getPath().split("_")[0]) + " Rune");
            add("item.nnp_rune_smithing." + rune.getId().getPath().split("_")[0] + "_mold", cFChar(rune.getId().getPath().split("_")[0]) + " Mold");
        });
        ITEMS.getEntries().stream()
                .filter(holder -> holder.get() instanceof CastingTemplate)
                .forEach(templateHolder -> {
                    String name = cFChar(templateHolder.getId().getPath().split("_")[0]);
                    add(templateHolder.get(), name + " Template");
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
     * Capitalize first character. No ops on empty strings.
     */
    String cFChar(String name) {
        return !name.isEmpty() ? name.substring(0, 1).toUpperCase() + name.substring(1) : name;
    }
}
