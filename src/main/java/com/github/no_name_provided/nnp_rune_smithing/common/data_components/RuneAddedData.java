package com.github.no_name_provided.nnp_rune_smithing.common.data_components;

import com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems;
import com.github.no_name_provided.nnp_rune_smithing.common.items.runes.AbstractRuneItem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.util.Lazy;

import java.util.function.Function;

/**
 * Record representing the data of runes added to a tool or armor.
 */
public record RuneAddedData(AbstractRuneItem rune, int color) {
    
    public static Lazy<RuneAddedData> DEFAULT = Lazy.of(() -> new RuneAddedData(RSItems.PLACE_HOLDER_RUNE.get(), 0));
    
    public static final Codec<RuneAddedData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BuiltInRegistries.ITEM.byNameCodec().comapFlatMap(item -> {
                            try {
                                 return DataResult.success((AbstractRuneItem) item);
                        } catch (Error ignoredError) {
                                return DataResult.error(() -> "Tried to save/read a non-child of AbstractRuneItem from RuneAddedData.");
                        }},
                            rItem -> rItem
                    ).fieldOf("rune").forGetter(RuneAddedData::rune),
                    Codec.INT.fieldOf("color").forGetter(RuneAddedData::color)
            ).apply(instance, RuneAddedData::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, RuneAddedData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodecWithRegistries(BuiltInRegistries.ITEM.byNameCodec()).map(item -> {
//                        try {
//                            return DataResult.success((AbstractRuneItem) item);
//                        } catch (Error ignoredError) {
//                            return DataResult.error(() -> "Tried to save/read a non-child of AbstractRuneItem from RuneAddedData.");
//                        }},
                return (AbstractRuneItem) item;
            },
                    (Function<AbstractRuneItem, Item>) item -> item
            ), RuneAddedData::rune,
            ByteBufCodecs.INT, RuneAddedData::color,
            RuneAddedData::new
    );
}
