package com.github.no_name_provided.nnp_rune_smithing.common.items.runes;

import com.github.no_name_provided.nnp_rune_smithing.common.items.interfaces.CastingMold;
import com.github.no_name_provided.nnp_rune_smithing.common.items.runes.molds.AbstractRuneMold;

import static com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems.WARD_RUNE;

public class WardRuneItem extends AbstractRuneItem {
    public WardRuneItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public Type getType() {
        return Type.EFFECT;
    }
    
    public static class Mold extends AbstractRuneMold implements CastingMold {
        
        public Mold(Properties properties) {
            super(properties, WARD_RUNE);
        }
    }
}
