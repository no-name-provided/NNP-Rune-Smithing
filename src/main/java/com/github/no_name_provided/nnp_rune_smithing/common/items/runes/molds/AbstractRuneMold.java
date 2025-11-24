package com.github.no_name_provided.nnp_rune_smithing.common.items.runes.molds;

import com.github.no_name_provided.nnp_rune_smithing.common.data_components.RuneData;
import com.github.no_name_provided.nnp_rune_smithing.common.fluids.MoltenMetalFluidType;
import com.github.no_name_provided.nnp_rune_smithing.common.items.interfaces.CastingMold;
import com.github.no_name_provided.nnp_rune_smithing.common.items.runes.AbstractRuneItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

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
    public @NotNull ItemStack getResult(FluidStack fluid) {
        // Validated in #validateFluid. Capability shouldn't allow any other fluid type to be added.
        MoltenMetalFluidType fluidType = (MoltenMetalFluidType)fluid.getFluidType();
        ItemStack output = new ItemStack(RUNE.get(), 1);
        output.set(RUNE_DATA, new RuneData(fluidType.TIER, fluidType.COLOR_WHEN_COOL));
        return output;
    }
}
