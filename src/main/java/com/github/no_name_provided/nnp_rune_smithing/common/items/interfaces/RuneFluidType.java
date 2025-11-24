package com.github.no_name_provided.nnp_rune_smithing.common.items.interfaces;

public interface RuneFluidType {
    default int getTier() {
        return 1;
    };
    default int getColor() {
        return 0;
    }
}
