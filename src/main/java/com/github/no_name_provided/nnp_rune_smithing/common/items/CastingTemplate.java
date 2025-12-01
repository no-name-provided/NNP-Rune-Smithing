package com.github.no_name_provided.nnp_rune_smithing.common.items;

import com.github.no_name_provided.nnp_rune_smithing.common.items.interfaces.CastingMold;
import com.github.no_name_provided.nnp_rune_smithing.common.items.runes.WardRuneItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.Supplier;

public class CastingTemplate extends Item {
    private final Supplier<? extends CastingMold> MOLD_CREATED;
    public CastingTemplate(Properties properties, DeferredHolder<Item, ? extends CastingMold> moldCreated) {
        super(properties);
        MOLD_CREATED = moldCreated;
    }
    
    public CastingMold getMold() {
        return MOLD_CREATED.get();
    }
    
    @Override
    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        // Should be the itemstack used for crafting
        return itemStack.copy();
    }
    
    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }
}
