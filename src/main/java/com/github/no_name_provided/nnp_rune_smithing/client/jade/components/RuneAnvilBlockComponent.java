package com.github.no_name_provided.nnp_rune_smithing.client.jade.components;

import com.github.no_name_provided.nnp_rune_smithing.common.data_components.RunesAdded;
import com.github.no_name_provided.nnp_rune_smithing.common.entities.RuneAnvilBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;
import static com.github.no_name_provided.nnp_rune_smithing.common.data_components.RSDataComponents.RUNES_ADDED;

public class RuneAnvilBlockComponent implements IBlockComponentProvider {
    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        BlockEntity be = blockAccessor.getBlockEntity();
        if (be instanceof RuneAnvilBlockEntity anvil) {
            if (!anvil.seeImmutableBase().isEmpty()) {
                iTooltip.add(MutableComponent.create(anvil.seeImmutableBase().getHoverName().getContents()).withStyle(ChatFormatting.GOLD));
                RunesAdded runes = anvil.seeImmutableBase().get(RUNES_ADDED);
                if (null != runes) {
                    iTooltip.addAll(runes.getLore());
                }
            }
            if (!anvil.seeImmutableAddition().isEmpty()) {
                iTooltip.add(MutableComponent.create(anvil.seeImmutableAddition().getHoverName().getContents()).withStyle(ChatFormatting.GOLD));
            }
        }
    }
    
    @Override
    public ResourceLocation getUid() {
        return ResourceLocation.fromNamespaceAndPath(MODID, "rune_anvil");
    }
}
