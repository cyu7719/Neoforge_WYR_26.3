package net.coba.wyr;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSyncFoodTimer(int secondsRemaining) implements CustomPacketPayload {
    public static final Type<PacketSyncFoodTimer> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WYR.MODID, "sync_food_timer"));
    public static final StreamCodec<FriendlyByteBuf, PacketSyncFoodTimer> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, PacketSyncFoodTimer::secondsRemaining, PacketSyncFoodTimer::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(PacketSyncFoodTimer message, IPayloadContext context) {
        context.enqueueWork(() -> ClientEffectCache.setFoodTimerSeconds(message.secondsRemaining()));
    }
}
