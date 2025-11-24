package com.github.no_name_provided.nnp_rune_smithing.common.recipes.serializers;

import com.github.no_name_provided.nnp_rune_smithing.common.recipes.MeltRecipe;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;

public class MeltSerializer implements RecipeSerializer<MeltRecipe> {
    
    public static final MapCodec<MeltRecipe> CODEC = RecordCodecBuilder.mapCodec(inst ->
            inst.group(
                    Ingredient.CODEC_NONEMPTY.fieldOf("meltable").forGetter(MeltRecipe::getTO_MELT),
                    ExtraCodecs.intRange(1, Integer.MAX_VALUE).fieldOf("meltingTemp").forGetter(MeltRecipe::getMELTING_TEMP),
                    FluidStack.CODEC.fieldOf("result").forGetter(MeltRecipe::getRESULT)
            ).apply(inst, MeltRecipe::new)
    );
    
    // Might not need the registry friendly buffer
    StreamCodec<RegistryFriendlyByteBuf, MeltRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, MeltRecipe::getTO_MELT,
            ByteBufCodecs.INT, MeltRecipe::getMELTING_TEMP,
            FluidStack.STREAM_CODEC, MeltRecipe::getRESULT,
            MeltRecipe::new
    );
    
    @Override
    public MapCodec<MeltRecipe> codec() {
        return CODEC;
    }
    
    @Override
    public StreamCodec<RegistryFriendlyByteBuf, MeltRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
