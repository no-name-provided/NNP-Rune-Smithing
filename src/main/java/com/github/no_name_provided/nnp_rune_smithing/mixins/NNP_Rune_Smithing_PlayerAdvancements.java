package com.github.no_name_provided.nnp_rune_smithing.mixins;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Patches a bug in Neo (and yes, it contradicts the docstring so it is a bug,
 * and not just another grossly incompetent - or sadistic - design decision).
 * This bug causes the advancement earn event to only fire when the advancement isn't hidden.
 * It exists because the Neo mixin is one line early, and therefore inside an extraneous conditional. This
 * handles the other case (achievement hidden).
 * <p>
 * Alternate workaround: Use the AdvancementProgress event and check to see if the advancement is finished.
 * This is less efficient, and defeats the purpose of the downstream event.
 * </p>
 * <p>
 * #BlameNeoForge
 * </p>
 */
@Mixin(PlayerAdvancements.class)
public abstract class NNP_Rune_Smithing_PlayerAdvancements {
    @Shadow
    private ServerPlayer player;
    
    @Inject(method = "award(Lnet/minecraft/advancements/AdvancementHolder;Ljava/lang/String;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/AdvancementRewards;grant(Lnet/minecraft/server/level/ServerPlayer;)V", shift = At.Shift.AFTER))
    private void nnp_fixNeoAdvancementEarnEvent(AdvancementHolder advancement, String criterionKey, CallbackInfoReturnable<Boolean> cir) {
        if (advancement.value().display().isEmpty()) {
            //noinspection UnstableApiUsage // Used by method we're patching
            net.neoforged.neoforge.event.EventHooks.onAdvancementEarnedEvent(player, advancement);
        }
    }
}
