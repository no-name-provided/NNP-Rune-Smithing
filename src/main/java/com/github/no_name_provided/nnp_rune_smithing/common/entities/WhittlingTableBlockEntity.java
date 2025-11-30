package com.github.no_name_provided.nnp_rune_smithing.common.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

import static com.github.no_name_provided.nnp_rune_smithing.common.entities.RSEntities.WHITTLING_TABLE_BLOCK_ENTITY;

public class WhittlingTableBlockEntity extends BlockEntity {
    private final ItemStackHandler inventory = makeInventory(30);
    private final String inventoryTag = "inventory";
    
    public WhittlingTableBlockEntity(BlockPos pos, BlockState blockState) {
        super(WHITTLING_TABLE_BLOCK_ENTITY.get(), pos, blockState);
    }
    
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound(inventoryTag));
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put(inventoryTag, inventory.serializeNBT(registries));
    }
    
    public ItemStackHandler getInventory() {
        return inventory;
    }
    
    private ItemStackHandler makeInventory(int size) {
        return new ItemStackHandler(size) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        };
    }
    
}
