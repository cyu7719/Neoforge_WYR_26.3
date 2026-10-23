package net.coba.wyr;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSyncScrambleState(int scrambleState) implements CustomPacketPayload {

    public static final Type<PacketSyncScrambleState> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WYR.MODID, "sync_scramble_state"));
    public static final StreamCodec<FriendlyByteBuf, PacketSyncScrambleState> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            PacketSyncScrambleState::scrambleState,
            PacketSyncScrambleState::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSyncScrambleState message, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientEffectCache.setScrambleState(message.scrambleState());
        });
    }
}