package com.github.no_name_provided.nnp_rune_smithing.datagen.loot_conditions;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Set;

// If there were documentation for it, this would probably be an entity (sub)predicate...
public record BooleanAttachmentTrue(AttachmentType<Boolean> attachment) implements LootItemCondition {
    // Add the context we need for this condition. In our case, this will be the xp level the player must have.
    public static final MapCodec<BooleanAttachmentTrue> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            // Technically, I should probably be fiddling with partial maps and data results here...
            NeoForgeRegistries.ATTACHMENT_TYPES.byNameCodec().fieldOf("attachment")
                    // We need to cast here, because the compiler will check types and will complain that Attachment<?> may not be Attachment<?>.
                    .xmap(a -> (AttachmentType<Boolean>) a, b -> b)
                    .forGetter(BooleanAttachmentTrue::attachment)
    ).apply(inst, BooleanAttachmentTrue::new));
    
    // Evaluates the condition here. Get the required loot context parameters from the provided LootContext.
    // In our case, we want the KILLER_ENTITY to have at least our required level.
    @Override
    public boolean test(LootContext context) {
        Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (null == entity) {
            
            return false;
        }
        Boolean attachmentValue = entity.getExistingDataOrNull(attachment());
        
        return null != attachmentValue ? attachmentValue : false;
    }
    
    // Tell the game what parameters we expect from the loot context. Used in validation.
    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.THIS_ENTITY);
    }
    
    @Override
    public LootItemConditionType getType() {
        return RSLootConditions.BOOLEAN_ATTACHMENT_TRUE.get();
    }
}
