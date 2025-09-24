// PacketPlayCustomSound.java
package net.coba.wyr;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import java.util.Optional;

// MODIFICATION: Added float for volume and pitch
public record PacketPlayCustomSound(ResourceLocation soundId, float volume, float pitch) implements CustomPacketPayload {

    // MODIFICATION: Changed the TYPE to be more generic, as this packet now plays any custom sound.
    public static final Type<PacketPlayCustomSound> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(WYR.MODID, "play_custom_sound"));

    // MODIFICATION: Updated StreamCodec to handle the new float values
    public static final StreamCodec<FriendlyByteBuf, PacketPlayCustomSound> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC,
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

            Player player = Minecraft.getInstance().player;
            if (player == null) return;

            // Find the registered SoundEvent from the ID
            Optional<SoundEvent> soundEvent = BuiltInRegistries.SOUND_EVENT.getOptional(message.soundId());

            // MODIFICATION: Use the volume and pitch from the packet message
            soundEvent.ifPresent(event -> player.level().playSound(
                    player,
                    player.blockPosition(),
                    event,
                    SoundSource.PLAYERS,
                    message.volume(), // Use volume from packet
                    message.pitch()   // Use pitch from packet
            ));
        });
    }
}