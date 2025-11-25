package com.github.no_name_provided.nnp_rune_smithing.common.items.runes;

import com.github.no_name_provided.nnp_rune_smithing.common.items.interfaces.CastingMold;
import com.github.no_name_provided.nnp_rune_smithing.common.items.runes.molds.AbstractRuneMold;

import java.util.function.Supplier;

public class BasicRuneItem extends AbstractRuneItem {
    public BasicRuneItem(Properties properties) {
        super(properties);
    }
    
    public static class Mold extends AbstractRuneMold implements CastingMold {
        
        public Mold(Properties properties, Supplier<AbstractRuneItem> rune) {
            super(properties, rune);
        }
    }
}
