package com.github.no_name_provided.nnp_rune_smithing.common.items.runes;

import com.github.no_name_provided.nnp_rune_smithing.common.items.runes.molds.AbstractRuneMold;

import static com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems.AMPLIFY_RUNE;

public class AmplifyRuneItem extends AbstractRuneItem {
    public AmplifyRuneItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public Type getType() {
        return Type.AMPLIFIER;
    }
    
    public static class Mold extends AbstractRuneMold {
        public Mold(Properties properties) {
            super(properties, AMPLIFY_RUNE);
        }
    }
}
