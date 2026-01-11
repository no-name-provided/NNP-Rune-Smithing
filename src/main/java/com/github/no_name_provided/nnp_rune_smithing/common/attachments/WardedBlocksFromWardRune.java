package com.github.no_name_provided.nnp_rune_smithing.common.attachments;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.HashSet;

public record WardedBlocksFromWardRune(HashSet<BlockPos> wardedBlocks) {
    
    public static Codec<WardedBlocksFromWardRune> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.list(BlockPos.CODEC).fieldOf("positions")
                            .xmap(HashSet::new, ArrayList::new)
                            .forGetter(WardedBlocksFromWardRune::wardedBlocks)
            ).apply(instance, WardedBlocksFromWardRune::new)
    );
    
    public static StreamCodec<ByteBuf, WardedBlocksFromWardRune> STREAM_CODEC = BlockPos.STREAM_CODEC
            .apply(ByteBufCodecs.list())
            .map(HashSet::new, ArrayList::new)
            .map(WardedBlocksFromWardRune::new, WardedBlocksFromWardRune::wardedBlocks
            );
}
