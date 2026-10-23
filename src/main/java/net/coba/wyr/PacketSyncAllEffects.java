package net.coba.wyr;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public record PacketSyncAllEffects(List<String> effects) implements CustomPacketPayload {

    public static final Type<PacketSyncAllEffects> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WYR.MODID, "sync_all_effects"));

    public static final StreamCodec<FriendlyByteBuf, PacketSyncAllEffects> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
            PacketSyncAllEffects::effects,
            PacketSyncAllEffects::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSyncAllEffects message, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientEffectCache.syncEffects(new HashSet<>(message.effects()));
        });
    }
}
