package com.github.no_name_provided.nnp_rune_smithing.common.attachments;

import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

import static net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion.MOD_ID;

public class RSAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MOD_ID);
    
    // No serialization (saving)
    public static final Supplier<AttachmentType<Boolean>> SHOW_RUNE_BLOCK_BOUNDING_BOXES = ATTACHMENT_TYPES.register(
            "show_rune_block_bounding_boxes", () -> AttachmentType.builder(() -> false)
                    .sync((holder, to) -> holder == to, ByteBufCodecs.BOOL)
                    .build()
    );
    
    public static void register(IEventBus bus) {
        ATTACHMENT_TYPES.register(bus);
    }
}
