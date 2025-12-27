package com.github.no_name_provided.nnp_rune_smithing.mixins;

import com.github.no_name_provided.nnp_rune_smithing.common.attachments.RSAttachments;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TargetingConditions.class)
public abstract class NNP_Rune_Smithing_TargetingConditions {
    
    @Shadow
    private boolean testInvisible;
    
    @Inject(method = "test(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At("RETURN"), cancellable = true)
    private void test(LivingEntity attacker, LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && testInvisible && target.getExistingData(RSAttachments.HIDDEN_BY_VOID).orElse(false)) {
            double distanceSquared = attacker.distanceToSqr(target.getX(), target.getY(), target.getZ());
            
            // Constant based on minimum range for vanilla invisibility (since our version just hides armor,
            // and this is presumably just the value used for vanilla invisible entities when no armor is equipped)
            cir.setReturnValue(distanceSquared < 4.0);
        }
    }
}
