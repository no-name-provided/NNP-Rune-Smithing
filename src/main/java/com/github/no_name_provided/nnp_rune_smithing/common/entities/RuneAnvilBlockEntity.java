package com.github.no_name_provided.nnp_rune_smithing.common.entities;

import com.github.no_name_provided.nnp_rune_smithing.common.data_components.RuneData;
import com.github.no_name_provided.nnp_rune_smithing.common.data_components.RunesAdded;
import com.github.no_name_provided.nnp_rune_smithing.common.items.runes.AbstractRuneItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nullable;

import static com.github.no_name_provided.nnp_rune_smithing.common.data_components.RSDataComponents.RUNES_ADDED;
import static com.github.no_name_provided.nnp_rune_smithing.common.data_components.RSDataComponents.RUNE_DATA;
import static com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems.RUNE_SMITH_HAMMER;
import static net.minecraft.SharedConstants.TICKS_PER_SECOND;

public class RuneAnvilBlockEntity extends BlockEntity {
    public static final int NUMBER_OF_HITS_PER_CRAFT = 3;
    ItemStackHandler inventory = makeInventoryHandler(3);
    public int craftingProgress = 0;
    
    public RuneAnvilBlockEntity(BlockPos pos, BlockState blockState) {
        super(RSEntities.RUNE_ANVIL.get(), pos, blockState);
    }
    
    public static void serverTick(Level lev, BlockPos pos, BlockState state, RuneAnvilBlockEntity table) {
        if (lev instanceof ServerLevel level) {
            return;
        }
    }
    
    /**
     * Added for symmetry with containers.
     * Makes a list of references to stored ItemStacks, but cannot guarantee they will remain valid.
     */
    public NonNullList<ItemStack> getItems() {
        NonNullList<ItemStack> inventory = NonNullList.createWithCapacity(this.inventory.getSlots());
        for (int i = 0; i < this.inventory.getSlots(); i++) {
            inventory.add(this.inventory.getStackInSlot(i));
        }
        
        return inventory;
    }
    
    public ItemStack seeImmutableBase() {
        
        return inventory.getStackInSlot(0);
    }
    
    /**
     * Extracts the base and (implicitly) calls saving/updating logic.
     *
     * @return The item stack extracted.
     */
    public ItemStack extractBase() {
        
        return inventory.extractItem(0, Item.DEFAULT_MAX_STACK_SIZE, false);
    }
    
    public ItemStack setBase(ItemStack toAdd) {
        // When (and only when) the stack has DataComponents, the reference is preserved and any changes to
        // the inserted stack are propagated to the version stored in the ItemStackHandler.
        // #BlameTheNeoForgeTeam
        return inventory.insertItem(0, toAdd.copy(), false);
    }
    
    public ItemStack seeImmutableAddition() {
        
        return inventory.getStackInSlot(1);
    }
    
    /**
     * Extracts the addition and (implicitly) calls saving/updating logic.
     *
     * @return The item stack extracted.
     */
    public ItemStack extractAddition() {
        
        return inventory.extractItem(1, Item.DEFAULT_MAX_STACK_SIZE, false);
    }
    
    public ItemStack setAddition(ItemStack toAdd) {
        // When (and only when) the stack has DataComponents, the reference is preserved and any changes to
        // the inserted stack are propagated to the version stored in the ItemStackHandler.
        // #BlameTheNeoForgeTeam
        return inventory.insertItem(1, toAdd.copy(), false);
    }
    
    public ItemStack seeImmutableResult() {
        
        return inventory.getStackInSlot(2);
    }
    
    public ItemStack extractResult() {
        
        return inventory.extractItem(2, Item.DEFAULT_MAX_STACK_SIZE, false);
    }
    
