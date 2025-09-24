// WyrSavedData.java
package net.coba.wyr;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.*;

public class WyrSavedData extends SavedData {
    private static final String DATA_NAME = "wyr_saved_data";

    // Data to be saved
    private final Set<String> shownChoicePairs = new HashSet<>();
    private final Map<UUID, List<String>> playerChoices = new HashMap<>();
    private final Map<UUID, Integer> playerPromptCounts = new HashMap<>();
    private final Map<UUID, Boolean> playerPromptPending = new HashMap<>();
    // --- NEW DATA FOR PERSISTENT TIMER ---
    private final Map<UUID, Integer> playerPromptIntervals = new HashMap<>();
    private final Map<UUID, Long> playerNextPromptTimes = new HashMap<>();
    // ---
    private final Map<UUID, Integer> playerThirstLevels = new HashMap<>();
    private final Map<UUID, Float> playerThirstExhaustion = new HashMap<>();


    // NBT Keys for saving/loading
    private static final String TAG_SHOWN_PAIRS = "ShownChoicePairs";
    private static final String TAG_PLAYER_DATA = "PlayerData";
    private static final String TAG_PLAYER_CHOICES = "PlayerChoices";
    private static final String TAG_PLAYER_PROMPT_COUNT = "PlayerPromptCount";
    private static final String TAG_PLAYER_PROMPT_PENDING = "PlayerPromptPending";
    // --- NEW NBT KEYS ---
    private static final String TAG_PLAYER_PROMPT_INTERVAL = "PlayerPromptInterval";
    private static final String TAG_PLAYER_NEXT_PROMPT_TIME = "PlayerNextPromptTime";
    // ---
    private static final String TAG_PLAYER_THIRST_LEVEL = "PlayerThirstLevel";
    private static final String TAG_PLAYER_THIRST_EXHAUSTION = "PlayerThirstExhaustion";


    public WyrSavedData() {}

    public static WyrSavedData get(ServerLevel world) {
        DimensionDataStorage storage = world.getDataStorage();
        return storage.computeIfAbsent(new Factory<>(WyrSavedData::new, WyrSavedData::load, null), DATA_NAME);
    }

