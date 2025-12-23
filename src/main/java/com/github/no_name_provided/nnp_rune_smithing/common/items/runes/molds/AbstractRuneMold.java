package com.github.no_name_provided.nnp_rune_smithing.common.items.runes.molds;

import com.github.no_name_provided.nnp_rune_smithing.common.data_components.RuneData;
import com.github.no_name_provided.nnp_rune_smithing.common.datamaps.CastableFluidData;
import com.github.no_name_provided.nnp_rune_smithing.common.datamaps.RSDataMaps;
import com.github.no_name_provided.nnp_rune_smithing.common.items.interfaces.CastingMold;
import com.github.no_name_provided.nnp_rune_smithing.common.items.runes.AbstractRuneItem;
import com.mojang.logging.LogUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.function.Supplier;

import static com.github.no_name_provided.nnp_rune_smithing.common.data_components.RSDataComponents.RUNE_DATA;

public abstract class AbstractRuneMold extends Item implements CastingMold {
    public final Supplier<AbstractRuneItem> RUNE;
    @SuppressWarnings("FieldCanBeLocal")
    private final int MILLIBUCKETS_PER_INGOT = 144;
    
    public AbstractRuneMold(Properties properties, Supplier<AbstractRuneItem> rune) {
        super(properties);
        RUNE = rune;
    }
    
    /**
     * @return Fluid consumed per craft.
     */
    @Override
    public int amountRequired() {
        return 3 * MILLIBUCKETS_PER_INGOT;
    }
    
    @Override
    public boolean validateFluid(FluidStack fluid) {
        return RUNE.get().validateFluid(fluid) && fluid.getAmount() >= amountRequired();
    }
    
    @Override
    public ItemStack getResult(FluidStack fluid) {
        ItemStack output = new ItemStack(RUNE.get(), 1);
        // Validated in #validateFluid. Capability shouldn't allow any unmapped fluid to be added
        CastableFluidData data = fluid.getFluidHolder().getData(RSDataMaps.CASTABLE_FLUID_DATA);
        // Still doing a nullability check, but logging a warning if it fails
        if (null != data) {
            output.set(RUNE_DATA, new RuneData(data.tier(), data.colorWhenCool()));
        } else {
            LogUtils.getLogger().error("Could not get color and tier for rune {} because fluid has no corresponding CastableFluid DataMap.", fluid);
            output.set(RUNE_DATA, new RuneData(1, 0));
        }
        
        return output;
    }
}
