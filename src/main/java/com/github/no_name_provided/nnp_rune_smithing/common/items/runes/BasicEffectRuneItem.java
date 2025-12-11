package com.github.no_name_provided.nnp_rune_smithing.common.items.runes;

import com.github.no_name_provided.nnp_rune_smithing.common.items.interfaces.CastingMold;
import com.github.no_name_provided.nnp_rune_smithing.common.items.runes.molds.AbstractRuneMold;

import java.util.function.Supplier;

public class BasicEffectRuneItem extends AbstractRuneItem {
    public BasicEffectRuneItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public Type getType() {
        return Type.EFFECT;
    }
    
    public static class Mold extends AbstractRuneMold implements CastingMold {
        
        public Mold(Properties properties, Supplier<AbstractRuneItem> rune) {
            super(properties, rune);
        }
    }
}
