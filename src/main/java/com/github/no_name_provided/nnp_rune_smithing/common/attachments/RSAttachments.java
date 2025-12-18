package com.github.no_name_provided.nnp_rune_smithing.common.attachments;

import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;

public class RSAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MODID);
    
    // No serialization (saving)
    public static final Supplier<AttachmentType<Boolean>> SHOW_RUNE_BLOCK_BOUNDING_BOXES = ATTACHMENT_TYPES.register(
            "show_rune_block_bounding_boxes", () -> AttachmentType.builder(() -> false)
                    .sync((holder, to) -> holder == to, ByteBufCodecs.BOOL)
                    .build()
    );
    
    // Keep track of augmented mobs, for loot modifiers, renderers, and other special effects
    
    // Charms
    public static final Supplier<AttachmentType<Boolean>> ROBUST_ZOMBIE = registerSimpleBoolean("robust");
    public static final Supplier<AttachmentType<Boolean>> POISONOUS_ZOMBIE = registerSimpleBoolean("poisonous");
    public static final Supplier<AttachmentType<Boolean>> LUCKY_SKELETON = registerSimpleBoolean("lucky");
    
    // Effects
    public static final Supplier<AttachmentType<Boolean>> BLAST_PROOF = registerSimpleBoolean("blast_proof");
    public static final Supplier<AttachmentType<Boolean>> FAR_SIGHTED = registerSimpleBoolean("far_sighted");
    public static final Supplier<AttachmentType<Boolean>> AQUATIC = registerSimpleBoolean("aquatic");
    public static final Supplier<AttachmentType<Boolean>> RAVENOUS = registerSimpleBoolean("ravenous");
    public static final Supplier<AttachmentType<Boolean>> GALE_BREEZE = registerSimpleBoolean("gale_breeze");
    public static final Supplier<AttachmentType<Boolean>> INFLAMED_BLAZE = registerSimpleBoolean("inflamed_blaze");
    public static final Supplier<AttachmentType<Boolean>> VOID_ENDERMAN = registerSimpleBoolean("void_enderman");
    public static final Supplier<AttachmentType<Boolean>> RADIANT_SHULKER = registerSimpleBoolean("radiant_shulker");
    
    // Modifiers
    public static final Supplier<AttachmentType<Boolean>> RAPIDLY_FIRING = registerSimpleBoolean("rapidly_firing");
    public static final Supplier<AttachmentType<Boolean>> TINY = registerSimpleBoolean("tiny");
    public static final Supplier<AttachmentType<Boolean>> GIANT = registerSimpleBoolean("giant_slime");
    public static final Supplier<AttachmentType<Boolean>> INVERTED = registerSimpleBoolean("inverted");
    
    // Amplifiers
    public static final Supplier<AttachmentType<Boolean>> CONTAINED = registerSimpleBoolean("contained");
    
    public static void register(IEventBus bus) {
        ATTACHMENT_TYPES.register(bus);
    }
    
    public static Supplier<AttachmentType<Boolean>> registerSimpleBoolean(String name) {
        return ATTACHMENT_TYPES.register(
                name, () -> AttachmentType.builder(() -> false)
                        .serialize(Codec.BOOL)
                        .sync((holder, to) -> holder == to, ByteBufCodecs.BOOL)
                        .build()
        );
    }
}
