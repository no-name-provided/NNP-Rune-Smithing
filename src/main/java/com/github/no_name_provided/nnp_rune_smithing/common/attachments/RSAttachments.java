package com.github.no_name_provided.nnp_rune_smithing.common.attachments;

import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.HashMap;
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
    
    
    // Levels
    public static final Supplier<AttachmentType<MarkedBlocksFromSightRune>> SIGHT_RUNE_MARKED_BLOCKS = ATTACHMENT_TYPES.register(
            "marked_blocks_from_sight_rune", () -> AttachmentType.builder(() -> new MarkedBlocksFromSightRune(new HashMap<>()))
                    .sync((holder, to) -> true, MarkedBlocksFromSightRune.STREAM_CODEC)
                    .build()
    );
    
    // Keep track of augmented mobs, for loot modifiers, renderers, and other special effects
    
    // Charms
    public static final Supplier<AttachmentType<Boolean>> ROBUST = registerSimpleBoolean("robust");
    public static final Supplier<AttachmentType<Boolean>> POISONOUS = registerSimpleBoolean("poisonous");
    public static final Supplier<AttachmentType<Boolean>> LUCKY = registerSimpleBoolean("lucky");
    
    // Effects
    public static final Supplier<AttachmentType<Boolean>> BLAST_PROOF = registerSimpleBoolean("blast_proof");
    public static final Supplier<AttachmentType<Boolean>> FAR_SIGHTED = registerSimpleBoolean("far_sighted");
    public static final Supplier<AttachmentType<Boolean>> AQUATIC = registerSimpleBoolean("aquatic");
    public static final Supplier<AttachmentType<Boolean>> RAVENOUS = registerSimpleBoolean("ravenous");
    public static final Supplier<AttachmentType<Boolean>> GALE_BREEZE = registerSimpleBoolean("gale_breeze");
    public static final Supplier<AttachmentType<Boolean>> INFLAMED_BLAZE = registerSimpleBoolean("inflamed_blaze");
    public static final Supplier<AttachmentType<Boolean>> VOID_FUSED = registerSimpleBoolean("void_fused");
    public static final Supplier<AttachmentType<Boolean>> RADIANT = registerSimpleBoolean("radiant");
    
    public static final Supplier<AttachmentType<Boolean>> SERENDIPITOUS_BIPED = registerSimpleBoolean("serendipitous_biped");
    public static final Supplier<AttachmentType<Byte>> SERENDIPITY_COUNT = registerSynchronizedTransientByte("serendipity_count");
    public static final Supplier<AttachmentType<Float>> PLAYER_XP_MULTIPLIER = registerSynchronizedFloat("player_xp_multiplier");
    public static final Supplier<AttachmentType<Boolean>> HIDDEN_BY_VOID = registerSynchronizedBoolean("hidden_by_void");
    public static final Supplier<AttachmentType<Byte>> HIDDEN_BY_VOID_COUNT = registerSynchronizedTransientByte("hidden_by_void_count");
    public static final Supplier<AttachmentType<Boolean>> VOID_CONSUMES_DEBUFFS = registerSynchronizedBoolean("void_consumes_debuffs");
    public static final Supplier<AttachmentType<Byte>> VOID_CONSUME_COUNT = registerSynchronizedTransientByte("void_consume_count");
    public static final Supplier<AttachmentType<Long>> BLINDING_FLASH_TIME = registerSynchronizedTransientLong("player_blinded_by_flash");
    public static final Supplier<AttachmentType<Byte>> LIGHT_FROM_ARMOR = registerSynchronizedTransientByte("light_from_armor");
    
    // Modifiers
    public static final Supplier<AttachmentType<Boolean>> RAPIDLY_FIRING = registerSimpleBoolean("rapidly_firing");
    public static final Supplier<AttachmentType<Boolean>> TINY = registerSimpleBoolean("tiny");
    public static final Supplier<AttachmentType<Boolean>> GIANT = registerSimpleBoolean("giant");
    public static final Supplier<AttachmentType<Boolean>> INVERTED = registerSimpleBoolean("inverted");
    
    // Amplifiers
    public static final Supplier<AttachmentType<Boolean>> CONTAINED = registerSimpleBoolean("contained");
    
    public static void register(IEventBus bus) {
        ATTACHMENT_TYPES.register(bus);
    }
    
    /**
     * Create and register a simple boolean attachment which is unsynchronized (to any client) and persists.
     *
     * @param name The unique id for the attachment.
     * @return Corresponding deferred holder.
     */
    public static Supplier<AttachmentType<Boolean>> registerSimpleBoolean(String name) {
        
        return ATTACHMENT_TYPES.register(
                name, () -> AttachmentType.builder(() -> false)
                        .serialize(Codec.BOOL)
                        // This is applied to mobs, and only needs to be checked on the server.
                        // Replace with "holder == to" for attachments that are on a player and only need to be sent to
                        // that player's client. Replace with "true" to synchronize to all clients. May be helpful for
                        // particles, especially if using particle utils.
                        .sync((holder, to) -> false, ByteBufCodecs.BOOL)
                        .build()
        );
    }
    
    @SuppressWarnings("unused") // Made for future use
    public static Supplier<AttachmentType<Boolean>> registerSynchronizedBoolean(String name) {
        
        return ATTACHMENT_TYPES.register(
                name, () -> AttachmentType.builder(() -> false)
                        .serialize(Codec.BOOL)
                        .sync((holder, to) -> true, ByteBufCodecs.BOOL)
                        .build()
        );
    }
    
    public static Supplier<AttachmentType<Byte>> registerSynchronizedByte(String name) {
        
        return ATTACHMENT_TYPES.register(
                name, () -> AttachmentType.builder(() -> (byte) 0)
                        .serialize(Codec.BYTE)
                        .sync((holder, to) -> true, ByteBufCodecs.BYTE)
                        .build()
        );
    }
    
    public static Supplier<AttachmentType<Byte>> registerSynchronizedTransientByte(String name) {
        
        return ATTACHMENT_TYPES.register(
                name, () -> AttachmentType.builder(() -> (byte) 0)
                        .sync((holder, to) -> true, ByteBufCodecs.BYTE)
                        .build()
        );
    }
    
    public static Supplier<AttachmentType<Float>> registerSynchronizedFloat(String name) {
        
        return ATTACHMENT_TYPES.register(
                name, () -> AttachmentType.builder(() -> 1f)
                        .serialize(Codec.FLOAT)
                        .sync((holder, to) -> holder == to, ByteBufCodecs.FLOAT)
                        .build()
        );
    }
    
    public static Supplier<AttachmentType<Long>> registerSynchronizedTransientLong(String name) {
        
        return ATTACHMENT_TYPES.register(
                name, () -> AttachmentType.builder(() -> 0L)
                        .sync((holder, to) -> holder == to, ByteBufCodecs.VAR_LONG)
                        .build()
        );
    }
}
