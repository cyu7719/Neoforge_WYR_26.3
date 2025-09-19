package net.coba.wyr;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketAttemptPhase() implements CustomPacketPayload {

    public static final Type<PacketAttemptPhase> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(WYR.MODID, "attempt_phase"));
    public static final StreamCodec<FriendlyByteBuf, PacketAttemptPhase> STREAM_CODEC = StreamCodec.unit(new PacketAttemptPhase());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketAttemptPhase message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player && ChoiceEffects.hasEffect(player, "phasing")) {
                // The server now performs the teleport logic when it receives the packet.
                Direction facing = player.getDirection();
                BlockPos wallPos = player.blockPosition().relative(facing);
                BlockState wallState = player.level().getBlockState(wallPos);

                BlockPos wallPosHead = player.blockPosition().above().relative(facing);
                BlockState wallStateHead = player.level().getBlockState(wallPosHead);

                boolean isWallInFront = wallState.isCollisionShapeFullBlock(player.level(), wallPos) ||
                        wallStateHead.isCollisionShapeFullBlock(player.level(), wallPosHead);

                if (isWallInFront) {
                    BlockPos destPos = wallPos.relative(facing);
                    AABB destinationBox = player.getBoundingBox().move(
                            (destPos.getX() + 0.5) - player.getX(),
                            destPos.getY() - player.getY(),
                            (destPos.getZ() + 0.5) - player.getZ()
                    );

                    if (player.level().noCollision(player, destinationBox)) {
                        player.teleportTo(
                                destPos.getX() + 0.5,
                                destPos.getY(),
                                destPos.getZ() + 0.5
                        );
                    }
                }
            }
        });
    }
}