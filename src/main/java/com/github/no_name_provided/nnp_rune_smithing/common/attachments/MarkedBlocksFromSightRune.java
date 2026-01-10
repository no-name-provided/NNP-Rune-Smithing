package com.github.no_name_provided.nnp_rune_smithing.common.attachments;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public record MarkedBlocksFromSightRune(HashMap<ChunkPos, HashSet<BlockPos>> posList) {
    public static Codec<MarkedBlocksFromSightRune> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.unboundedMap(
                                    Codec.STRING.xmap(string -> new ChunkPos(Long.parseLong(string)), pos -> Long.toString(pos.toLong())),
                                    BlockPos.CODEC.fieldOf("pos").codec().listOf().xmap(HashSet::new, ArrayList::new)
                                    // This exists because of compiler/codec type checking silliness
                            ).xmap(HashMap::new, HashMap::new)
                            .fieldOf("positions").forGetter(MarkedBlocksFromSightRune::posList)
            ).apply(instance, MarkedBlocksFromSightRune::new)
    );
    
//    public static StreamCodec<RegistryFriendlyByteBuf, MarkedBlocksFromSightRune> STREAM_CODEC = RecordCodecBuilder.create(
//            instance -> instance.group(
//                    ByteBufCodecs.map(
//                                    HashMap::new,
//                                    ByteBufCodecs.STRING_UTF8.map(string -> new ChunkPos(Long.parseLong(string)), pos -> Long.toString(pos.toLong())),
//                                    ByteBufCodecs.collection(
//                                            HashSet::new,
//                                            BlockPos.STREAM_CODEC
//                                    )
//                            )
//            ).apply(instance, MarkedBlocksFromSightRune::new)
//    );
    
    public static StreamCodec<ByteBuf, MarkedBlocksFromSightRune> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
}
