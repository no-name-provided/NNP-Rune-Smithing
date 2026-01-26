package com.github.no_name_provided.nnp_rune_smithing.common.entities;

import com.github.no_name_provided.nnp_rune_smithing.common.data_components.RSDataComponents;
import com.github.no_name_provided.nnp_rune_smithing.common.gui.menus.WhittlingTableMenu;
import com.github.no_name_provided.nnp_rune_smithing.common.recipes.RSRecipes;
import com.github.no_name_provided.nnp_rune_smithing.common.recipes.WhittlingRecipe;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.github.no_name_provided.nnp_rune_smithing.common.entities.RSEntities.WHITTLING_TABLE_BLOCK_ENTITY;
import static com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems.WHITTLING_KNIFE;
import static com.github.no_name_provided.nnp_rune_smithing.datagen.providers.RSItemTagProvider.*;

public class WhittlingTableBlockEntity extends BlockEntity implements MenuProvider {
    public static final int MATERIAL_SLOT = 0;
    public static final int TOOL_SLOT = 1;
    public static final int TEMPLATE_SLOT = 2;
    public static final int OUTPUT_SLOT = 3;
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
                // Tag driven, to support datapacks
                return switch (slot) {
                    case MATERIAL_SLOT -> stack.is(WHITTLING_MATERIALS);
                    case TOOL_SLOT -> stack.is(WHITTLING_TOOLS);
                    case TEMPLATE_SLOT -> stack.is(WHITTLING_TEMPLATES);
                    case OUTPUT_SLOT -> false;
                    default -> true;
                };
            }
            
            @Override
            public void setStackInSlot(int slot, ItemStack stack) {
                super.setStackInSlot(slot, stack);
                switch (slot) {
                    // This is only called when a stack is removed from the output slot (a craft has occurred)
                    case OUTPUT_SLOT -> {
                        // Use stacks.set here to avoid an update loop
                        stacks.getFirst().shrink(1);
                        // Knife reduces its durability by one when used in a craft
                        stacks.set(TOOL_SLOT, stacks.get(TOOL_SLOT).getCraftingRemainingItem());
                    }
                }
                updateOutputSlot(false);
                onContentsChanged(slot);
            }
            
            private void updateOutputSlot(boolean simulate) {
                if (!simulate &&
                        level instanceof ServerLevel sLevel &&
                        !getStackInSlot(MATERIAL_SLOT).isEmpty() &&
                        getStackInSlot(TOOL_SLOT).is(WHITTLING_KNIFE)
                ) {
                    RecipeManager manager = sLevel.getRecipeManager();
                    Optional<RecipeHolder<WhittlingRecipe>> resultHolder = manager.getRecipeFor(
                            RSRecipes.WHITTLING.get(),
                            new SingleRecipeInput(getStackInSlot(TEMPLATE_SLOT)), sLevel
                    );
                    if (resultHolder.isPresent()) {
                        RecipeHolder<WhittlingRecipe> result = resultHolder.get();
                        ItemStack resultStack = result.value().getResult();
                        // Should always be true (think it's guaranteed by the codec)
                        if (!resultStack.isEmpty()) {
                            // Use stacks.set here to avoid an update loop
                            stacks.set(OUTPUT_SLOT, resultStack.copy());
                            setChanged();
                        }
                        // No valid recipe
                    } else {
                        // Use stacks#set here to avoid an update loop
                        stacks.set(OUTPUT_SLOT, ItemStack.EMPTY);
                        setChanged();
                    }
                } else {
                    // Use stacks#set here to avoid an update loop
                    stacks.set(OUTPUT_SLOT, ItemStack.EMPTY);
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
    
    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);
        List<ItemStack> inventory = componentInput.get(RSDataComponents.ITEMSTACK_HANDLER_INVENTORY);
        if (null != inventory) {
            for (int i = 0; i < inventory.size() && i < this.inventory.getSlots(); i++) {
                this.inventory.setStackInSlot(i, inventory.get(i));
            }
        }
    }
    
    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        ArrayList<ItemStack> itemStacks = new ArrayList<>();
        // Even though we aren't extracting anything, just getting the stack in the last position (the result item)
        // seems to trigger a "craft" and apply costs. The simple fix is to not save that stack, and allow it to be
        // recalculated normally when the new BlockEntity's inventory is populated with our other saved stacks
        for (int i = 0; i < this.inventory.getSlots() - 1; i++) {
            itemStacks.add(this.inventory.getStackInSlot(i));
        }
        components.set(RSDataComponents.ITEMSTACK_HANDLER_INVENTORY, itemStacks);
    }
    
    @Override
    @SuppressWarnings("deprecation")
    // No alternative is apparent, and the variant used by BaseContainerBlockEntity isn't deprecated
    public void removeComponentsFromTag(CompoundTag tag) {
        // For some weird reason, we need to provide the (registry path) "name" here.
        tag.remove(RSDataComponents.ITEMSTACK_HANDLER_INVENTORY.getId().getPath());
    }
}
