package com.github.no_name_provided.nnp_rune_smithing.common.items;

import com.github.no_name_provided.nnp_rune_smithing.client.ClientUtilWrapper;
import com.github.no_name_provided.nnp_rune_smithing.common.items.interfaces.CastingMold;
import com.github.no_name_provided.nnp_rune_smithing.common.items.runes.molds.AbstractRuneMold;
import com.mojang.logging.LogUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.util.thread.EffectiveSide;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.Supplier;

public class CastingTemplate extends Item {
    private final Supplier<? extends CastingMold> MOLD_CREATED;
    public CastingTemplate(Properties properties, DeferredHolder<Item, ? extends CastingMold> moldCreated) {
        super(properties);
        MOLD_CREATED = moldCreated;
    }
    
    public CastingMold getMold() {
        return MOLD_CREATED.get();
    }
    
    @Override
    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        // Should be the itemstack used for crafting, with a count of 1,
        // since the full stack is added to any extra ingredients that happen to be left over
        return itemStack.copyWithCount(1);
    }
    
    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }
    
    @Override
    public String getDescriptionId() {
        if (EffectiveSide.get().isClient() && MOLD_CREATED.get() instanceof AbstractRuneMold runeMold) {
            try {
                
                return ClientUtilWrapper.localPlayerKnowsRune(runeMold.RUNE.get()) ? this.getOrCreateDescriptionId() : "unknown.nnp_rune_smithing.template";
            } catch (Error e) {
                LogUtils.getLogger().error("Failed to hide the names of unknown runes. Please disable this feature in the config.");
            }
        }
        
        return this.getOrCreateDescriptionId();
    }
}
