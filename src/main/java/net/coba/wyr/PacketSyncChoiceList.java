package net.coba.wyr;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record PacketSyncChoiceList(List<Choice> choices) implements CustomPacketPayload {

    public static final Type<PacketSyncChoiceList> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(WYR.MODID, "sync_choice_list"));

    // We can reuse the same codec from PacketSyncNewChoices to send a list of Choices by their ID.
    public static final StreamCodec<FriendlyByteBuf, PacketSyncChoiceList> STREAM_CODEC = StreamCodec.composite(
            PacketSyncNewChoices.CHOICE_LIST_CODEC,
            PacketSyncChoiceList::choices,
            PacketSyncChoiceList::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSyncChoiceList message, IPayloadContext context) {
        context.enqueueWork(() -> {
            // This runs on the client.
            Minecraft.getInstance().setScreen(new PromptListScreen(message.choices()));
        });
    }
}