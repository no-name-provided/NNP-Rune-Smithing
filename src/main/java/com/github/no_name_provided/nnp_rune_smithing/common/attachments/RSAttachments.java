package com.github.no_name_provided.nnp_rune_smithing.common.attachments;

import net.minecraft.core.Direction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

import static net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion.MOD_ID;

public class RSAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MOD_ID);
    
    // No serialization
    public static final Supplier<AttachmentType<Direction>> WARD_RUNE_PUSH_DIRECTION = ATTACHMENT_TYPES.register(
            "ward_rune_push_direction", () -> AttachmentType.builder(() -> Direction.NORTH).build()
    );
    
    public static void register(IEventBus bus) {
        ATTACHMENT_TYPES.register(bus);
    }
}
