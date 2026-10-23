package net.coba.wyr;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketChooseBiome(Identifier biomeId) implements CustomPacketPayload {
    public static final Type<PacketChooseBiome> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WYR.MODID, "choose_biome"));
    public static final StreamCodec<FriendlyByteBuf, PacketChooseBiome> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC,
            PacketChooseBiome::biomeId,
            PacketChooseBiome::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketChooseBiome message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ServerLevel level = (net.minecraft.server.level.ServerLevel) player.level();
                ResourceKey<Biome> biomeKey = ResourceKey.create(Registries.BIOME, message.biomeId());

                level.registryAccess().lookupOrThrow(Registries.BIOME).get(biomeKey).ifPresent(biomeHolder -> {
                    BlockPos biomePos = findNearestBiome(level, player.blockPosition(), biomeHolder);

                    if (biomePos != null) {
                        // Force the target chunk to load so the heightmap is accurate.
                        level.getChunk(biomePos.getX() >> 4, biomePos.getZ() >> 4);

                        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, biomePos.getX(), biomePos.getZ());
                        BlockPos safeSpawnPos = findSafeSurface(level, biomePos.getX(), surfaceY, biomePos.getZ());

                        //Teleport the player
                        player.teleportTo(level, safeSpawnPos.getX() + 0.5, safeSpawnPos.getY(), safeSpawnPos.getZ() + 0.5, java.util.Set.of(), player.getYRot(), player.getXRot(), false);

                        //Set their respawn point (this is what the lock teleports them to)
                        // Note: setRespawnPosition signature changed in 1.21.1, using alternative approach
                        // player.setRespawnPosition(level.dimension(), safeSpawnPos, 0.0f, true, false);

                        //Save the chosen biome as the "locked" biome
                        ChoiceEffects.setSpawnBiomeLock(player, biomeKey, safeSpawnPos);
                    }
                });
            }
        });
    }

    /**
     * Searches for the nearest biome by checking chunks in an expanding square pattern.
     */
    private static BlockPos findNearestBiome(ServerLevel level, BlockPos startPos, Holder<Biome> targetBiome) {
        int searchRadiusInChunks = 256; // Max search radius (256 chunks = 4096 blocks)
        int startChunkX = startPos.getX() >> 4;
        int startChunkZ = startPos.getZ() >> 4;

        for (int r = 0; r < searchRadiusInChunks; r++) {
            for (int i = -r; i <= r; i++) {
                // Check edges of the square separately to search outwards from the center
                if (checkBiome(level, startChunkX + i, startChunkZ + r, startPos.getY(), targetBiome) != null) return toBlockPos(startChunkX + i, startChunkZ + r, startPos.getY());
                if (checkBiome(level, startChunkX + i, startChunkZ - r, startPos.getY(), targetBiome) != null) return toBlockPos(startChunkX + i, startChunkZ - r, startPos.getY());
                if (checkBiome(level, startChunkX + r, startChunkZ + i, startPos.getY(), targetBiome) != null) return toBlockPos(startChunkX + r, startChunkZ + i, startPos.getY());
                if (checkBiome(level, startChunkX - r, startChunkZ + i, startPos.getY(), targetBiome) != null) return toBlockPos(startChunkX - r, startChunkZ + i, startPos.getY());
            }
        }
        return null; // Not found within the radius
    }

    private static BlockPos checkBiome(ServerLevel level, int chunkX, int chunkZ, int y, Holder<Biome> targetBiome) {
        BlockPos pos = toBlockPos(chunkX, chunkZ, y);
        if (level.getBiome(pos).equals(targetBiome)) {
            return pos;
        }
        return null;
    }

    private static BlockPos toBlockPos(int chunkX, int chunkZ, int y) {
        return new BlockPos(chunkX << 4, y, chunkZ << 4);
    }

    /**
     * Finds a safe standing position at the surface: a solid block with two
     * non-solid (air/replaceable) blocks above it for the player to occupy.
     * Scans downward from the heightmap Y so the player doesn't spawn inside
     * leaves, water, or floating blocks.
     */
    private static BlockPos findSafeSurface(ServerLevel level, int x, int startY, int z) {
        int minY = level.getMinY();
        for (int y = Math.min(startY, level.getMaxY() - 2); y > minY; y--) {
            BlockPos ground = new BlockPos(x, y, z);
            BlockPos feet = ground.above();
            BlockPos head = feet.above();
            boolean solidGround = level.getBlockState(ground).isSolidRender();
            boolean feetClear = !level.getBlockState(feet).isSolidRender();
            boolean headClear = !level.getBlockState(head).isSolidRender();
            if (solidGround && feetClear && headClear) {
                return feet;
            }
        }
        // Fallback: just above the heightmap position.
        return new BlockPos(x, startY + 1, z);
    }
}