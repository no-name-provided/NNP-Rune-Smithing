package com.github.no_name_provided.nnp_rune_smithing.common.recipes;

import com.github.no_name_provided.nnp_rune_smithing.common.recipes.serializers.MeltSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;
import static net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion.MOD_ID;

public class RSRecipes {
    public static DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, MODID);
    
    public static final Supplier<RecipeType<MeltRecipe>> MELT =
            RECIPE_TYPES.register(
                    "melt",
                    // We need the qualifying generic here due to generics being generics.
                    () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(MOD_ID, "melt"))
            );
    
    public static DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, MODID);
    
    public static final Supplier<RecipeSerializer<MeltRecipe>> MELT_SERIALIZER = RECIPE_SERIALIZERS.register(
            "melt",
            MeltSerializer::new
    );
    
    public static void register(IEventBus bus) {
        RECIPE_TYPES.register(bus);
        RECIPE_SERIALIZERS.register(bus);
    }
}
