package com.github.no_name_provided.nnp_rune_smithing.common.entities;

import com.github.no_name_provided.nnp_rune_smithing.common.gui.menus.WhittlingTableMenu;
import com.github.no_name_provided.nnp_rune_smithing.common.recipes.RSRecipes;
import com.github.no_name_provided.nnp_rune_smithing.common.recipes.WhittlingRecipe;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static com.github.no_name_provided.nnp_rune_smithing.common.entities.RSEntities.WHITTLING_TABLE_BLOCK_ENTITY;
import static com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems.WHITTLING_KNIFE;

public class WhittlingTableBlockEntity extends BlockEntity implements MenuProvider {
    public static final int INVENTORY_SIZE = 4;
    private final ItemStackHandler inventory = makeInventory(INVENTORY_SIZE);
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
    
    /**
     * Returns the actual inventory, not a copy.
     */
    public ItemStackHandler getInventory() {
        return inventory;
    }
    
    private ItemStackHandler makeInventory(int size) {
        return new ItemStackHandler(size) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
            
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return switch (slot) {
                    // Material
                    case 0 -> stack.is(Items.STRIPPED_OAK_LOG) || stack.is(Items.OAK_LOG);
                    // Knife
                    case 1 -> stack.is(WHITTLING_KNIFE);
                    // Pattern
                    case 2 -> true;
                    // Output
                    case 3 -> false;
                    default -> true;
                };
            }
            
            @Override
            public void setStackInSlot(int slot, ItemStack stack) {
                super.setStackInSlot(slot, stack);
                if (slot != 3) {
                    updateOutputSlot(false);
                    onContentsChanged(slot);
                } else {
                    // Use stacks.set here to avoid an update loop
                    stacks.getFirst().shrink(1);
                    stacks.set(1, stacks.get(1).getCraftingRemainingItem());
//                    stacks.get(1).setDamageValue(stacks.get(1).getDamageValue() + 1);
                    updateOutputSlot(false);
                    onContentsChanged(slot);
                }
            }
            
            private void updateOutputSlot(boolean simulate) {
                if (!simulate && level instanceof ServerLevel sLevel && !getStackInSlot(0).isEmpty() && getStackInSlot(1).is(WHITTLING_KNIFE)) {
                    RecipeManager manager = sLevel.getRecipeManager();
                    Optional<RecipeHolder<WhittlingRecipe>> resultHolder = manager.getRecipeFor(RSRecipes.WHITTLING.get(), new SingleRecipeInput(getStackInSlot(2)), sLevel);
                    if (resultHolder.isPresent()) {
                        RecipeHolder<WhittlingRecipe> result = resultHolder.get();
                        ItemStack resultStack = result.value().getResult();
                        // Should always be true (think it's guaranteed by the codec)
                        if (!resultStack.isEmpty()) {
                            // Use stacks.set here to avoid an update loop
                            stacks.set(3, resultStack.copy());
                            setChanged();
                        }
                    }
                } else {
                    // Use stacks.set here to avoid an update loop
                    stacks.set(3, ItemStack.EMPTY);
                    setChanged();
                }
            }
        };
    }
    
    @Override
    public Component getDisplayName() {
        
        return Component.literal("Whittling Table").withStyle(ChatFormatting.DARK_GRAY);
    }
    
    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        
        return new WhittlingTableMenu(containerId, playerInventory, getBlockPos(), getInventory());
    }
}
