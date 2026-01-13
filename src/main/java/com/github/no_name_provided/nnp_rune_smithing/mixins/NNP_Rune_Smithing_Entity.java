package com.github.no_name_provided.nnp_rune_smithing.mixins;

import com.github.no_name_provided.nnp_rune_smithing.common.attachments.RSAttachments;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class NNP_Rune_Smithing_Entity {
    
    @Inject(method = "canFreeze()Z", at = @At("HEAD"), cancellable = true)
    private void nnp_rune_smithing_canFreeze(CallbackInfoReturnable<Boolean> cir) {
        if (((Entity) (Object) this).getExistingData(RSAttachments.COLD_RESISTANCE).orElse((byte) 0) >= 1) {
            
            cir.setReturnValue(false);
        }
    }
    
}
