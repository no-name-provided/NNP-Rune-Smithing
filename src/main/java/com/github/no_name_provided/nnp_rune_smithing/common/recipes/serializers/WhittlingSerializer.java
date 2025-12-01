package com.github.no_name_provided.nnp_rune_smithing.common.recipes.serializers;

import com.github.no_name_provided.nnp_rune_smithing.common.recipes.MeltRecipe;
import com.github.no_name_provided.nnp_rune_smithing.common.recipes.WhittlingRecipe;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;

public class WhittlingSerializer implements RecipeSerializer<WhittlingRecipe> {
    
    public static final MapCodec<WhittlingRecipe> CODEC = RecordCodecBuilder.mapCodec(inst ->
            inst.group(
                    Codec.STRING.fieldOf("group").forGetter(WhittlingRecipe::getGroup),
                    Ingredient.CODEC_NONEMPTY.fieldOf("input").forGetter(WhittlingRecipe::getIngredient),
                    ItemStack.CODEC.fieldOf("result").forGetter(WhittlingRecipe::getResult)
            ).apply(inst, WhittlingRecipe::new)
    );
    
    // Might not need the registry friendly buffer
    StreamCodec<RegistryFriendlyByteBuf, WhittlingRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, WhittlingRecipe::getGroup,
            Ingredient.CONTENTS_STREAM_CODEC, WhittlingRecipe::getIngredient,
            ItemStack.STREAM_CODEC, WhittlingRecipe::getResult,
            WhittlingRecipe::new
    );
    
    
    @Override
    public MapCodec<WhittlingRecipe> codec() {
        return CODEC;
    }
    
    @Override
    public StreamCodec<RegistryFriendlyByteBuf, WhittlingRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
