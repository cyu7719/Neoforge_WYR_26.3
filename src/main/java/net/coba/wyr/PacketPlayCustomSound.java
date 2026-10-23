// PacketPlayCustomSound.java
package net.coba.wyr;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import java.util.Optional;

// MODIFICATION: Added float for volume and pitch
public record PacketPlayCustomSound(Identifier soundId, float volume, float pitch) implements CustomPacketPayload {

    // MODIFICATION: Changed the TYPE to be more generic, as this packet now plays any custom sound.
    public static final Type<PacketPlayCustomSound> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WYR.MODID, "play_custom_sound"));

    // MODIFICATION: Updated StreamCodec to handle the new float values
    public static final StreamCodec<FriendlyByteBuf, PacketPlayCustomSound> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC,
            PacketPlayCustomSound::soundId,
            ByteBufCodecs.FLOAT,
            PacketPlayCustomSound::volume,
            ByteBufCodecs.FLOAT,
            PacketPlayCustomSound::pitch,
            PacketPlayCustomSound::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketPlayCustomSound message, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientPacketHandlers.handlePlayCustomSound(message);
        });
    }
}