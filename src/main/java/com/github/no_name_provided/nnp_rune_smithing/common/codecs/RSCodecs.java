package com.github.no_name_provided.nnp_rune_smithing.common.codecs;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Helper class for some codecs that are particularly long, or likely to be reused.
 */
public class RSCodecs {
    public static Codec<ChunkPos> CHUNK_POS = Codec.LONG.xmap(ChunkPos::new, ChunkPos::toLong);
    
    /**
     * This exists to clear up IDE nonsense created by implicitly permitting subtypes of wildcards.
     */
    public static class AnnoyinglyTypedCodecs<T, R> {
        public Codec<HashSet<T>> hashSet(Codec<T> valueCodec) {
            
            return Codec.list(valueCodec).xmap(HashSet::new, ArrayList::new);
        }
    }
   
    
}
