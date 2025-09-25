package net.coba.wyr;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketChooseBiome(ResourceLocation biomeId) implements CustomPacketPayload {
    public static final Type<PacketChooseBiome> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(WYR.MODID, "choose_biome"));
    public static final StreamCodec<FriendlyByteBuf, PacketChooseBiome> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC,
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
                ServerLevel level = player.serverLevel();
                ResourceKey<Biome> biomeKey = ResourceKey.create(Registries.BIOME, message.biomeId());

                level.registryAccess().registryOrThrow(Registries.BIOME).getHolder(biomeKey).ifPresent(biomeHolder -> {
                    // --- FIX: Use a manual, reliable search method ---
                    BlockPos biomePos = findNearestBiome(level, player.blockPosition(), biomeHolder);

                    if (biomePos != null) {
                        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, biomePos.getX(), biomePos.getZ());
                        player.teleportTo(level, biomePos.getX() + 0.5, surfaceY + 1.0, biomePos.getZ() + 0.5, player.getYRot(), player.getXRot());
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
}