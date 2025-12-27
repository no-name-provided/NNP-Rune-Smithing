package com.github.no_name_provided.nnp_rune_smithing.mixins;

import com.github.no_name_provided.nnp_rune_smithing.common.RSServerConfig;
import com.github.no_name_provided.nnp_rune_smithing.common.attachments.RSAttachments;
import net.minecraft.world.entity.Attackable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class NNP_Rune_Smithing_LivingEntity extends Entity implements Attackable, net.neoforged.neoforge.common.extensions.ILivingEntityExtension {
    
    private NNP_Rune_Smithing_LivingEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }
    
    @Inject(method = "canBeSeenByAnyone()Z", at = @At("HEAD"), cancellable = true)
    private void nnp_rune_smithing_canBeSeenByAnyone(CallbackInfoReturnable<Boolean> cir) {
        if (getExistingData(RSAttachments.HIDDEN_BY_VOID).orElse(false) && RSServerConfig.voidRuneInvisibilityWorksOnMobs) {
            
            cir.setReturnValue(false);
        }
    }
}
