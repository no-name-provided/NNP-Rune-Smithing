package com.github.no_name_provided.nnp_rune_smithing.mixins;

import com.github.no_name_provided.nnp_rune_smithing.common.attachments.RSAttachments;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LightLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Client only. Allows entities with light runes on armor to glow.
 */
@Mixin(EntityRenderer.class)
public abstract class NNP_Rune_Smithing_EntityRenderer<T extends Entity> {
    
    @Inject(method = "getBlockLightLevel(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;)I", at = @At("HEAD"), cancellable = true)
    private void nnp_rune_smithing_getBlockLightLevel(T entity, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        if (entity.hasData(RSAttachments.LIGHT_FROM_ARMOR)) {
            
            cir.setReturnValue(Math.max(entity.getData(RSAttachments.LIGHT_FROM_ARMOR), entity.level().getBrightness(LightLayer.BLOCK, pos)));
            cir.cancel();
        }
    }
}