    public static WyrSavedData load(CompoundTag nbt, HolderLookup.Provider provider) {
        WyrSavedData data = new WyrSavedData();

        ListTag shownPairsTag = nbt.getList(TAG_SHOWN_PAIRS, 8);
        for (int i = 0; i < shownPairsTag.size(); i++) {
            data.shownChoicePairs.add(shownPairsTag.getString(i));
        }

        if (nbt.contains(TAG_PLAYER_DATA, 10)) {
            CompoundTag allPlayerData = nbt.getCompound(TAG_PLAYER_DATA);
            for (String uuidString : allPlayerData.getAllKeys()) {
                UUID uuid = UUID.fromString(uuidString);
                CompoundTag playerTag = allPlayerData.getCompound(uuidString);

                if (playerTag.contains(TAG_PLAYER_CHOICES, 9)) {
                    List<String> choices = new ArrayList<>();
                    ListTag choicesTag = playerTag.getList(TAG_PLAYER_CHOICES, 8);
                    for (int i = 0; i < choicesTag.size(); i++) choices.add(choicesTag.getString(i));
                    data.playerChoices.put(uuid, choices);
                }
                if (playerTag.contains(TAG_PLAYER_PROMPT_COUNT, 3)) data.playerPromptCounts.put(uuid, playerTag.getInt(TAG_PLAYER_PROMPT_COUNT));
                if (playerTag.contains(TAG_PLAYER_PROMPT_PENDING, 1)) data.playerPromptPending.put(uuid, playerTag.getBoolean(TAG_PLAYER_PROMPT_PENDING));
                if (playerTag.contains(TAG_PLAYER_PROMPT_INTERVAL, 3)) data.playerPromptIntervals.put(uuid, playerTag.getInt(TAG_PLAYER_PROMPT_INTERVAL));
                if (playerTag.contains(TAG_PLAYER_NEXT_PROMPT_TIME, 4)) data.playerNextPromptTimes.put(uuid, playerTag.getLong(TAG_PLAYER_NEXT_PROMPT_TIME));
                if (playerTag.contains(TAG_PLAYER_THIRST_LEVEL, 3)) data.playerThirstLevels.put(uuid, playerTag.getInt(TAG_PLAYER_THIRST_LEVEL));
                if (playerTag.contains(TAG_PLAYER_THIRST_EXHAUSTION, 5)) data.playerThirstExhaustion.put(uuid, playerTag.getFloat(TAG_PLAYER_THIRST_EXHAUSTION));
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag nbt, HolderLookup.Provider provider) {
        ListTag shownPairsTag = new ListTag();
        shownChoicePairs.forEach(pair -> shownPairsTag.add(StringTag.valueOf(pair)));
        nbt.put(TAG_SHOWN_PAIRS, shownPairsTag);

        CompoundTag allPlayerData = new CompoundTag();
        Set<UUID> allPlayerUUIDs = new HashSet<>();
        allPlayerUUIDs.addAll(playerChoices.keySet());
        allPlayerUUIDs.addAll(playerPromptCounts.keySet());
        allPlayerUUIDs.addAll(playerPromptPending.keySet());
        allPlayerUUIDs.addAll(playerPromptIntervals.keySet());
        allPlayerUUIDs.addAll(playerNextPromptTimes.keySet());
        allPlayerUUIDs.addAll(playerThirstLevels.keySet());

        for (UUID uuid : allPlayerUUIDs) {
            CompoundTag playerTag = new CompoundTag();

            List<String> choices = playerChoices.get(uuid);
            if (choices != null && !choices.isEmpty()) {
                ListTag choicesTag = new ListTag();
                choices.forEach(id -> choicesTag.add(StringTag.valueOf(id)));
                playerTag.put(TAG_PLAYER_CHOICES, choicesTag);
            }

            if (playerPromptCounts.containsKey(uuid)) playerTag.putInt(TAG_PLAYER_PROMPT_COUNT, playerPromptCounts.get(uuid));
            if (playerPromptPending.containsKey(uuid)) playerTag.putBoolean(TAG_PLAYER_PROMPT_PENDING, playerPromptPending.get(uuid));
            if (playerPromptIntervals.containsKey(uuid)) playerTag.putInt(TAG_PLAYER_PROMPT_INTERVAL, playerPromptIntervals.get(uuid));
            if (playerNextPromptTimes.containsKey(uuid)) playerTag.putLong(TAG_PLAYER_NEXT_PROMPT_TIME, playerNextPromptTimes.get(uuid));
            if (playerThirstLevels.containsKey(uuid)) playerTag.putInt(TAG_PLAYER_THIRST_LEVEL, playerThirstLevels.get(uuid));
            if (playerThirstExhaustion.containsKey(uuid)) playerTag.putFloat(TAG_PLAYER_THIRST_EXHAUSTION, playerThirstExhaustion.get(uuid));

            allPlayerData.put(uuid.toString(), playerTag);
        }
        nbt.put(TAG_PLAYER_DATA, allPlayerData);

        return nbt;
    }

    // --- Accessor and Mutator Methods ---
    public int getPlayerPromptInterval(UUID uuid) {
        return playerPromptIntervals.getOrDefault(uuid, 0);
    }
    public void setPlayerPromptInterval(UUID uuid, int interval) {
        if (interval > 0) playerPromptIntervals.put(uuid, interval);
        else playerPromptIntervals.remove(uuid);
        setDirty();
    }
    public long getNextPromptTime(UUID uuid) {
        return playerNextPromptTimes.getOrDefault(uuid, Long.MAX_VALUE);
    }
    public void setNextPromptTime(UUID uuid, long time) {
        if (time > 0) playerNextPromptTimes.put(uuid, time);
        else playerNextPromptTimes.remove(uuid);
        setDirty();
    }

    public boolean isPromptPending(UUID uuid) { return playerPromptPending.getOrDefault(uuid, false); }
    public void setPromptPending(UUID uuid, boolean pending) {
        if (pending) playerPromptPending.put(uuid, true);
        else playerPromptPending.remove(uuid);
        setDirty();
    }

    public int getPlayerThirst(UUID uuid) { return playerThirstLevels.getOrDefault(uuid, 20); }
    public void setPlayerThirst(UUID uuid, int thirst) { playerThirstLevels.put(uuid, thirst); setDirty(); }
    public float getPlayerThirstExhaustion(UUID uuid) { return playerThirstExhaustion.getOrDefault(uuid, 0.0F); }
    public void setPlayerThirstExhaustion(UUID uuid, float exhaustion) { playerThirstExhaustion.put(uuid, exhaustion); setDirty(); }
    public Set<String> getShownChoicePairs() { return shownChoicePairs; }
    public List<String> getPlayerChoices(UUID playerUUID) { return playerChoices.getOrDefault(playerUUID, Collections.emptyList()); }
    public void addPlayerChoice(UUID playerUUID, String choiceId) { playerChoices.computeIfAbsent(playerUUID, k -> new ArrayList<>()).add(choiceId); setDirty(); }
    public void removePlayerChoice(UUID playerUUID, String choiceId) { playerChoices.computeIfPresent(playerUUID, (k, v) -> { v.remove(choiceId); return v; }); setDirty(); }
    public int getPlayerPromptCount(UUID playerUUID) { return playerPromptCounts.getOrDefault(playerUUID, 0); }
    public void incrementPlayerPromptCount(UUID playerUUID) { playerPromptCounts.put(playerUUID, getPlayerPromptCount(playerUUID) + 1); setDirty(); }
    public void resetShownPairs() { shownChoicePairs.clear(); setDirty(); }
    public void clearPlayerChoices(UUID playerUUID) {
        playerChoices.remove(playerUUID);
        playerPromptCounts.remove(playerUUID);
        playerPromptPending.remove(playerUUID);
        playerPromptIntervals.remove(playerUUID);
        playerNextPromptTimes.remove(playerUUID);
        playerThirstLevels.remove(playerUUID);
        playerThirstExhaustion.remove(playerUUID);
        setDirty();
    }
}