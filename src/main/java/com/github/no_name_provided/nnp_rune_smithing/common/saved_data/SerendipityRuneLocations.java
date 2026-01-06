package com.github.no_name_provided.nnp_rune_smithing.common.saved_data;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

public class SerendipityRuneLocations extends SavedData {
    HashMap<ChunkPos, HashSet<Pair<BlockPos, Float>>> locationsAndStrengths = new HashMap<>();
    
    public SerendipityRuneLocations(Map<ChunkPos, HashSet<Pair<BlockPos, Float>>> map) {
        super();
        locationsAndStrengths.putAll(map);
    }
    
    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        // We use a codec here, because I'm more familiar with the codec helpers and I struggled with the direct NBT encoding.
        // Also, Minecraft is generally transitioning towards codecs
        DataResult<Tag> result = CODEC.encodeStart(NbtOps.INSTANCE, this);
        tag.put("locations", result.getOrThrow());
        
        return tag;
    }
    
    /**
     * Load existing instance of saved data
     */
    public static SerendipityRuneLocations load(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        return CODEC.decode(NbtOps.INSTANCE, tag.get("locations")).getOrThrow().getFirst();
    }
    
    public static Codec<SerendipityRuneLocations> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    // Requires keys encode/decode as strings
                    Codec.unboundedMap(
                                    Codec.STRING.xmap(
                                            string -> new ChunkPos(Long.decode(string)),
                                            chunkPos -> Long.toString(chunkPos.toLong())
                                    ),
                                    // Pairs only work with codecs that have associated fields, like codecs for records or converted map codecs
                                    Codec.list(Codec.pair(BlockPos.CODEC.fieldOf("blockpos").codec(), Codec.FLOAT.fieldOf("strength").codec())).xmap(
                                            HashSet::new,
                                            ArrayList::new
                                    )
                            ).fieldOf("serendipity_rune_locations")
                            .forGetter(SerendipityRuneLocations::getLocationsAndStrengths)
            ).apply(instance, SerendipityRuneLocations::new));
    
    public HashMap<ChunkPos, HashSet<Pair<BlockPos, Float>>> getLocationsAndStrengths() {
        
        return locationsAndStrengths;
    }
    
    public void add(ChunkPos chunkPos, BlockPos pos, float strength) {
        Set<Pair<BlockPos, Float>> set = locationsAndStrengths.computeIfAbsent(chunkPos, cPos -> new HashSet<>());
        set.add(Pair.of(pos, strength));
        setDirty();
    }
    
    public void remove(ChunkPos chunkPos, BlockPos pos) {
        Set<Pair<BlockPos, Float>> set = locationsAndStrengths.get(chunkPos);
        if (null != set) {
            set.removeIf(pair -> pair.getFirst().equals(pos));
            setDirty();
        }
    }
    
    public void update(ChunkPos chunkPos, BlockPos pos, float strength) {
        remove(chunkPos, pos);
        add(chunkPos, pos, strength);
        //setDirty is handled in each method call
    }
    
    public static SerendipityRuneLocations get(ServerLevel sLevel) {
        
        return sLevel.getDataStorage().computeIfAbsent(new SerendipityRuneLocations.Factory<>(() -> new SerendipityRuneLocations(Map.of()), SerendipityRuneLocations::load), "serendipity_rune_locations");
    }
}
