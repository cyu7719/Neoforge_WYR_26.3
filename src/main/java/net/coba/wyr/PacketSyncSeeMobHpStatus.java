package net.coba.wyr;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSyncSeeMobHpStatus(boolean hasEffect) implements CustomPacketPayload {

    public static final Type<PacketSyncSeeMobHpStatus> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(WYR.MODID, "sync_see_mob_hp_status"));

    public static final StreamCodec<FriendlyByteBuf, PacketSyncSeeMobHpStatus> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            PacketSyncSeeMobHpStatus::hasEffect,
            PacketSyncSeeMobHpStatus::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSyncSeeMobHpStatus message, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientEffectCache.setHasSeeMobHp(message.hasEffect());
        });
    }
}