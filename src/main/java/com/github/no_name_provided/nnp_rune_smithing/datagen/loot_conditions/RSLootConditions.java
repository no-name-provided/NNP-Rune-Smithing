package com.github.no_name_provided.nnp_rune_smithing.datagen.loot_conditions;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;

public class RSLootConditions {
    public static final DeferredRegister<LootItemConditionType> LOOT_CONDITION_TYPES =
            DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, MODID);
    
    public static final Supplier<LootItemConditionType> BOOLEAN_ATTACHMENT_TRUE =
            LOOT_CONDITION_TYPES.register("boolean_attachment_true", () -> new LootItemConditionType(BooleanAttachmentTrue.CODEC));
    
    public static void register(IEventBus bus) {
        LOOT_CONDITION_TYPES.register(bus);
    }
}
