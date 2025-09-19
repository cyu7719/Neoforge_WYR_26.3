package net.coba.wyr;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import java.util.List;

// Packet from Server to Client
public record PacketSyncNewChoices(List<Choice> choices) implements CustomPacketPayload {
    public static final Type<PacketSyncNewChoices> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(WYR.MODID, "sync_new_choices"));

    // Custom codec to write/read a list of Choice objects
    public static final StreamCodec<FriendlyByteBuf, List<Choice>> CHOICE_LIST_CODEC = new StreamCodec<>() {
        @Override
        public List<Choice> decode(FriendlyByteBuf buf) {
            return buf.readList(b -> ChoiceRegistry.getChoiceById(b.readUtf()));
        }

        @Override
        public void encode(FriendlyByteBuf buf, List<Choice> choices) {
            buf.writeCollection(choices, (b, choice) -> b.writeUtf(choice.getId()));
        }
    };

    public static final StreamCodec<FriendlyByteBuf, PacketSyncNewChoices> STREAM_CODEC = StreamCodec.composite(
            CHOICE_LIST_CODEC,
            PacketSyncNewChoices::choices,
            PacketSyncNewChoices::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSyncNewChoices message, IPayloadContext context) {
        context.enqueueWork(() -> {
            // Client receives the choices and opens the screen
            if (!message.choices().isEmpty()) {
                Minecraft.getInstance().setScreen(new WyrScreen(message.choices()));
            }
        });
    }
}