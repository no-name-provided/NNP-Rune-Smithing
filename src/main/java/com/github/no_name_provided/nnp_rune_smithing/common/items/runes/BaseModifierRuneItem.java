package com.github.no_name_provided.nnp_rune_smithing.common.items.runes;

import com.github.no_name_provided.nnp_rune_smithing.common.items.runes.molds.AbstractRuneMold;

import java.util.function.Supplier;

public class BaseModifierRuneItem extends AbstractRuneItem {
    public BaseModifierRuneItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public Type getType() {
        return Type.MODIFIER;
    }
    
    public static class Mold extends AbstractRuneMold {
        public Mold(Properties properties, Supplier<AbstractRuneItem> rune) {
            super(properties, rune);
        }
    }
}