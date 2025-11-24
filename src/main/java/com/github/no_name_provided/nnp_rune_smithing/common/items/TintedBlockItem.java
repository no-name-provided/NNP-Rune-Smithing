package com.github.no_name_provided.nnp_rune_smithing.common.items;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

public class TintedBlockItem extends BlockItem {
    public final int COLOR;
    
    public TintedBlockItem(Block block, Properties properties, int color) {
        super(block, properties);
        COLOR = color;
    }
}
