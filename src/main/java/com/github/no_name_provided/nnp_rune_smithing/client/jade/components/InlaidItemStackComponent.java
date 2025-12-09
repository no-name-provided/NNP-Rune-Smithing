package com.github.no_name_provided.nnp_rune_smithing.client.jade.components;

import com.github.no_name_provided.nnp_rune_smithing.common.data_components.RunesAdded;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.loading.FMLEnvironment;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;
import static com.github.no_name_provided.nnp_rune_smithing.common.data_components.RSDataComponents.RUNES_ADDED;

public class InlaidItemStackComponent implements IComponentProvider<EntityAccessor> {
    @Override
    public void appendTooltip(ITooltip iTooltip, EntityAccessor entityAccessor, IPluginConfig iPluginConfig) {
        if (entityAccessor.getEntity() instanceof ItemEntity itemEntity && !itemEntity.getItem().isEmpty()) {
            if (!FMLEnvironment.dist.isDedicatedServer()) {
                Player player = Minecraft.getInstance().player;
                if (null != player && player.isShiftKeyDown()) {
                    RunesAdded runes = itemEntity.getItem().get(RUNES_ADDED);
                    if (null != runes) {
                        iTooltip.addAll(runes.getLore());
                    }
                }
            }
        }
    }
    
    @Override
    public ResourceLocation getUid() {
        return ResourceLocation.fromNamespaceAndPath(MODID, "inlaid_itemstack");
    }
    

}
