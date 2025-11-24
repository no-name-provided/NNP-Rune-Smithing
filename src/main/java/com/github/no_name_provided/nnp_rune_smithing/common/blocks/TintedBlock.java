package com.github.no_name_provided.nnp_rune_smithing.common.blocks;

import net.minecraft.world.level.block.Block;

public class TintedBlock extends Block {
    public final int COLOR;
    
    public TintedBlock(Properties properties, int color) {
        super(properties);
        COLOR = color;
    }
}
