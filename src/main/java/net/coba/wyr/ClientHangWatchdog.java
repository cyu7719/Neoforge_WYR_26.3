package net.coba.wyr;

import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Map;

/**
 * Detects when the client render thread OR the integrated server thread stops
 * ticking (e.g. blocked during world load) and dumps stack traces to the log
 * so we can see exactly which call is hanging.
 */
public class ClientHangWatchdog {
    private static volatile long lastRenderTickMs = System.currentTimeMillis();
    private static volatile long lastServerTickMs = System.currentTimeMillis();
    private static volatile boolean started = false;
    private static volatile long lastDumpMs = 0;
    private static volatile long stuckLoadingSinceMs = 0;

    private static final long HANG_THRESHOLD_MS = 8000; // no tick for 8s = hung
    private static final long STUCK_LOADING_MS = 15000; // server has player but client has no level for 15s
    private static final long DUMP_COOLDOWN_MS = 15000; // don't spam dumps

    public static void init() {
        if (started) return;
        started = true;
        NeoForge.EVENT_BUS.addListener(ClientHangWatchdog::onClientTick);
        NeoForge.EVENT_BUS.addListener(ClientHangWatchdog::onServerTick);
        Thread t = new Thread(ClientHangWatchdog::watchLoop, "WYR-HangWatchdog");
        t.setDaemon(true);
        t.start();
        WYR.LOGGER.info("[WYR] Client hang watchdog started");
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        lastRenderTickMs = System.currentTimeMillis();
    }

    private static void onServerTick(ServerTickEvent.Post event) {
        lastServerTickMs = System.currentTimeMillis();
    }

    private static void watchLoop() {
        while (true) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ignored) {
            }
            long now = System.currentTimeMillis();
            long renderSilence = now - lastRenderTickMs;
            // Only flag server silence while an integrated server is actually running.
            boolean serverRunning = WYR.getServer() != null;
            long serverSilence = serverRunning ? now - lastServerTickMs : 0;

            // Detect "stuck on loading screen": the integrated server has a player
            // online but the client still has no level/player after 15s. Neither
            // thread is hung in this case - the client is waiting for a condition
            // that never becomes true (e.g. a packet that never arrives).
            boolean stuckLoading = false;
            try {
                Minecraft mc = Minecraft.getInstance();
                IntegratedServer server = mc.getSingleplayerServer();
                boolean serverHasPlayer = server != null && !server.getPlayerList().getPlayers().isEmpty();
                boolean clientReady = mc.level != null && mc.player != null;
                if (serverHasPlayer && !clientReady) {
                    if (stuckLoadingSinceMs == 0) stuckLoadingSinceMs = now;
                    stuckLoading = now - stuckLoadingSinceMs > STUCK_LOADING_MS;
                } else {
                    stuckLoadingSinceMs = 0;
                }
            } catch (Throwable ignored) {
            }

