package com.github.no_name_provided.nnp_rune_smithing.common.advancements.instances;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Optional;

import static com.github.no_name_provided.nnp_rune_smithing.common.advancements.RSAdvancements.USED_RUNE_ANVIL;

public record UsedRuneAnvilTriggerInstance(Optional<ContextAwarePredicate> player, Optional<ItemPredicate> base,
                                           Optional<ItemPredicate> addition) implements SimpleCriterionTrigger.SimpleInstance {
    
    public static Criterion<UsedRuneAnvilTriggerInstance> instance(@Nullable ContextAwarePredicate player, @Nullable ItemPredicate base, @Nullable ItemPredicate addition) {
        // These three lines exist solely because someone, somewhere decided that using optionals for parameters is a style violation.
        // If I can't find a better reason than "it offends someone else's sense of aesthetics", I'm going to just disable this inspection.
        // (The Minecraft devs apparently already have, since they use optionals as inputs in their own advancement instance factories)
        Optional<ContextAwarePredicate> pOption = null == player ? Optional.empty() : Optional.of(player);
        Optional<ItemPredicate> bOption = null == base ? Optional.empty() : Optional.of(base);
        Optional<ItemPredicate> aOption = null == addition ? Optional.empty() : Optional.of(addition);
        
        return USED_RUNE_ANVIL.get().createCriterion(new UsedRuneAnvilTriggerInstance(pOption, bOption, aOption));
    }
    
    public boolean matches(ItemStack base, ItemStack addition) {
        
        return (base().isEmpty() || base().get().test(base)) && (addition().isEmpty() || addition().get().test(addition));
    }
    
    public static Codec<UsedRuneAnvilTriggerInstance> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(UsedRuneAnvilTriggerInstance::player),
                    ItemPredicate.CODEC.optionalFieldOf("base").forGetter(UsedRuneAnvilTriggerInstance::base),
                    ItemPredicate.CODEC.optionalFieldOf("addition").forGetter(UsedRuneAnvilTriggerInstance::addition)
            ).apply(instance, UsedRuneAnvilTriggerInstance::new)
    );
}
