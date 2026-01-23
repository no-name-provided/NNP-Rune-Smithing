package com.github.no_name_provided.nnp_rune_smithing.common.advancements.triggers;

import com.github.no_name_provided.nnp_rune_smithing.common.advancements.instances.UsedRuneAnvilTriggerInstance;
import com.mojang.serialization.Codec;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import static com.github.no_name_provided.nnp_rune_smithing.common.advancements.instances.UsedRuneAnvilTriggerInstance.CODEC;

/**
 * Fired whenever a "rune" is inlaid using the rune anvil.
 */
public class UsedRuneAnvilTrigger extends SimpleCriterionTrigger<UsedRuneAnvilTriggerInstance> {
    
    @Override
    public Codec<UsedRuneAnvilTriggerInstance> codec() {
        return CODEC;
    }
    
    /**
     * Call this method to grant advancements locked behind this trigger.
     *
     * @param player The player doing the enhancing.
     * @param base The item being enhanced.
     * @param addition The rune being inlaid.
     */
    public void trigger(ServerPlayer player, ItemStack base, ItemStack addition) {
        this.trigger(
                player,
                triggerInstance -> triggerInstance.matches(base, addition)
        );
    }
}