    public int getContainerSize() {
        return inventory.getSlots();
    }
    
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        // These checks probably aren't necessary, but they're here because this method handles tags from packets
        if (tag.contains("inventory")) {
            inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        }
        if (tag.contains("craftingProgress")) {
            craftingProgress = tag.getInt("craftingProgress");
        }
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.putInt("crafting_progress", craftingProgress);
    }
    
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        
        return tag;
    }
    
    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    
    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
        CompoundTag tag = pkt.getTag();
        if (!tag.isEmpty()) {
            handleUpdateTag(tag, registries);
        }
    }
    
    private ItemStackHandler makeInventoryHandler(int size) {
        return new ItemStackHandler(size) {
            final int RESULT_SLOT = 2;
            
            @Override
            protected void onContentsChanged(int slot) {
                // Call BlockEntity's method
                // Might cause update spam during reloading, as slots have to be set one at a time
                setChanged();
                if (null != level) {
                    level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
                }
            }
            
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return switch (slot) {
                    case 0 -> !(stack.getItem() instanceof BlockItem);
                    case 1 -> stack.getItem() instanceof AbstractRuneItem;
                    default -> true;
                };
            }
            
            @Override
            public int getSlotLimit(int slot) {
                return slot == RESULT_SLOT ? Item.ABSOLUTE_MAX_STACK_SIZE : 1;
            }
            
        };
    }
    
    /**
     * If I bump up the max stack size of inputs, this method will eat the whole stack.
     * Annoying idiosyncrasies of ItemStackHandlers make this a pain to work around. #BlameTheNeoForgeTeam
     */
    public void createResult() {
        ItemStack rune = extractAddition();
        RuneData runeData = rune.getOrDefault(RUNE_DATA, RuneData.DEFAULT);
        ItemStack toUpgrade = extractBase();
        RunesAdded oldData = toUpgrade.getOrDefault(RUNES_ADDED, RunesAdded.DEFAULT.get());
        
        if (rune.getItem() instanceof AbstractRuneItem actualRune) {
            RunesAdded newData = oldData.makeUpdated(runeData, actualRune);
            toUpgrade.set(RUNES_ADDED, newData);
            // When (and only when) the stack has DataComponents, the reference is preserved and any changes to
            // the inserted stack are propagated to the version stored in the ItemStackHandler. Resolved by manually
            // copying the stack.
            // #BlameTheNeoForgeTeam
//            if (newData.target().rune() != PLACE_HOLDER_RUNE.get() && newData.effect().rune() != PLACE_HOLDER_RUNE.get() && newData.modifier().rune() != PLACE_HOLDER_RUNE.get() && newData.amplifier().rune() != PLACE_HOLDER_RUNE.get()) {
                inventory.insertItem(0, toUpgrade.copy(), false);
//            } else {
//                ItemHandlerHelper.giveItemToPlayer();
//            }
        }
    }
    
    public boolean tryCreateResult(ItemStack stack, @Nullable Player player) {
        if (stack.is(RUNE_SMITH_HAMMER) && !seeImmutableBase().isEmpty() && !seeImmutableAddition().isEmpty() && seeImmutableResult().isEmpty()) {
            RunesAdded runes = seeImmutableBase().getOrDefault(RUNES_ADDED, RunesAdded.DEFAULT.get());
            if (seeImmutableAddition().getItem() instanceof AbstractRuneItem rune && runes.getByType(rune.getType()).rune().getType() == AbstractRuneItem.Type.PLACE_HOLDER) {
                if ((null != player && player.isCreative()) || incrementCraftingProgress()) {
                    createResult();
                }
                if (level instanceof ServerLevel sLevel) {
                    // Think there's a convenience method somewhere that does this
                    stack.hurtAndBreak(1, sLevel, null, item -> stack.shrink(1));
                }
                if (null != player) {
                    player.getCooldowns().addCooldown(stack.getItem(), TICKS_PER_SECOND * 2);
                }
                if (null != level) {
                    level.playLocalSound(getBlockPos(), SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.1F + 0.9F, false);
                }
                
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Increments crafting progress, or resets it when a craft is completed. Should be used after all other crafting
     * checks have succeeded.
     * <p>
     *     Also displays particle effect.
     * </p>
     *
     * @return True if the craft has completed, false otherwise.
     */
    private boolean incrementCraftingProgress() {
        if (null != level) {
            for (int i = level.getRandom().nextInt(5); i < 10; i++) {
                level.addParticle(
                        ParticleTypes.SMALL_FLAME,
                        getBlockPos().getX() + 0.5,
                        getBlockPos().getY() + 1,
                        getBlockPos().getZ() + 0.5,
                        level.random.triangle(0, 0.1),
                        level.random.triangle(0.1, 0.1),
                        level.random.triangle(0, 0.1)
                );
            }
        }
        if (craftingProgress < NUMBER_OF_HITS_PER_CRAFT - 1) {
            craftingProgress++;
            setChanged();
            
            return false;
        } else  {
            craftingProgress = 0;
            setChanged();
            
            return true;
        }
    }
}
