package com.github.no_name_provided.nnp_rune_smithing.datagen.providers;

import com.github.no_name_provided.nnp_rune_smithing.common.fluids.RSFluidTags;
import com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

import static com.github.no_name_provided.nnp_rune_smithing.common.fluids.FluidHelper.FLUID_SETS;
import static com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems.*;

public class Languages_EN_US extends LanguageProvider {
    public Languages_EN_US(PackOutput output, String modid, String locale) {
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
        RSItems.METAL_STORAGE_BLOCKS.getEntries().forEach((storage_block) -> {
            add(storage_block.get(), "Block of " + cFChar(storage_block.getKey().location().getPath().split("_")[0]));
        });
        RSItems.RUNES.getEntries().forEach(rune -> {
            add(rune.get(), cFChar(rune.getId().getPath().split("_")[0]) + " Rune");
            add("item.nnp_rune_smithing." + rune.getId().getPath().split("_")[0] + "_mold", cFChar(rune.getId().getPath().split("_")[0]) + " Mold");
        });
        add(MELTER.get(), "Melting Furnace");
        add("container.melter", "Melting Furnace");
        add(CASTING_TABLE.get(), "Casting Table");
        add("container.casting_table", "Casting Table");
        add(RUNE_ANVIL.get(), "Rune Anvil");
        
        add(RUNE_SMITH_HAMMER.get(), "Rune Smith's Hammer");
        add(PLACE_HOLDER_RUNE.get(), "Empty");
    }
    
    /**
     * Capitalize first character.
     */
    String cFChar(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
}
