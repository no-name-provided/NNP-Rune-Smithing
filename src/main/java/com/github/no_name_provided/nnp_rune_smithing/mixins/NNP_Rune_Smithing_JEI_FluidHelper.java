package com.github.no_name_provided.nnp_rune_smithing.mixins;

import com.github.no_name_provided.nnp_rune_smithing.common.datamaps.CastableFluidData;
import com.github.no_name_provided.nnp_rune_smithing.common.datamaps.RSDataMaps;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.text.NumberFormat;

// We use a string here because we aren't compiling against JEI internals
@Mixin(targets = "mezz.jei.neoforge.platform.FluidHelper", remap = false)
public class NNP_Rune_Smithing_JEI_FluidHelper {
    
    @Inject(method = "getTooltip(Lmezz/jei/api/gui/builder/ITooltipBuilder;Lnet/neoforged/neoforge/fluids/FluidStack;Lnet/minecraft/world/item/TooltipFlag;)V", at = @At("TAIL"), remap = false)
    private void nnp_rune_smithing_getToolTip(ITooltipBuilder tooltip, FluidStack ingredient, TooltipFlag tooltipFlag, CallbackInfo ci) {
        CastableFluidData data = ingredient.getFluidHolder().getData(RSDataMaps.CASTABLE_FLUID_DATA);
        if (null != data) {
            // No space here. JEI doesn't correctly parse quoted spaces in tooltips, and I want players to be able to search for things like "Tier: 4".
            tooltip.add(Component.translatable("jei.nnp_rune_smithing.tier").append(Component.literal(":" + data.tier())));
            // I'm not actually using my datamap to store the temperature, since that's a property of the fluid type,
            // but I don't want to add tooltip spam to fluids that aren't supported.
            tooltip.add(Component.literal(NumberFormat.getIntegerInstance().format(ingredient.getFluidType().getTemperature()) + " °C"));
        }
    }
}
