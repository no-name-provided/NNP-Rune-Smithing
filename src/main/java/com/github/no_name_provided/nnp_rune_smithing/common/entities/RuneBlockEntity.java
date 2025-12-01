package com.github.no_name_provided.nnp_rune_smithing.common.entities;

import com.github.no_name_provided.nnp_rune_smithing.common.items.runes.AbstractRuneItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

import static com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems.*;
import static com.github.no_name_provided.nnp_rune_smithing.common.items.runes.AbstractRuneItem.Type;

public class RuneBlockEntity extends BaseContainerBlockEntity {
    public NonNullList<ItemStack> inventory = NonNullList.withSize(4, ItemStack.EMPTY);
    int TIER = 1;
    public static int TARGET = Type.TARGET.ordinal();
    public static int EFFECT = Type.EFFECT.ordinal();
    public static int MODIFIER = Type.MODIFIER.ordinal();
    public static int AMPLIFIER = Type.AMPLIFIER.ordinal();
    private final ItemStack fauxBonemeal = new ItemStack(Items.BONE_MEAL);
    private Boolean didSomethingRecently = true;
    
    public RuneBlockEntity(BlockPos pos, BlockState blockState) {
        super(RSEntities.RUNE_BLOCK_ENTITY.get(), pos, blockState);
    }
    
    @Override
    protected Component getDefaultName() {
        return Component.literal("Rune(s)");
    }
    
