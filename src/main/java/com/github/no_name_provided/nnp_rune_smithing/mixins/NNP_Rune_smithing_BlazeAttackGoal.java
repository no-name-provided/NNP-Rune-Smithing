package com.github.no_name_provided.nnp_rune_smithing.mixins;

import com.github.no_name_provided.nnp_rune_smithing.common.attachments.RSAttachments;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Blaze;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(targets = "net.minecraft.world.entity.monster.Blaze$BlazeAttackGoal")
public abstract class NNP_Rune_smithing_BlazeAttackGoal extends Goal {
    
    @Shadow @Final
    private Blaze blaze;
    
    /**
     * By default, there is a loop which executes exactly once, producing one fireball per applicable tick. This simply
     * increases the number of times the loop iterates, turning each projectile into a shotgun effect.
     */
    @ModifyConstant(method = "tick()V", constant = @Constant(intValue = 1, ordinal = 6))
    private int nnp_rune_smithing_fireBallPerAttackTickBuff(int numberOfLoops) {
        
        return this.blaze.getExistingData(RSAttachments.INFLAMED_BLAZE).orElse(false) ?
                numberOfLoops + 6 : numberOfLoops;
    }
}
