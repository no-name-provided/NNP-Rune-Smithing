package com.github.no_name_provided.nnp_rune_smithing.common.data_components;

import com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems;
import com.github.no_name_provided.nnp_rune_smithing.common.items.runes.AbstractRuneItem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.Lazy;

import java.util.ArrayList;
import java.util.List;

public record RunesAdded(RuneAddedData target, RuneAddedData effect, RuneAddedData modifier, RuneAddedData amplifier, Integer effectiveTier) {
    
    public RuneAddedData getByType(AbstractRuneItem.Type type) {
        return switch (type) {
            case TARGET, PLACE_HOLDER -> target();
            case EFFECT -> effect();
            case MODIFIER -> modifier();
            case AMPLIFIER -> amplifier();
        };
    }
    
    public static final Codec<RunesAdded> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    RuneAddedData.CODEC.fieldOf("target").forGetter(RunesAdded::target),
                    RuneAddedData.CODEC.fieldOf("effect").forGetter(RunesAdded::effect),
                    RuneAddedData.CODEC.fieldOf("modifier").forGetter(RunesAdded::modifier),
                    RuneAddedData.CODEC.fieldOf("amplifier").forGetter(RunesAdded::amplifier),
                    Codec.INT.fieldOf("tier").forGetter(RunesAdded::effectiveTier)
            ).apply(instance, RunesAdded::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, RunesAdded> STREAM_CODEC = StreamCodec.composite(
            RuneAddedData.STREAM_CODEC, RunesAdded::target,
            RuneAddedData.STREAM_CODEC, RunesAdded::effect,
            RuneAddedData.STREAM_CODEC, RunesAdded::modifier,
            RuneAddedData.STREAM_CODEC, RunesAdded::amplifier,
            ByteBufCodecs.INT, RunesAdded::effectiveTier,
            RunesAdded::new
    );
    public static final Lazy<RunesAdded> DEFAULT = Lazy.of(() -> new RunesAdded(RuneAddedData.DEFAULT.get(), RuneAddedData.DEFAULT.get(), RuneAddedData.DEFAULT.get(), RuneAddedData.DEFAULT.get(), AbstractRuneItem.getMaterialTier(RSItems.PLACE_HOLDER_RUNE.get().getDefaultInstance())));
    
    public RunesAdded makeUpdated(RuneData data, AbstractRuneItem rune) {
        final int newTier = Math.min(data.tier(), effectiveTier());
        
        return switch (rune.getType()) {
            case TARGET -> new RunesAdded(new RuneAddedData(rune, data.color()), effect(), modifier(), amplifier(), newTier);
            case EFFECT -> new RunesAdded(target(), new RuneAddedData(rune, data.color()), modifier(), amplifier(), newTier);
            case MODIFIER -> new RunesAdded(target(), effect(), new RuneAddedData(rune, data.color()), amplifier(), newTier);
            case AMPLIFIER -> new RunesAdded(target(), effect(), modifier(), new RuneAddedData(rune, data.color()), newTier);
            case PLACE_HOLDER -> throw new IllegalArgumentException("Can't use this method to remove rune data.");
        };
    }
    
    public ArrayList<Component> getLore() {
        ArrayList<Component> result = new ArrayList<>(4);
        result.add(Component.literal("Target: ").append(target().rune().getDescription()));
        result.add(Component.literal("Effect: ").append(effect().rune().getDescription()));
        result.add(Component.literal("Modifier: ").append(modifier().rune().getDescription()));
        result.add(Component.literal("Amplifier: ").append(amplifier().rune().getDescription()));
        result.add(Component.literal("Tier: " + effectiveTier));
        
        return result;
    }
}
