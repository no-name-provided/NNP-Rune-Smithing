package com.github.no_name_provided.nnp_rune_smithing.common.items;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class WhittlingKnife extends Item {
    public WhittlingKnife(Item.Properties properties) {
        super(properties.durability(32));
    }
    
    /**
     * ItemStack sensitive version of {@link Item#getCraftingRemainingItem()}.
     * Returns a full ItemStack instance of the result.
     *
     * @param itemStack The current ItemStack
     * @return The resulting ItemStack
     */
    @Override
    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        ItemStack result = itemStack.copy();
        result.setDamageValue(result.getDamageValue() + 1);
        
        return result.getDamageValue() == result.getMaxDamage() || !result.has(DataComponents.MAX_DAMAGE) ? ItemStack.EMPTY : result;
    }
    
    /**
     * ItemStack sensitive version of {@link Item#hasCraftingRemainingItem()}.
     *
     * @param stack The current item stack
     * @return True if this item has a crafting remaining item
     */
    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        
        return true;
    }
}
