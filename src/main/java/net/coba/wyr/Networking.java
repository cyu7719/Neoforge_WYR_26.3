package net.coba.wyr;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
// The import for IPayloadRegistrar has been completely removed.

public class Networking {
    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(Networking::registerPayloads);
    }

    private static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(WYR.MODID);

        registrar.playToClient(PacketOpenBiomeGui.TYPE, PacketOpenBiomeGui.STREAM_CODEC, PacketOpenBiomeGui::handle);
        registrar.playToServer(PacketChooseBiome.TYPE, PacketChooseBiome.STREAM_CODEC, PacketChooseBiome::handle);

        registrar.playToClient(PacketSyncSeeMobHpStatus.TYPE, PacketSyncSeeMobHpStatus.STREAM_CODEC, PacketSyncSeeMobHpStatus::handle);

        registrar.playToClient(PacketSyncScrambleState.TYPE, PacketSyncScrambleState.STREAM_CODEC, PacketSyncScrambleState::handle);

        registrar.playToClient(PacketSyncRandomSoundsStatus.TYPE, PacketSyncRandomSoundsStatus.STREAM_CODEC, PacketSyncRandomSoundsStatus::handle);

        registrar.playToClient(net.coba.wyr.PacketSyncShaderStatus.TYPE, net.coba.wyr.PacketSyncShaderStatus.STREAM_CODEC, net.coba.wyr.PacketSyncShaderStatus::handle);

        registrar.playToServer(PacketSetPromptInterval.TYPE, PacketSetPromptInterval.STREAM_CODEC, PacketSetPromptInterval::handle);

        registrar.playToServer(PacketTeleport.TYPE, PacketTeleport.STREAM_CODEC, PacketTeleport::handle);

        registrar.playToServer(PacketApplyEffect.TYPE, PacketApplyEffect.STREAM_CODEC, PacketApplyEffect::handle);

        registrar.playToServer(PacketAttemptPhase.TYPE, PacketAttemptPhase.STREAM_CODEC, PacketAttemptPhase::handle);

        registrar.playToServer(PacketRequestChoiceList.TYPE, PacketRequestChoiceList.STREAM_CODEC, PacketRequestChoiceList::handle);
        registrar.playToClient(PacketSyncChoiceList.TYPE, PacketSyncChoiceList.STREAM_CODEC, PacketSyncChoiceList::handle);

        registrar.playToServer(PacketRequestNewChoices.TYPE, PacketRequestNewChoices.STREAM_CODEC, PacketRequestNewChoices::handle);
        registrar.playToClient(PacketSyncNewChoices.TYPE, PacketSyncNewChoices.STREAM_CODEC, PacketSyncNewChoices::handle);

        registrar.playToClient(PacketSyncThirst.TYPE, PacketSyncThirst.STREAM_CODEC, PacketSyncThirst::handle);

        registrar.playToClient(PacketSyncStackedLogsStatus.TYPE, PacketSyncStackedLogsStatus.STREAM_CODEC, PacketSyncStackedLogsStatus::handle);

        registrar.playToClient(PacketPlayCustomSound.TYPE, PacketPlayCustomSound.STREAM_CODEC, PacketPlayCustomSound::handle);
    }
}