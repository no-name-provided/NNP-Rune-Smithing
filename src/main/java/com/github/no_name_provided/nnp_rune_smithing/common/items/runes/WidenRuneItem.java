package com.github.no_name_provided.nnp_rune_smithing.common.items.runes;

import com.github.no_name_provided.nnp_rune_smithing.common.items.runes.molds.AbstractRuneMold;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

import static com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems.WIDEN_RUNE;

public class WidenRuneItem extends AbstractRuneItem {
    public WidenRuneItem(Item.Properties properties) {
        super(properties);
    }
    
    @Override
    public Type getType() {
        return Type.MODIFIER;
    }
    
    public static class Mold extends AbstractRuneMold {
        public Mold(Properties properties) {
            super(properties, WIDEN_RUNE);
        }
    }
}