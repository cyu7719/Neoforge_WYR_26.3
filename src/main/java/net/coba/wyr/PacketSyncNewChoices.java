package net.coba.wyr;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import java.util.List;

// Packet from Server to Client - simplified to use only choice IDs
public record PacketSyncNewChoices(List<String> choiceIds) implements CustomPacketPayload {
    public static final Type<PacketSyncNewChoices> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WYR.MODID, "sync_new_choices"));

    // Custom codec to write/read a list of choice IDs (strings)
    public static final StreamCodec<FriendlyByteBuf, List<String>> CHOICE_ID_LIST_CODEC = new StreamCodec<>() {
        @Override
        public List<String> decode(FriendlyByteBuf buf) {
            int size = buf.readVarInt();
            List<String> choiceIds = new java.util.ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                choiceIds.add(buf.readUtf());
            }
            return choiceIds;
        }

        @Override
        public void encode(FriendlyByteBuf buf, List<String> choiceIds) {
            buf.writeVarInt(choiceIds.size());
            for (String choiceId : choiceIds) {
                buf.writeUtf(choiceId);
            }
        }
    };

    public static final StreamCodec<FriendlyByteBuf, PacketSyncNewChoices> STREAM_CODEC = StreamCodec.composite(
            CHOICE_ID_LIST_CODEC,
            PacketSyncNewChoices::choiceIds,
            PacketSyncNewChoices::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSyncNewChoices message, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientPacketHandlers.handleSyncNewChoices(message);
        });
    }
}