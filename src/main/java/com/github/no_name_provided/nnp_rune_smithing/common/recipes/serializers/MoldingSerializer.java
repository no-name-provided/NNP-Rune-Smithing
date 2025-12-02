package com.github.no_name_provided.nnp_rune_smithing.common.recipes.serializers;

import com.github.no_name_provided.nnp_rune_smithing.common.recipes.MoldingRecipe;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class MoldingSerializer implements RecipeSerializer<MoldingRecipe> {
    public static final MapCodec<MoldingRecipe> CODEC = RecordCodecBuilder.mapCodec(inst ->
            inst.group(
                    Ingredient.CODEC_NONEMPTY.fieldOf("template").forGetter(MoldingRecipe::template),
                    Ingredient.CODEC_NONEMPTY.fieldOf("material").forGetter(MoldingRecipe::material),
                    ItemStack.CODEC.fieldOf("result").forGetter(MoldingRecipe::result)
            ).apply(inst, MoldingRecipe::new)
    );
    
    // Might not need the registry friendly buffer
    StreamCodec<RegistryFriendlyByteBuf, MoldingRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, MoldingRecipe::template,
            Ingredient.CONTENTS_STREAM_CODEC, MoldingRecipe::material,
            ItemStack.STREAM_CODEC, MoldingRecipe::result,
            MoldingRecipe::new
    );
    
    @Override
    public MapCodec<MoldingRecipe> codec() {
        return CODEC;
    }
    
    @Override
    public StreamCodec<RegistryFriendlyByteBuf, MoldingRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
