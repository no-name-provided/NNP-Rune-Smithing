package com.github.no_name_provided.nnp_rune_smithing.common.events;

import com.github.no_name_provided.nnp_rune_smithing.common.RSServerConfig;
import com.github.no_name_provided.nnp_rune_smithing.common.attachments.RSAttachments;
import com.github.no_name_provided.nnp_rune_smithing.common.attachments.WardedBlocksFromWardRune;
import com.github.no_name_provided.nnp_rune_smithing.common.data_components.RunesAdded;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDestroyBlockEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;

import java.util.HashSet;
import java.util.Optional;

import static com.github.no_name_provided.nnp_rune_smithing.NNPRuneSmithing.MODID;
import static com.github.no_name_provided.nnp_rune_smithing.common.data_components.RSDataComponents.RUNES_ADDED;
import static com.github.no_name_provided.nnp_rune_smithing.common.items.RSItems.*;

@EventBusSubscriber(modid = MODID)
public class BreakEvents {
    // Unsynced client only constants - distinct values for each player, don't access on server
    static int miningDuration;
    static BlockPos targetedForBreaking;
    
    /**
     * Special breaking logic. Mostly reimplementing vanilla behavior for chain mined or hammered blocks.
     * <p>
     * As alternatives, consider calling ServerPlayer#gameMode#destroyBlock and passing it a version of the tool without
     * runes attached (to avoid recursion issues) or simply using an event earlier in the call chain.
     * </p>
     */
    @SubscribeEvent
    static void onBlockBreak(BlockEvent.BreakEvent event) {
        // Handle warded blocks
        if (event.getLevel().getChunk(event.getPos()).getData(RSAttachments.BLOCKS_WARDED_BY_WARD_RUNE).wardedBlocks().contains(event.getPos())) {
            event.setCanceled(true);
        }
        
        // Handle enchanted tools
        ItemStack tool = event.getPlayer().getMainHandItem();
        if (!event.isCanceled() && !tool.isEmpty() && event.getPlayer() instanceof ServerPlayer player) {
            RunesAdded runesAdded = tool.get(RUNES_ADDED);
            if (null != runesAdded && runesAdded.amplifier().rune() != CONTAIN_RUNE.get()) {
                int tier = runesAdded.effectiveTier();
                if (runesAdded.target().rune() == COLLISION_RUNE.get() && tier > 0) {
                    if (runesAdded.effect().rune() == EARTH_RUNE.get()) {
                        int radius = 1;
                        if (runesAdded.modifier().rune() == WIDEN_RUNE.get()) {
                            radius++;
                        } else if (runesAdded.modifier().rune() == NARROW_RUNE.get()) {
                            radius--;
                        }
                        // Event is only thrown on server and player is already an instance of server player...
                        ServerLevel level = (ServerLevel) player.level();
                        BlockPos pos = event.getPos();
                        Pair<BlockPos, BlockPos> posPair = getStartEndBreakPositions(pos, player, radius);
                        BlockPos.betweenClosed(posPair.getFirst(), posPair.getSecond()).forEach(position -> {
                            BlockState stateToHarvest = level.getBlockState(position);
                            Block blockToHarvest = stateToHarvest.getBlock();
                            BlockEntity entityToHarvest = level.getBlockEntity(position);
                            
                            boolean isWarded = false;
                            Optional<WardedBlocksFromWardRune> wardedBlocks = level.getChunkAt(position).getExistingData(RSAttachments.BLOCKS_WARDED_BY_WARD_RUNE);
                            if (wardedBlocks.isPresent()) {
                                isWarded = wardedBlocks.get().wardedBlocks().contains(position);
                            }
                            // Reference: net.minecraft.server.level.ServerPlayerGameMode.destroyBlock
                            // Not calling it directly via ServerPlayer#gameMode#destroyBlock, because then my event handler would call itself
                            if (stateToHarvest.canHarvestBlock(level, position, player) &&
                                    // Skip warded blocks
                                    !isWarded &&
                                    // Skip indestructible blocks, like bedrock
                                    blockToHarvest.defaultDestroyTime() >= 0 &&
                                    tool.isCorrectToolForDrops(stateToHarvest) &&
                                    // Account for spawn chunk protection and world border
                                    player.mayInteract(level, position) &&
                                    // Check game mode
                                    !player.blockActionRestricted(level, position, player.gameMode.getGameModeForPlayer()) &&
                                    !(blockToHarvest instanceof GameMasterBlock && !player.canUseGameMasterBlocks())) {
                                stateToHarvest = blockToHarvest.playerWillDestroy(level, position, stateToHarvest, player);
                                boolean wasDestroyed = stateToHarvest.onDestroyedByPlayer(level, position, player, false, level.getFluidState(position));
                                if (player.isCreative() && wasDestroyed) {
                                    stateToHarvest.getBlock().destroy(level, position, stateToHarvest);
                                    
                                    // In iterable foreach loops, return apparently functions as continue. Shrug, that's Java.
                                    return;
                                }
                                // Increment item use statistics
                                tool.mineBlock(level, stateToHarvest, pos, player);
                                if (wasDestroyed && stateToHarvest.canHarvestBlock(level, position, player)) {
                                    level.destroyBlock(position, true, player);
                                    // Handle block break statistics
                                    blockToHarvest.playerDestroy(level, player, position, stateToHarvest, entityToHarvest, tool);
                                    
                                    // We should never get here with an empty item stack (no components)
                                    if (tool.isEmpty()) {
                                        net.neoforged.neoforge.event.EventHooks.onPlayerDestroyItem(player, tool, InteractionHand.MAIN_HAND);
                                    }
                                }
                            }
                        });
                    } else if (runesAdded.effect().rune() == LIGHT_RUNE.get()) {
                        int expectedExperience = event.getState().getExpDrop(
                                event.getLevel(),
                                event.getPos(),
                                event.getLevel().getBlockEntity(event.getPos()),
                                player,
                                tool
                        );
                        if (expectedExperience > 0) {
                            event.getLevel().addFreshEntity(new ExperienceOrb(player.level(), event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(), RSServerConfig.breakingXPPerTier * tier));
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Let players know they can't harvest warded blocks (and slows down breaking progress). Final breaking is
     * separately prevented elsewhere.
     */
    @SubscribeEvent
    static void onHarvestCheck(PlayerEvent.HarvestCheck event) {
        // While this is generally the same as the BlockGetter passed into the event, it isn't guaranteed.
        // We need a proper level for chunk access
        Level level = event.getEntity().level();
        Optional<WardedBlocksFromWardRune> wardedBlocks = level.getChunk(event.getPos())
                .getExistingData(RSAttachments.BLOCKS_WARDED_BY_WARD_RUNE);
        wardedBlocks.ifPresent(set ->
                event.setCanHarvest(!set.wardedBlocks().contains(event.getPos()))
        );
    }
    
    /**
     * Should outright stop braking progress for warded blocks. Final breaking is
     * separately prevented elsewhere.
     */
    @SubscribeEvent
    static void onCheckBreakSpeed(PlayerEvent.BreakSpeed event) {
        // If we don't have the position, we can't make sure it's not warded. Hard to get around that
        if (event.getPosition().isPresent()) {
            BlockPos pos = event.getPosition().get();
            Level level = event.getEntity().level();
            Optional<WardedBlocksFromWardRune> wardedBlocks = level.getChunk(pos)
                    .getExistingData(RSAttachments.BLOCKS_WARDED_BY_WARD_RUNE);
            wardedBlocks.ifPresent(blocks ->
                    event.setCanceled(blocks.wardedBlocks().contains(pos))
            );
        }
    }
    
    /**
     * Maps axis to corresponding block range. Would be better if I spun up a clip context and used the targeted face,
     * but oh well.
     */
    public static Pair<BlockPos, BlockPos> getStartEndBreakPositions(BlockPos pos, Player player, int radius) {
        
        // Can't get clicked face from this event?
        Direction direction = player.getNearestViewDirection();
        Direction.Axis orientation = direction.getAxis();
        return switch (orientation) {
            // East - West
            case X -> Pair.of(pos.above(radius).north(radius), pos.below(radius).south(radius));
            // Up - Down
            case Y -> Pair.of(pos.west(radius).south(radius), pos.east(radius).north(radius));
            // North - South
            case Z -> Pair.of(pos.above(radius).east(radius), pos.below(radius).west(radius));
        };
    }
    
    /**
     * References:
     * {@link
     * net.minecraft.world.level.block.state.BlockBehaviour#getDestroyProgress(net.minecraft.world.level.block.state.BlockState,
     * net.minecraft.world.entity.player.Player, net.minecraft.world.level.BlockGetter, net.minecraft.core.BlockPos)}
     * {@link ServerPlayerGameMode#incrementDestroyProgress(BlockState, BlockPos, int)}
     * {@link LevelRenderer#destroyBlockProgress(int, BlockPos, int)}
     */
    @SubscribeEvent
    static void onPlayerLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        ItemStack useItem = event.getItemStack();
        if (!useItem.isEmpty()) {
            RunesAdded runesAdded = useItem.get(RUNES_ADDED);
            if (null != runesAdded && runesAdded.amplifier().rune() != CONTAIN_RUNE.get()) {
                if (runesAdded.effect().rune() == EARTH_RUNE.get()) {
                    BlockPos pos = event.getPos();
                    Level level = event.getLevel();
                    int radius = 1;
                    if (runesAdded.modifier().rune() == WIDEN_RUNE.get()) {
                        radius++;
                    } else if (runesAdded.modifier().rune() == NARROW_RUNE.get()) {
                        radius--;
                    }
                    Player player = event.getEntity();
                    float destroyProgress = level.getBlockState(pos).getDestroyProgress(player, level, pos) * (float) (1 + miningDuration) * 10f;
                    Pair<BlockPos, BlockPos> posPair = getStartEndBreakPositions(pos, player, radius);
                    Iterable<BlockPos> breakingPositions = BlockPos.betweenClosed(posPair.getFirst(), posPair.getSecond());
                    
                    if (event.getAction().equals(PlayerInteractEvent.LeftClickBlock.Action.START)) {
                        // Both buses
                        onStartHammering(level, pos);
                    } else if (event.getAction().equals(PlayerInteractEvent.LeftClickBlock.Action.STOP) || (event.getAction().equals(PlayerInteractEvent.LeftClickBlock.Action.ABORT))) {
                        // Server only
                        onStopHammering(player, level, pos, breakingPositions);
                    } else if (event.getAction().equals(PlayerInteractEvent.LeftClickBlock.Action.CLIENT_HOLD)) {
                        // Client only
                        onContinueHammering(player, level, destroyProgress, pos, breakingPositions);
                    }
                }
            }
        }
    }
    
    /**
     * Fired on both buses when the player depresses the attack key while targeting a block.
     */
    private static void onStartHammering(Level level, BlockPos center) {
        if (level.isClientSide()) {
            miningDuration = 0;
            targetedForBreaking = center;
        }
    }
    
    /**
     * Fired on the server when the player stops attacking a block.
     */
    private static void onStopHammering(Player player, Level level, BlockPos center, Iterable<BlockPos> breakingPositions) {
        // These are secretly MutableBlockPos
        breakingPositions.forEach(position -> {
            if (!position.equals(center) && !level.getBlockState(position).isAir()) {
                player.level().destroyBlockProgress(player.getId() + position.hashCode(), position.immutable(), -1);
            }
        });
    }
    
    /**
     * Fired on the client when the player holds the attack key on a block.
     */
    private static void onContinueHammering(Player player, Level level, float destroyProgress, BlockPos targetPos, Iterable<BlockPos> breakingPositions) {
        // Handle player aim wandering
        if (targetedForBreaking.equals(targetPos)) {
            miningDuration++;
        } else {
            miningDuration = 0;
            targetedForBreaking = targetPos;
        }
        // Update breaking status
        // These are secretly MutableBlockPos
        breakingPositions.forEach(position -> {
            if (targetedForBreaking != position && !level.getBlockState(position).isAir()) {
                if (level.isClientSide()) {
                    level.destroyBlockProgress(player.getId() + position.hashCode(), position.immutable(), (int) destroyProgress);
                } else {
                    ((ServerPlayer) player).connection.send(new ClientboundBlockDestructionPacket(player.getId() + position.hashCode(), position.immutable(), -1));
                }
            }
        });
    }
    
    @SubscribeEvent
    private static void onLivingDestroyBlock(LivingDestroyBlockEvent event) {
        if (!event.isCanceled()) {
            Level level = event.getEntity().level();
            // Don't let mobs break warded blocks
            Optional<WardedBlocksFromWardRune> wardedBlocksOptional = level.getChunk(event.getPos()).getExistingData(RSAttachments.BLOCKS_WARDED_BY_WARD_RUNE);
            if (wardedBlocksOptional.isPresent() && wardedBlocksOptional.get().wardedBlocks().contains(event.getPos())) {
                event.setCanceled(true);
            }
        }
    }
    
    @SubscribeEvent
    private static void onExplosionDetonate(ExplosionEvent.Detonate event) {
        Level level = event.getLevel();
        event.getAffectedBlocks().removeIf(pos ->
                // Ignore warded blocks
                // No guarantee all blocks are in same chunk. Can do some caching if this becomes an issue
                level.getChunk(pos).getExistingData(RSAttachments.BLOCKS_WARDED_BY_WARD_RUNE)
                        .orElse(new WardedBlocksFromWardRune(new HashSet<>())).wardedBlocks().contains(pos)
        );
    }
}
