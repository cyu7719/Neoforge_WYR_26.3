package net.coba.wyr;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record PacketSyncChoiceList(List<Choice> choices) implements CustomPacketPayload {

    public static final Type<PacketSyncChoiceList> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WYR.MODID, "sync_choice_list"));

    // Create a separate codec for this packet that handles Choice objects
    public static final StreamCodec<FriendlyByteBuf, List<Choice>> CHOICE_LIST_CODEC = new StreamCodec<>() {
        @Override
        public List<Choice> decode(FriendlyByteBuf buf) {
            int size = buf.readVarInt();
            List<Choice> choices = new java.util.ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                String choiceId = buf.readUtf();
                Choice choice = ChoiceRegistry.getChoiceById(choiceId);
                if (choice != null) {
                    choices.add(choice);
                }
            }
            return choices;
        }

        @Override
        public void encode(FriendlyByteBuf buf, List<Choice> choices) {
            buf.writeVarInt(choices.size());
            for (Choice choice : choices) {
                buf.writeUtf(choice.getId());
            }
        }
    };

    public static final StreamCodec<FriendlyByteBuf, PacketSyncChoiceList> STREAM_CODEC = StreamCodec.composite(
            CHOICE_LIST_CODEC,
            PacketSyncChoiceList::choices,
            PacketSyncChoiceList::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSyncChoiceList message, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientPacketHandlers.handleSyncChoiceList(message);
        });
    }
}