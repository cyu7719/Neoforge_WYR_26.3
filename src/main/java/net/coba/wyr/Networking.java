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

        registrar.playToServer(PacketTeleport.TYPE, PacketTeleport.STREAM_CODEC, PacketTeleport::handle);

        registrar.playToServer(PacketApplyEffect.TYPE, PacketApplyEffect.STREAM_CODEC, PacketApplyEffect::handle);

        registrar.playToServer(PacketAttemptPhase.TYPE, PacketAttemptPhase.STREAM_CODEC, PacketAttemptPhase::handle);

        registrar.playToServer(PacketRequestChoiceList.TYPE, PacketRequestChoiceList.STREAM_CODEC, PacketRequestChoiceList::handle);
        registrar.playToClient(PacketSyncChoiceList.TYPE, PacketSyncChoiceList.STREAM_CODEC, PacketSyncChoiceList::handle);

        registrar.playToServer(PacketRequestNewChoices.TYPE, PacketRequestNewChoices.STREAM_CODEC, PacketRequestNewChoices::handle);
        registrar.playToClient(PacketSyncNewChoices.TYPE, PacketSyncNewChoices.STREAM_CODEC, PacketSyncNewChoices::handle);

        registrar.playToClient(PacketSyncThirst.TYPE, PacketSyncThirst.STREAM_CODEC, PacketSyncThirst::handle);

        registrar.playToClient(PacketSyncStackedLogsStatus.TYPE, PacketSyncStackedLogsStatus.STREAM_CODEC, PacketSyncStackedLogsStatus::handle);
    }
}