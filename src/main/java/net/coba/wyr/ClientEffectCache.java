package net.coba.wyr;

// A simple client-side class to store the state of server-side effects.
public class ClientEffectCache {
    private static boolean hasStackedLogs = false;
    private static boolean hasSeeMobHp = false;
    private static int scrambleState = 0;
    private static boolean hasRandomSounds = false;
    private static boolean hasRandomShader = false;
    private static boolean hasFovChange = false;
    private static boolean hasLowRenderDistance = false;
    private static boolean isMuted = false;

    public static void setHasStackedLogs(boolean status) {
        hasStackedLogs = status;
    }

    public static boolean hasStackedLogs() {
        return hasStackedLogs;
    }

    public static void setHasSeeMobHp(boolean status) {
        hasSeeMobHp = status;
    }
    public static boolean hasSeeMobHp() {
        return hasSeeMobHp;
    }

    public static void setScrambleState(int state) {
        scrambleState = state;
    }
    public static int getScrambleState() {
        return scrambleState;
    }
    public static void setHasRandomSounds(boolean status) {
        hasRandomSounds = status;
    }
    public static boolean hasRandomSounds() {
        return hasRandomSounds;
    }
    public static void setHasRandomShader(boolean status) {
        hasRandomShader = status;
    }
    public static boolean hasRandomShader() {
        return hasRandomShader;
    }

    public static void setHasFovChange(boolean status) { hasFovChange = status; }
    public static boolean hasFovChange() { return hasFovChange; }
    public static void setHasLowRenderDistance(boolean status) { hasLowRenderDistance = status; }
    public static boolean hasLowRenderDistance() { return hasLowRenderDistance; }

    public static void setMuted(boolean status) {
        isMuted = status;
    }
    public static boolean isMuted() {
        return isMuted;
    }
}