    @Override
    public NonNullList<ItemStack> getItems() {
        return inventory;
    }
    
    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        inventory = items;
        setChanged();
    }
    
    @SuppressWarnings("DataFlowIssue") @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return null;
    }
    
    @Override
    public int getContainerSize() {
        return 4;
    }
    
    /**
     * Called when this is first added to the world (by {@link LevelChunk#addAndRegisterBlockEntity(BlockEntity)})
     * or right before the first tick when the chunk is generated or loaded from disk.
     * Override instead of adding {@code if (firstTick)} stuff in update.
     */
    @Override
    public void onLoad() {
        super.onLoad();
        cacheEffectiveRuneTier();
    }
    
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, inventory, registries);
        // Quick and inefficient hack to fix bug in ContainerHelper#saveAllItems
        for (int i = 0; i < getContainerSize(); i++) {
            if (tag.getBoolean("clearSlot" + i)) {
                inventory.set(i, ItemStack.EMPTY);
                setChanged();
            }
        }
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, inventory, registries);
        // Quick and inefficient hack to fix bug in ContainerHelper#saveAllItems
        for (int i = 0; i < getContainerSize(); i++) {
            tag.putBoolean("clearSlot" + i, getItem(i).isEmpty());
        }
    }
    
    @Override
    public void setChanged() {
        cacheEffectiveRuneTier();
        super.setChanged();
        if (level != null) {
            // Force a block update
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL_IMMEDIATE);
        }
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
    
    public void dropInventory() {
        if (level != null) {
            for (ItemStack stack : inventory) {
                level.addFreshEntity(new ItemEntity(level, getBlockPos().getX() + 0.5, getBlockPos().getY() + 1, getBlockPos().getZ() + 0.5, stack.copy()));
            }
            clearContent();
            setChanged();
        }
    }
    
    public static void serverTick(Level levelIn, BlockPos pos, @SuppressWarnings("unused") BlockState state, RuneBlockEntity runes) {
        if (levelIn instanceof ServerLevel level) {
            if (runes.isEmpty()) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
            // Attempt at an "eco" mode. Each path should set this flag to true if it does something
            int extraDelay = runes.didSomethingRecently ? 1 : 20;
            if (runes.didSomethingRecently) {
                runes.didSomethingRecently = false;
            }
            if (runes.getItem(TARGET).is(SELF_RUNE)) {
                if (runes.getItem(EFFECT).is(EARTH_RUNE) && level.getGameTime() % (200 + extraDelay) == 1) {
                    int radius = runes.getItem(MODIFIER).is(WIDEN_RUNE) ? 5 : 2;
                    BlockPos.betweenClosed(
                            pos.east(radius).north(radius),
                            pos.west(radius).south(radius)
                    ).forEach(position -> {
                        if (level.getBlockState(position).getBlock() instanceof CropBlock && Mth.randomBetweenInclusive(level.random, 1, 10) % 10 == 0) {
                            BoneMealItem.applyBonemeal(runes.fauxBonemeal.copy(), level, position, null);
                            // This regular code (adapted from vanilla) does nothing server-side
//                            BoneMealItem.addGrowthParticles(level, position, 15);
                            // and I assume direct bone mealing is more efficient than scheduling like 10 times as many random ticks
//                            level.scheduleTick(position, crop, Mth.randomBetweenInclusive(level.random, 1, 20));
                            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, position.getX() + 0.5, position.getY() + 0.3, position.getZ() + 0.5, 5, 0.5, 0.3, 0.5, 0.05);
                            runes.didSomethingRecently = true;
                        }
                    });
                } else if (runes.getItem(EFFECT).is(AIR_RUNE) && level.getGameTime() % (200 + extraDelay) == 1) {
                    @Nullable IItemHandler cap = level.getCapability(Capabilities.ItemHandler.BLOCK, pos.below(1), Direction.UP);
                    if (null != cap) {
                        int radius = runes.getItem(MODIFIER).is(WIDEN_RUNE) ? 8 : 3;
                        AABB boundingBox = new AABB(pos).inflate(radius);
                        level.getEntitiesOfClass(ItemEntity.class, boundingBox, EntitySelector.ENTITY_STILL_ALIVE).forEach(item -> {
                            ItemStack remainder = ItemHandlerHelper.insertItem(cap, item.getItem(), false);
                            if (remainder.isEmpty()) {
                                item.setItem(ItemStack.EMPTY);
                                item.discard();
                            } else {
                                item.setItem(remainder);
                            }
                            runes.didSomethingRecently = true;
                        });
                    }
                } else if (runes.getItem(EFFECT).is(FIRE_RUNE) && level.getGameTime() % (20 + 10 * extraDelay) == 1) {
                    int radius = runes.getItem(MODIFIER).is(WIDEN_RUNE) ? 5 : 1;
                    BlockPos.betweenClosed(
                            pos.east(radius).north(radius),
                            pos.west(radius).south(radius)
                    ).forEach(position -> {
                        if (level.getBlockState(position).isAir() && Mth.randomBetweenInclusive(level.random, 1, 10) % 10 == 0) {
                            level.setBlock(position, Blocks.FIRE.defaultBlockState(), Block.UPDATE_ALL);
                            runes.didSomethingRecently = true;
                        }
                    });
                } else if (runes.getItem(EFFECT).is(WATER_RUNE) && level.getGameTime() % (20 + 10 * extraDelay) == 2) {
                    int radius = runes.getItem(MODIFIER).is(WIDEN_RUNE) ? 3 : 0;
                    if (radius != 0) {
                        BlockPos.betweenClosed(
                                pos.east(radius).north(radius).below(),
                                pos.west(radius).south(radius).below()
                        ).forEach(position -> {
                            if ((level.getBlockState(position).isAir() || (level.getBlockState(position).is(Blocks.WATER) && !level.getFluidState(position).isSource()))) {
                                level.setBlock(position, Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
                                runes.didSomethingRecently = true;
                            }
                        });
                        // We need this, because, although #betweenClosed seems to be inclusive in general, it doesn't work for ranges of 1
                    } else {
                        if (level.getBlockState(pos.below()).isAir()) {
                            level.setBlock(pos.below(), Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
                            runes.didSomethingRecently = true;
                        }
                    }
                }
            }
        }
    }
    
    void cacheEffectiveRuneTier() {
        // Make sure I don't introduce a number smaller than any existing tier
        // by defaulting to the tier of the first rune
        for (ItemStack rune : inventory) {
            if (!rune.isEmpty()) {
                TIER = AbstractRuneItem.getMaterialTier(rune);
                break;
            }
        }
        // Find the minimum of all the rune tiers
        for (ItemStack rune : inventory) {
            if (!rune.isEmpty()) {
                TIER = Math.min(AbstractRuneItem.getMaterialTier(rune), TIER);
            }
        }
        
        if (inventory.get(AMPLIFIER).is(AMPLIFY_RUNE)) {
            TIER++;
        }
    }
    public int getTier() {
        return TIER;
    }
}
