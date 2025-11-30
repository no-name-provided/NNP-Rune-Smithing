package com.github.no_name_provided.nnp_rune_smithing.common.data_components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record RuneData(Integer tier, Integer color) {
    public static final Codec<RuneData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("tier").forGetter(RuneData::tier),
                    Codec.INT.fieldOf("color").forGetter(RuneData::color)
            ).apply(instance, RuneData::new)
    );
    public static final StreamCodec<ByteBuf, RuneData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, RuneData::tier,
            ByteBufCodecs.INT, RuneData::color,
            RuneData::new
    );
    public static final RuneData DEFAULT = new RuneData(5, 0);
}
