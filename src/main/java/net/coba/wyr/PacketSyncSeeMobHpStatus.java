package net.coba.wyr;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public record PacketSyncSeeMobHpStatus(boolean hasEffect) implements CustomPacketPayload {

    public static final Type<PacketSyncSeeMobHpStatus> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WYR.MODID, "sync_see_mob_hp_status"));

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
            if (Minecraft.getInstance().player != null) {
            }
            ClientEffectCache.setHasSeeMobHp(message.hasEffect());
        });
    }
}