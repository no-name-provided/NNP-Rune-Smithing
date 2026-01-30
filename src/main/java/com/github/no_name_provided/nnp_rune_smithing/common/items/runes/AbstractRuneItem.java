package com.github.no_name_provided.nnp_rune_smithing.common.items.runes;

import com.github.no_name_provided.nnp_rune_smithing.client.ClientUtilWrapper;
import com.github.no_name_provided.nnp_rune_smithing.common.data_components.RSDataComponents;
import com.github.no_name_provided.nnp_rune_smithing.common.data_components.RuneData;
import com.github.no_name_provided.nnp_rune_smithing.common.datamaps.CastableFluidData;
import com.github.no_name_provided.nnp_rune_smithing.common.datamaps.RSDataMaps;
import com.github.no_name_provided.nnp_rune_smithing.common.entities.RuneBlockEntity;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.neoforged.fml.util.thread.EffectiveSide;
import net.neoforged.neoforge.fluids.FluidStack;

import static com.github.no_name_provided.nnp_rune_smithing.common.blocks.RSBlocks.RUNE_BLOCK;

public class AbstractRuneItem extends BlockItem {
    public enum Type {
        TARGET,
        EFFECT,
        MODIFIER,
        AMPLIFIER,
        PLACE_HOLDER
    }
    
    
    public AbstractRuneItem(Properties properties) {
        super(
                RUNE_BLOCK.get(),
                properties
                        .stacksTo(16)
        );
    }
    
    public static int getMaterialTier(ItemStack rune) {
        
        // Will need to adjust if the max tier ever exceeds 5, or properly filter out placeholders in calcs
        return rune.getOrDefault(RSDataComponents.RUNE_DATA, RuneData.DEFAULT).tier();
    }
    public static int getMaterialColor(ItemStack rune) {
        
        return rune.getOrDefault(RSDataComponents.RUNE_DATA, RuneData.DEFAULT).color();
    }
    public Type getType() {
        
        return Type.TARGET;
    }
    public boolean validateFluid(FluidStack fluid) {
        CastableFluidData data = fluid.getFluidHolder().getData(RSDataMaps.CASTABLE_FLUID_DATA);
        
        return null != data && data.tier() >= 1;
    }
    
    /**
     * Allows this item to use normal translation keys, rather than copying the one for its block.
     */
    @Override
    public String getDescriptionId() {
        if (EffectiveSide.get().isClient()) {
            try {
                
                return ClientUtilWrapper.localPlayerKnowsRune(this) ? this.getOrCreateDescriptionId() : "unknown_rune.nnp_rune_smithing.rune";
            } catch (Error e) {
                LogUtils.getLogger().error("Failed to hide the names of unknown runes. Please disable this feature in the config.");
            }
        }
        
        return this.getOrCreateDescriptionId();
    }
    
    /**
     * Called when this item is used when targeting a Block.
     * Used to insert a copy of our rune into the new BlockEntity.
     */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack item = context.getItemInHand().copyWithCount(1);
        BlockPlaceContext bContext = new BlockPlaceContext(context);
        bContext.getClickedPos();
        InteractionResult result = super.useOn(context);
        if (context.getLevel() instanceof ServerLevel && result.indicateItemUse() && context.getLevel().getBlockEntity(bContext.getClickedPos()) instanceof RuneBlockEntity runes && item.getItem() instanceof AbstractRuneItem rune) {
            runes.setItem(rune.getType().ordinal(), item);
        }
        
        return result;
    }
}
