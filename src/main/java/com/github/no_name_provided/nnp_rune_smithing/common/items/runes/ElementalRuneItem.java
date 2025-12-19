package com.github.no_name_provided.nnp_rune_smithing.common.items.runes;

import com.github.no_name_provided.nnp_rune_smithing.common.fluids.MoltenMetalFluidType;
import com.github.no_name_provided.nnp_rune_smithing.common.items.interfaces.CastingMold;
import com.github.no_name_provided.nnp_rune_smithing.common.items.runes.molds.AbstractRuneMold;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.function.Supplier;

import static com.github.no_name_provided.nnp_rune_smithing.common.fluids.RSFluidTags.getFluidTag;

public class ElementalRuneItem extends AbstractRuneItem {
    public enum Affinity {
        EARTH("earth"),
        AIR("air"),
        FIRE("fire"),
        WATER("water"),
        VOID("void"),
        LIGHT("light");
        private final String name;
        private Affinity(String name) {
            this.name = name + "_affinity";
        }
        private String getName() {
            return this.name;
        }
    }
    private final Affinity affinity;
    
    public ElementalRuneItem(Properties properties, Affinity affinity) {
        super(properties);
        this.affinity = affinity;
    }
    
    @Override
    public Type getType() {
        return Type.EFFECT;
    }
    
    @Override
    public boolean validateFluid(FluidStack fluid) {
        if (fluid.getFluidType() instanceof MoltenMetalFluidType type) {
            return type.TIER >= 2 || fluid.is(getFluidTag(affinity.getName()));
        } else {
            return false;
        }
    }
    
    public static class Mold extends AbstractRuneMold implements CastingMold {
        
        public Mold(Properties properties, Supplier<AbstractRuneItem> rune) {
            super(properties, rune);
        }
    }
}
