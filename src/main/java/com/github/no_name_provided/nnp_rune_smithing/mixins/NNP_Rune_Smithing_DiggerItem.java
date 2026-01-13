package com.github.no_name_provided.nnp_rune_smithing.mixins;

import com.github.no_name_provided.nnp_rune_smithing.common.data_components.RSDataComponents;
import com.github.no_name_provided.nnp_rune_smithing.common.data_components.RunesAdded;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import static com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems.SELF_RUNE;
import static com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems.WARD_RUNE;

@Mixin(DiggerItem.class)
public abstract class NNP_Rune_Smithing_DiggerItem {
    
    @ModifyArg(method = "postHurtEnemy(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V"))
    private int nnp_rune_smithing_removeExtraToolDamageFromAttackingAnEntity(int amount, @Local(argsOnly = true)ItemStack stack) {
        RunesAdded runes = stack.get(RSDataComponents.RUNES_ADDED);
        if (runes != null) {
            
            // Reduce damage from 2 to 1 in the vanilla case. May have reduced effect in a modded environment
            return runes.target().rune() == SELF_RUNE.get() && runes.effect().rune() == WARD_RUNE.get() ? Mth.clamp(amount - 1, 1, amount) : amount;
        } else {
            
            return amount;
        }
    }
}
