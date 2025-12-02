package com.github.no_name_provided.nnp_rune_smithing.common.items.runes;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.fml.ModList;

import java.util.List;

public class WoodenCharm extends Item {
    public WoodenCharm(Properties properties) {
        super(properties);
    }
    
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (!ModList.get().isLoaded("curios")) {
            tooltipComponents.add(Component.literal("Does nothing unless Curios is installed.").withStyle(ChatFormatting.DARK_RED));
            tooltipComponents.add(Component.literal("(Charms are the only part of this mod that relies on Curios.)").withStyle(ChatFormatting.GRAY));
        }
    }
}
