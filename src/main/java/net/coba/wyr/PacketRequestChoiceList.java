package net.coba.wyr;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.stream.Collectors;

public record PacketRequestChoiceList() implements CustomPacketPayload {

    public static final Type<PacketRequestChoiceList> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(WYR.MODID, "request_choice_list"));
    public static final StreamCodec<FriendlyByteBuf, PacketRequestChoiceList> STREAM_CODEC = StreamCodec.unit(new PacketRequestChoiceList());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketRequestChoiceList message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                List<Choice> choices = PlayerChoiceTracker.getChoices(serverPlayer);
                PacketDistributor.sendToPlayer(serverPlayer, new PacketSyncChoiceList(choices));
            }
        });
    }
}