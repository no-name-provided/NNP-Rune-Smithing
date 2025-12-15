package com.github.no_name_provided.nnp_rune_smithing.common.recipes.serializers;

import com.github.no_name_provided.nnp_rune_smithing.common.recipes.AlloyRecipe;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

public class AlloySerializer implements RecipeSerializer<AlloyRecipe> {
    
    public static final MapCodec<AlloyRecipe> CODEC = RecordCodecBuilder.mapCodec(inst ->
            inst.group(
                    SizedFluidIngredient.NESTED_CODEC.fieldOf("input1").forGetter(AlloyRecipe::getInput1),
                    SizedFluidIngredient.NESTED_CODEC.fieldOf("input2").forGetter(AlloyRecipe::getInput2),
                    FluidStack.CODEC.fieldOf("result").forGetter(AlloyRecipe::getResult)
            ).apply(inst, AlloyRecipe::new)
    );
    
    // Might not need the registry friendly buffer
    StreamCodec<RegistryFriendlyByteBuf, AlloyRecipe> STREAM_CODEC = StreamCodec.composite(
            SizedFluidIngredient.STREAM_CODEC, AlloyRecipe::getInput1,
            SizedFluidIngredient.STREAM_CODEC, AlloyRecipe::getInput2,
            FluidStack.STREAM_CODEC, AlloyRecipe::getResult,
            AlloyRecipe::new
    );
    
    @Override
    public MapCodec<AlloyRecipe> codec() {
        return CODEC;
    }
    
    @Override
    public StreamCodec<RegistryFriendlyByteBuf, AlloyRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
