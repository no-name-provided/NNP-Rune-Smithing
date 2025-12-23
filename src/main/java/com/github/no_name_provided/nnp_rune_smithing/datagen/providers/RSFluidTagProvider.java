package com.github.no_name_provided.nnp_rune_smithing.datagen.providers;

import com.github.no_name_provided.nnp_rune_smithing.common.items.runes.ElementalRuneItem.Affinity;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.FluidTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;
import static com.github.no_name_provided.nnp_rune_smithing.common.fluids.FluidHelper.unsafeGetFluidFromRegistryPath;
import static com.github.no_name_provided.nnp_rune_smithing.common.fluids.RSFluidTags.getFluidTag;

public class RSFluidTagProvider extends FluidTagsProvider {
    
    public RSFluidTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, provider, MODID, existingFileHelper);
    }
    
    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(getFluidTag(Affinity.AIR.getName()))
                .add(unsafeGetFluidFromRegistryPath("molten_sodium"));
        tag(getFluidTag(Affinity.WATER.getName()))
                .add(unsafeGetFluidFromRegistryPath("molten_aluminum"));
        tag(getFluidTag(Affinity.FIRE.getName()))
                .add(unsafeGetFluidFromRegistryPath("molten_phosphorus"));
        tag(getFluidTag(Affinity.EARTH.getName()))
                .add(unsafeGetFluidFromRegistryPath("molten_lead"));
        tag(getFluidTag(Affinity.VOID.getName()))
                .add(unsafeGetFluidFromRegistryPath("molten_solder"));
        tag(getFluidTag(Affinity.LIGHT.getName()))
                .add(unsafeGetFluidFromRegistryPath("molten_silver"));
    }
}
