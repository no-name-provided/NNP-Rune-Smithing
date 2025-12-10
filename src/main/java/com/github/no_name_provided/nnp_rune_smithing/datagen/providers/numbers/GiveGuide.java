package com.github.no_name_provided.nnp_rune_smithing.datagen.providers.numbers;

import com.github.no_name_provided.nnp_rune_smithing.ServerConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import static com.github.no_name_provided.nnp_rune_smithing.datagen.providers.numbers.RSNumbers.GIVE_GUIDE;

public record GiveGuide(int PlaceHolder) implements NumberProvider {
    
    public static final MapCodec<GiveGuide> CODEC = RecordCodecBuilder.mapCodec(
            inst -> inst.group(
                    Codec.INT.fieldOf("placeHolder").forGetter((p) -> placeHolder())
            ).apply(inst, GiveGuide::new));
    
    private static int placeHolder() {
        return 0;
    }
    
    @Override @ParametersAreNonnullByDefault
    public float getFloat(LootContext lootContext) {
        // True is 1, false is 0. Conveniently, only one book needs to be given.
        return ServerConfig.giveGuideToNewPlayers ? 1 : 0;
    }
    
    @Override
    public @Nonnull LootNumberProviderType getType() {
        return GIVE_GUIDE.get();
    }
}