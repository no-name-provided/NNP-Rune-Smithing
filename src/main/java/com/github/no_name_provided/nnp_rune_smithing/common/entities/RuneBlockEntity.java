package com.github.no_name_provided.nnp_rune_smithing.common.entities;

import com.github.no_name_provided.nnp_rune_smithing.common.blocks.RuneBlock;
import com.github.no_name_provided.nnp_rune_smithing.common.items.runes.AbstractRuneItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.world.AuxiliaryLightManager;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

import static com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems.*;
import static com.github.no_name_provided.nnp_rune_smithing.common.items.runes.AbstractRuneItem.Type;

public class RuneBlockEntity extends BaseContainerBlockEntity {
    private int radius = 0;
    private int height = 0;
    private BlockPos offset = BlockPos.ZERO;
    public NonNullList<ItemStack> inventory = NonNullList.withSize(4, ItemStack.EMPTY);
    private AuxiliaryLightManager lightManager;
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
     * Called when this is first added to the world (by {@link LevelChunk#addAndRegisterBlockEntity(BlockEntity)}) or
     * right before the first tick when the chunk is generated or loaded from disk. Override instead of adding
     * {@code if (firstTick)} stuff in update.
     */
    @Override
    public void onLoad() {
        super.onLoad();
        cacheEffectiveRuneTier();
        if (null != level) {
            lightManager = level.getAuxLightManager(getBlockPos());
        }
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
        setRadius(tag.getInt("radius"));
        setHeight(tag.getInt("height"));
        setOffset(NbtUtils.readBlockPos(tag, "offset").orElse(BlockPos.ZERO));
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, inventory, registries);
        // Quick and inefficient (?) hack to fix bug in ContainerHelper#saveAllItems
        for (int i = 0; i < getContainerSize(); i++) {
            tag.putBoolean("clearSlot" + i, getItem(i).isEmpty());
        }
        tag.putInt("radius", getRadius());
        tag.putInt("height", getHeight());
        tag.put("offset", NbtUtils.writeBlockPos(getOffset()));
        // There's a codec for the above, but I'm not sure how to convert a byte buffer (#encode) to a tag
        // and don't feel like screwing around until it works. Inspired by Fabric wiki, but untested, and left for
        // reference (since Nbt is being phased out in favor of codecs, and I may want to port at some point):
//        BlockPos.CODEC.encodeStart(NbtOps.INSTANCE, getBlockPos()).getOrThrow();
    
    }
    
    @Override
    public void setChanged() {
        cacheEffectiveRuneTier();
        super.setChanged();
        if (null != level) {
            // Force a block update
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL_IMMEDIATE);
            
            // This could theoretically be in #setItem, but it seems to get immediately overwritten somehow.
            if (null == lightManager) {
                lightManager = level.getAuxLightManager(getBlockPos());
            }
            if (null != lightManager) {
                if (!getItem(0).isEmpty() && !getItem(1).isEmpty()) {
                    lightManager.setLightAt(getBlockPos(), Mth.clamp(getTier() * 3, 3, 15));
                } else {
                    lightManager.removeLightAt(getBlockPos());
                }
            }
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
            float tickRate = level.tickRateManager().tickrate();
            if (runes.isEmpty()) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
            // Attempt at an "eco" mode. Each path should set this flag to true if it does something
            int extraDelay = runes.didSomethingRecently ? 1 : 20;
            boolean tunneling;
            if (runes.didSomethingRecently) {
                runes.didSomethingRecently = false;
            }
            boolean isInverted = runes.getItem(MODIFIER).is(INVERT_RUNE);
            if (runes.getItem(MODIFIER).is(TUNNEL_RUNE)) {
                tunneling = true;
                runes.setRadius(0);
                runes.setHeight(1);
                runes.setOffset(getTunnelingOffset(runes.getBlockState()));
            } else {
                // Default put here, so the compiler will realize that tunneling is "effectively final"
                tunneling = false;
            }
            if (runes.getItem(TARGET).is(SELF_RUNE)) {
                if (runes.getItem(EFFECT).is(SIGHT_RUNE) && level.getGameTime() % (20 + extraDelay) == 1) {
                    if (!tunneling) {
                        runes.setHeight(2);
                        int baseRadius = 5;
                        int radiusModifier = 0;
                        if (runes.getItem(3).is(WIDEN_RUNE)) {
                            radiusModifier = 3;
                        } else if (runes.getItem(3).is(NARROW_RUNE)) {
                            radiusModifier = -3;
                        }
                        runes.setRadius(baseRadius + radiusModifier);
                        runes.setOffset(BlockPos.ZERO);
                    }
                    AABB boundingBox = new AABB(pos.offset(runes.getOffset())).inflate(Math.max(runes.getRadius() - 1, 0), Math.max(runes.getHeight() - 1, 0), Math.max(runes.getRadius() - 1, 0));
                    level.getEntitiesOfClass(LivingEntity.class, boundingBox, EntitySelector.ENTITY_STILL_ALIVE).forEach(lifeform -> {
                        if (lifeform.isAffectedByPotions()) {
                            lifeform.addEffect(
                                    new MobEffectInstance(
                                            !isInverted ? MobEffects.INVISIBILITY : MobEffects.GLOWING,
                                            (int) (tickRate * 10 * runes.getTier()),
                                            runes.getTier()
                                    )
                            );
                            runes.didSomethingRecently = true;
                        }
                        
                    });
                }
                if (runes.getItem(EFFECT).is(EARTH_RUNE) && level.getGameTime() % (200 + extraDelay) == 1) {
                    if (!tunneling) {
                        runes.setRadius(runes.getItem(MODIFIER).is(WIDEN_RUNE) ? 5 : (runes.getItem(MODIFIER).is(NARROW_RUNE) ? 1 : 3));
                        runes.setHeight(1);
                        runes.setOffset(BlockPos.ZERO);
                    }
                    BlockPos.betweenClosed(
                            pos.offset(runes.getOffset()).east(runes.getRadius()).north(runes.getRadius()),
                            pos.offset(runes.getOffset()).west(runes.getRadius()).south(runes.getRadius())
                    ).forEach(position -> {
                        if (!isInverted) {
                            if (level.getBlockState(position).getBlock() instanceof CropBlock && level.random.nextInt(100) < (10 + (tunneling ? 20 : 0))) {
                                BoneMealItem.applyBonemeal(runes.fauxBonemeal.copy(), level, position, null);
                                // This regular code (adapted from vanilla) does nothing server-side
//                            BoneMealItem.addGrowthParticles(level, position, 15);
                                // and I assume direct bone mealing is more efficient than scheduling like 10 times as many random ticks
//                            level.scheduleTick(position, crop, Mth.randomBetweenInclusive(level.random, 1, 20));
                                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, position.getX() + 0.5, position.getY() + 0.3, position.getZ() + 0.5, 5, 0.5, 0.3, 0.5, 0.05);
                                runes.didSomethingRecently = true;
                            }
                        } else if (level.getBlockState(position).getBlock() instanceof CropBlock cropBlock && cropBlock.isMaxAge(level.getBlockState(position))) {
                            // TODO: support gentle harvesting, possibly as a config option
                            // Might be a better way to hook into block drop logic
                            level.destroyBlock(position, true);
                        }
                    });
                } else if (runes.getItem(EFFECT).is(AIR_RUNE) && level.getGameTime() % (200 + extraDelay) == 1) {
                    @Nullable IItemHandler cap = level.getCapability(Capabilities.ItemHandler.BLOCK, getAttachedBlockPos(runes), state.getValue(RuneBlock.FACING).getOpposite());
                    if (null != cap) {
                        if (!isInverted) {
                            // Insert to inventory
                            if (!tunneling) {
                                runes.setRadius(runes.getItem(MODIFIER).is(WIDEN_RUNE) ? 8 : (runes.getItem(MODIFIER).is(NARROW_RUNE) ? 1 : 3));
                                runes.setHeight(2 * runes.getRadius() - 1);
                                runes.setOffset(BlockPos.ZERO.below(runes.getHeight() / 2));
                            }
                            // We already have one block accounted for, so we subtract that off
                            AABB boundingBox = new AABB(pos.offset(runes.getOffset())).inflate(Math.max(runes.getRadius() - 1, 0));
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
                        } else {
                            // Extract from inventory
                            runes.setRadius(0);
                            runes.setHeight(0);
                            runes.setOffset(BlockPos.ZERO);
                            for (int slot = 0; slot < cap.getSlots(); slot++) {
                                ItemStack toRemove = cap.extractItem(slot, cap.getSlotLimit(slot), true);
                                if (!toRemove.isEmpty()) {
                                    toRemove = cap.extractItem(slot, cap.getSlotLimit(slot), false).copy();
                                    BlockPos position = pos.relative(state.getValue(RuneBlock.FACING).getOpposite());
                                    ItemEntity freshEntity = new ItemEntity(level, position.getX(), position.getY(), position.getZ(), toRemove);
                                    freshEntity.setPickUpDelay((int) (tickRate * 20));
                                    level.addFreshEntity(freshEntity);
                                    runes.didSomethingRecently = true;
                                    break;
                                }
                            }
                        }
                    }
                } else if (runes.getItem(EFFECT).is(FIRE_RUNE) && level.getGameTime() % (20 + 10 * extraDelay) == 1) {
                    if (!tunneling) {
                        runes.setRadius(runes.getItem(MODIFIER).is(WIDEN_RUNE) ? 5 : 1);
                        runes.setHeight(1);
                        runes.setOffset(BlockPos.ZERO);
                    }
                    BlockPos.betweenClosed(
                            pos.offset(runes.getOffset()).east(runes.getRadius()).north(runes.getRadius()),
                            pos.offset(runes.getOffset()).west(runes.getRadius()).south(runes.getRadius())
                    ).forEach(position -> {
                        if (!isInverted) {
                            if (level.getBlockState(position).isAir() && Mth.randomBetweenInclusive(level.random, 1, 10) % 10 == 0) {
                                level.setBlock(position, Blocks.FIRE.defaultBlockState(), Block.UPDATE_ALL);
                                runes.didSomethingRecently = true;
                            }
                        } else if (level.getBlockState(position).is(Blocks.FIRE)) {
                            level.removeBlock(position, false);
                            runes.didSomethingRecently = true;
                        }
                    });
                } else if (runes.getItem(EFFECT).is(WATER_RUNE) && level.getGameTime() % (20 + 10 * extraDelay) == 2) {
                    if (!tunneling) {
                        runes.setRadius(runes.getItem(MODIFIER).is(WIDEN_RUNE) ? 3 : 0);
                        runes.setHeight(1);
                        runes.setOffset(BlockPos.ZERO.below());
                    }
                    if (runes.getRadius() != 0) {
                        BlockPos.betweenClosed(
                                pos.east(runes.getRadius()).north(runes.getRadius()).below(),
                                pos.west(runes.getRadius()).south(runes.getRadius()).below()
                        ).forEach(position -> {
                            if (!isInverted) {
                                if ((level.getBlockState(position).isAir() || (level.getBlockState(position).is(Blocks.WATER) && !level.getFluidState(position).isSource()))) {
                                    if (!level.dimensionType().ultraWarm() || runes.getItem(AMPLIFIER).is(AMPLIFY_RUNE)) {
                                        level.setBlock(position, Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
                                    } else {
                                        placeEvaporatedWater(level, pos);
                                    }
                                    runes.didSomethingRecently = true;
                                }
                            } else {
                                // Will work on virtually all modded fluids on the strength of the tag check alone.
                                // This is a bug on their end, `cause they were too lazy to properly implement things
                                if ((level.getFluidState(position).is(Tags.Fluids.WATER) ||
                                        level.getFluidState(position).is(Fluids.WATER) ||
                                        level.getBlockState(position).is(Blocks.WATER))) {
                                    level.setBlock(position, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                                    placeEvaporatedWater(level, pos);
                                    runes.didSomethingRecently = true;
                                }
                            }
                        });
                        // We need this, because, although #betweenClosed seems to be inclusive in general, it doesn't work for ranges of 1
                    } else {
                        if (!isInverted) {
                            if (level.getBlockState(pos.offset(runes.getOffset())).isAir()) {
                                if (!level.dimensionType().ultraWarm() || runes.getItem(AMPLIFIER).is(AMPLIFY_RUNE)) {
                                    level.setBlock(pos.offset(runes.getOffset()), Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
                                } else {
                                    placeEvaporatedWater(level, pos);
                                }
                                runes.didSomethingRecently = true;
                            }
                        } else if ((level.getFluidState(pos.offset(runes.getOffset())).is(Tags.Fluids.WATER) ||
                                level.getFluidState(pos.offset(runes.getOffset())).is(Fluids.WATER) ||
                                level.getBlockState(pos.offset(runes.getOffset())).is(Blocks.WATER))) {
                            level.setBlock(pos.offset(runes.getOffset()), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                            placeEvaporatedWater(level, pos.offset(runes.getOffset()));
                            runes.didSomethingRecently = true;
                        }
                    }
                }
            } else {
                runes.setRadius(0); // Enough to prevent BB rendering, so no point resetting the rest
            }
        }
    }
    
    public static BlockPos getPosInFrontOffset(BlockState runesState, int distance) {
        return switch (runesState.getValue(RuneBlock.FACING)) {
            case DOWN -> BlockPos.ZERO.below(distance);
            case UP -> BlockPos.ZERO.above(distance);
            case NORTH -> BlockPos.ZERO.north(distance);
            case SOUTH -> BlockPos.ZERO.south(distance);
            case WEST -> BlockPos.ZERO.west(distance);
            case EAST -> BlockPos.ZERO.east(distance);
        };
    }
    
    public static BlockPos getAttachedBlockPos(RuneBlockEntity runes) {
        return runes.getBlockPos().offset(getPosInFrontOffset(runes.getBlockState(), 1));
    }
    
    public static BlockPos getTunnelingOffset(BlockState runesState) {
        return getPosInFrontOffset(runesState, 2);
    }
    
    
    /**
     * Lightly edited from appropriate section of net.minecraft.world.item.BucketItem#emptyContents.
     */
    private static void placeEvaporatedWater(ServerLevel level, BlockPos pos) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        level.playSound(
                null,
                pos,
                SoundEvents.FIRE_EXTINGUISH,
                SoundSource.BLOCKS,
                0.5F,
                2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F
        );
        
        for (int index = 0; index < 8; index++) {
            level.sendParticles(
                    ParticleTypes.LARGE_SMOKE, (double) x + Math.random(), (double) y + Math.random(), (double) z + Math.random(), 1, 0.0, 0.0, 0.0, 0.01
            );
        }
    }

//    static AABB getUnitAABB(BlockPos pos, BlockPos offset) {
//        return BlockPos.betweenClosed(
//                pos,
//                pos,
//        );
//    }
    
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
    
    public void setRadius(int radius) {
        if (getRadius() != radius) {
            this.radius = radius;
            setChanged();
        }
    }
    
    public int getRadius() {
        
        return this.radius;
    }
    
    public void setHeight(int height) {
        if (getHeight() != height) {
            this.height = height;
            setChanged();
        }
    }
    
    public int getHeight() {
        
        return this.height;
    }
    
    public void setOffset(BlockPos offset) {
        if (getOffset() != offset) {
            this.offset = offset;
            setChanged();
        }
    }
    
    public BlockPos getOffset() {
        
        return this.offset;
    }
}
