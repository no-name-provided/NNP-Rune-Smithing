package com.github.no_name_provided.nnp_rune_smithing.common.items;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class LayeredTintedBlockItem extends BlockItem {
    public final List<Integer> colors;
    
    public LayeredTintedBlockItem(Block block, Properties properties, List<Integer> colorByLayer) {
        super(block, properties);
        colors = colorByLayer;
    }
}
