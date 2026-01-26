package com.github.no_name_provided.nnp_rune_smithing.common.data_components;

import com.mojang.serialization.Codec;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Why not just use a fluid stack? DataComponents must be immutable, so we use a record as a wrapper.
 *
 * @param tank The fluid and quantity in the tank.
 */
public record SingleTankContents(FluidStack tank) {
    public static Codec<SingleTankContents> CODEC = FluidStack.OPTIONAL_CODEC.xmap(
            SingleTankContents::new, SingleTankContents::tank
    );
}
