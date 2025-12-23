package com.github.no_name_provided.nnp_rune_smithing.common.items.runes;

import com.github.no_name_provided.nnp_rune_smithing.common.datamaps.CastableFluidData;
import com.github.no_name_provided.nnp_rune_smithing.common.datamaps.RSDataMaps;
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
        Affinity(String name) {
            this.name = name + "_affinity";
        }
        public String getName() {
            return this.name;
        }
    }
    private final Affinity affinity;
    private final int minimumTier;
    
    public ElementalRuneItem(Properties properties, Affinity affinity) {
        this(properties, affinity, 2);
    }
    
    public ElementalRuneItem(Properties properties, Affinity affinity, int minimumTier) {
        super(properties);
        this.affinity = affinity;
        this.minimumTier = minimumTier;
    }
    
    @Override
    public Type getType() {
        return Type.EFFECT;
    }
    
    @Override
    public boolean validateFluid(FluidStack fluid) {
        CastableFluidData data = fluid.getFluidHolder().getData(RSDataMaps.CASTABLE_FLUID_DATA);
        if (null != data) {
            return data.tier() >= minimumTier || fluid.is(getFluidTag(affinity.getName()));
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
