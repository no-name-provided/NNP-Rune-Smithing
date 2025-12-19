package com.github.no_name_provided.nnp_rune_smithing.mixins;

import com.github.no_name_provided.nnp_rune_smithing.common.attachments.RSAttachments;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PowerableMob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Stops blast proof creepers from self-destructing when they explode. This is normally hardcoded.
 */
@Mixin(Creeper.class)
public abstract class NNP_Rune_Smithing_Creeper extends Monster implements PowerableMob {
    @Shadow
    private int explosionRadius;
    
    private NNP_Rune_Smithing_Creeper(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }
    
    @Inject(method = "explodeCreeper()V", at = @At("HEAD"), cancellable = true)
    private void nnp_rune_smithing_explodeCreeper(CallbackInfo ci) {
        if (!this.level().isClientSide && this.getExistingData(RSAttachments.BLAST_PROOF).orElse(false)) {
            float f = this.isPowered() ? 2.0F : 1.0F;
            this.level().explode(this, this.getX(), this.getY(), this.getZ(), (float)this.explosionRadius * f, Level.ExplosionInteraction.MOB);
            this.spawnLingeringCloud();
            
            ci.cancel();
        }
    }
    
    @Shadow
    protected abstract void spawnLingeringCloud();
}
