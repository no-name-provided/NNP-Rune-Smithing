package com.github.no_name_provided.nnp_rune_smithing.common.items;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import static com.github.no_name_provided.nnp_rune_smithing.common.attachments.RSAttachments.SHOW_RUNE_BLOCK_BOUNDING_BOXES;

public class RuneSmithHammer extends Item {
    public RuneSmithHammer(Properties properties) {
        super(properties);
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (player.isShiftKeyDown()) {
            // Toggle bounding box visibility
            player.setData(SHOW_RUNE_BLOCK_BOUNDING_BOXES, !player.getData(SHOW_RUNE_BLOCK_BOUNDING_BOXES));
            
            return InteractionResultHolder.success(player.getItemInHand(usedHand));
        }
        
        return InteractionResultHolder.pass(player.getItemInHand(usedHand));
    }
}
