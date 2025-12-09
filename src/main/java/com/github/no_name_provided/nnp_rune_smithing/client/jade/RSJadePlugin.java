package com.github.no_name_provided.nnp_rune_smithing.client.jade;

import com.github.no_name_provided.nnp_rune_smithing.client.jade.components.InlaidItemStackComponent;
import com.github.no_name_provided.nnp_rune_smithing.client.jade.components.RuneAnvilBlockComponent;
import com.github.no_name_provided.nnp_rune_smithing.common.blocks.RuneAnvilBlock;
import net.minecraft.world.entity.item.ItemEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Can't verify this is actually client side. Documentation unclear. May be misfiled.
 */
@WailaPlugin
public class RSJadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        //TODO register data providers
    }
    
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        //TODO register component providers, icon providers, callbacks, and config options here
//        registration.usePickedResult(RSBlocks.RUNE_BLOCK.get());
        
        registration.registerEntityComponent(
                new InlaidItemStackComponent(),
                ItemEntity.class
        );
        registration.registerBlockComponent(
                new RuneAnvilBlockComponent(),
                RuneAnvilBlock.class
        );
    }
}
