package net.coba.wyr;

// A simple client-side class to store the state of server-side effects.
public class ClientEffectCache {
    private static boolean hasStackedLogs = false;

    public static void setHasStackedLogs(boolean status) {
        hasStackedLogs = status;
    }

    public static boolean hasStackedLogs() {
        return hasStackedLogs;
    }
}