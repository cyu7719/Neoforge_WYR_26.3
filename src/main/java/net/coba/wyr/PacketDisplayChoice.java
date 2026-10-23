package net.coba.wyr;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record PacketDisplayChoice(Choice choice) implements CustomPacketPayload {

    public static final Type<PacketDisplayChoice> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WYR.MODID, "display_choice"));

    // Create a separate codec for this packet that handles Choice objects
    public static final StreamCodec<FriendlyByteBuf, Choice> CHOICE_CODEC = new StreamCodec<>() {
        @Override
        public Choice decode(FriendlyByteBuf buf) {
            String choiceId = buf.readUtf();
            return ChoiceRegistry.getChoiceById(choiceId);
        }

        @Override
        public void encode(FriendlyByteBuf buf, Choice choice) {
            buf.writeUtf(choice.getId());
        }
    };

    public static final StreamCodec<FriendlyByteBuf, PacketDisplayChoice> STREAM_CODEC = StreamCodec.composite(
            CHOICE_CODEC,
            PacketDisplayChoice::choice,
            PacketDisplayChoice::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketDisplayChoice message, IPayloadContext context) {
        context.enqueueWork(() -> {
            // This code runs on the client
            if (context.player() != null) {
                Choice choice = message.choice();

                // Build the formatted chat message
                Component header = Component.literal("You chose the following:").withStyle(ChatFormatting.BLUE);
                Component positive = Component.literal(" - " + choice.positive()).withStyle(ChatFormatting.GREEN);
                Component negative = Component.literal(" - " + choice.negative()).withStyle(ChatFormatting.RED);

                // Send the components to the player's chat
                context.player().sendSystemMessage(header);
                context.player().sendSystemMessage(positive);
                context.player().sendSystemMessage(negative);
            }
        });
    }
}