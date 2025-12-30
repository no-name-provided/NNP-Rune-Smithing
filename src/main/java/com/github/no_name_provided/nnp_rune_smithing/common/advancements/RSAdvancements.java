package com.github.no_name_provided.nnp_rune_smithing.common.advancements;

import com.github.no_name_provided.nnp_rune_smithing.common.advancements.triggers.UsedRuneAnvilTrigger;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;

public class RSAdvancements {
    public static DeferredRegister<CriterionTrigger<?>> CRITERION_TRIGGERS =
            DeferredRegister.create(Registries.TRIGGER_TYPE, MODID);
    
    public static Supplier<UsedRuneAnvilTrigger> USED_RUNE_ANVIL = CRITERION_TRIGGERS.register(
            "used_rune_anvil",
            UsedRuneAnvilTrigger::new
    );
    
    public static void register(IEventBus bus) {
        CRITERION_TRIGGERS.register(bus);
    }
}
