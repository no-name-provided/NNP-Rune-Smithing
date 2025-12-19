package com.github.no_name_provided.nnp_rune_smithing.mixins;

import com.github.no_name_provided.nnp_rune_smithing.common.attachments.RSAttachments;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes most skeletons actually use attachment-based attack rate.
 */
@Mixin(AbstractSkeleton.class)
public abstract class NNP_Rune_Smithing_Abstract_Skeleton extends Monster implements RangedAttackMob {
    
    private NNP_Rune_Smithing_Abstract_Skeleton(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }
    
    @Inject(method = "getHardAttackInterval()I", at = @At("HEAD"), cancellable = true)
    private void getHardAttackInterval(CallbackInfoReturnable<Integer> cir) {
        if (this.getExistingData(RSAttachments.RAPIDLY_FIRING).orElse(false)) {
            cir.setReturnValue(10);
            
            cir.cancel();
        }
    }
    
    @Inject(method = "getAttackInterval()I", at = @At("HEAD"), cancellable = true)
    private void getAttackInterval(CallbackInfoReturnable<Integer> cir) {
        if (this.getExistingData(RSAttachments.RAPIDLY_FIRING).orElse(false)) {
            cir.setReturnValue(20);
            
            cir.cancel();
        }
    }
    
}
