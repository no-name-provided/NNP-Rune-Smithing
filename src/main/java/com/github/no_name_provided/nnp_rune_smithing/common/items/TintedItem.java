package com.github.no_name_provided.nnp_rune_smithing.common.items;

import net.minecraft.world.item.Item;

public class TintedItem extends Item {
    final public int COLOR;
    
    public TintedItem(Properties properties, int color) {
        super(properties);
        COLOR = color | 0xFF000000;
    }
}
