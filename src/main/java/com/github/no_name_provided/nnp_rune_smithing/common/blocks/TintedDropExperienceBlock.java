package com.github.no_name_provided.nnp_rune_smithing.common.blocks;

import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.DropExperienceBlock;

public class TintedDropExperienceBlock extends DropExperienceBlock {
    public final int color;
    public TintedDropExperienceBlock(IntProvider xpRange, Properties properties, int color) {
        super(xpRange, properties);
        this.color = color;
    }
}