            if ((renderSilence > HANG_THRESHOLD_MS || serverSilence > HANG_THRESHOLD_MS || stuckLoading)
                    && now - lastDumpMs > DUMP_COOLDOWN_MS) {
                lastDumpMs = now;
                dumpThreads(renderSilence, serverSilence, stuckLoading);
            }
        }
    }

    private static void dumpThreads(long renderSilenceMs, long serverSilenceMs, boolean stuckLoading) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n===== [WYR] HANG DETECTED =====\n");
        sb.append("Render thread silent for ").append(renderSilenceMs).append(" ms\n");
        sb.append("Server thread silent for ").append(serverSilenceMs).append(" ms\n");
        sb.append("Stuck loading (server has player, client has no level): ").append(stuckLoading).append('\n');
        try {
            Minecraft mc = Minecraft.getInstance();
            sb.append("mc.level=").append(mc.level).append(" mc.player=").append(mc.player)
              .append(" mc.getConnection=").append(mc.getConnection()).append('\n');
            sb.append(describeConnection(mc));
        } catch (Throwable t) {
            sb.append("(could not query Minecraft state: ").append(t).append(")\n");
        }
        // Dump ALL threads - the blocker may be on Netty IO or a worker thread.
        for (Map.Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
            Thread t = entry.getKey();
            sb.append("----- Thread: ").append(t.getName())
              .append(" state=").append(t.getState()).append(" -----\n");
            for (StackTraceElement el : entry.getValue()) {
                sb.append("    at ").append(el).append('\n');
            }
        }
        sb.append("===== [WYR] END HANG DUMP =====");
        WYR.LOGGER.error(sb.toString());
    }

    /**
     * Reflectively inspects Minecraft.pendingConnection and its current
     * PacketListener so we can tell which handshake phase (handshake / login /
     * configuration / play) the client is stuck in when the world never loads.
     */
    private static String describeConnection(Minecraft mc) {
        StringBuilder sb = new StringBuilder();
        try {
            Object conn = findField(mc, "net.minecraft.network.Connection");
            if (conn == null) {
                sb.append("pendingConnection=null\n");
                return sb.toString();
            }
            sb.append("pendingConnection=").append(conn.getClass().getName()).append('\n');
            try {
                sb.append("  isConnected=").append(conn.getClass().getMethod("isConnected").invoke(conn))
                  .append(" isMemoryConnection=").append(conn.getClass().getMethod("isMemoryConnection").invoke(conn))
                  .append('\n');
            } catch (Throwable t) {
                sb.append("  (isConnected/isMemoryConnection failed: ").append(t).append(")\n");
            }
            // Use the public getter - field-by-type lookup can hit disconnectListener instead.
            try {
                Object listener = conn.getClass().getMethod("getPacketListener").invoke(conn);
                sb.append("  packetListener=").append(listener == null ? "null" : listener.getClass().getName()).append('\n');
            } catch (Throwable t) {
                sb.append("  (getPacketListener failed: ").append(t).append(")\n");
            }
            // The inbound protocol id tells us exactly which handshake phase the
            // client is in: LOGIN -> CONFIGURATION -> PLAY.
            try {
                Object proto = conn.getClass().getMethod("getInboundProtocol").invoke(conn);
                Object protoId = proto.getClass().getMethod("id").invoke(proto);
                sb.append("  inboundProtocol=").append(protoId).append('\n');
            } catch (Throwable t) {
                sb.append("  (getInboundProtocol failed: ").append(t).append(")\n");
            }
            // Packet counters tell us whether the channel is actually receiving.
            try {
                Object rx = conn.getClass().getMethod("getAverageReceivedPackets").invoke(conn);
                Object tx = conn.getClass().getMethod("getAverageSentPackets").invoke(conn);
                sb.append("  avgReceived=").append(rx).append(" avgSent=").append(tx).append('\n');
            } catch (Throwable t) {
                sb.append("  (packet counters failed: ").append(t).append(")\n");
            }
            // Inspect the netty channel's read state - if autoRead is off, inbound
            // packets (incl. ClientboundLoginPacket) are never delivered.
            try {
                Object channel = conn.getClass().getMethod("channel").invoke(conn);
                Object config = channel.getClass().getMethod("config").invoke(channel);
                sb.append("  channel.isOpen=").append(channel.getClass().getMethod("isOpen").invoke(channel))
                  .append(" isRegistered=").append(channel.getClass().getMethod("isRegistered").invoke(channel))
                  .append(" autoRead=").append(config.getClass().getMethod("isAutoRead").invoke(config))
                  .append('\n');
            } catch (Throwable t) {
                sb.append("  (channel inspect failed: ").append(t).append(")\n");
            }
        } catch (Throwable t) {
            sb.append("(describeConnection failed: ").append(t).append(")\n");
        }
        return sb.toString();
    }

    private static Object findField(Object target, String typeName) {
        Class<?> cls = target.getClass();
        while (cls != null) {
            for (java.lang.reflect.Field f : cls.getDeclaredFields()) {
                if (f.getType().getName().equals(typeName)) {
                    try {
                        f.setAccessible(true);
                        return f.get(target);
                    } catch (Throwable ignored) {
                        return null;
                    }
                }
            }
            cls = cls.getSuperclass();
        }
        return null;
    }
}
