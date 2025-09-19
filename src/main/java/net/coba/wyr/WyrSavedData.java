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
    // NEW DATA FOR THIRST
    private final Map<UUID, Integer> playerThirstLevels = new HashMap<>();
    private final Map<UUID, Float> playerThirstExhaustion = new HashMap<>();


    // NBT Keys for saving/loading
    private static final String TAG_SHOWN_PAIRS = "ShownChoicePairs";
    private static final String TAG_PLAYER_DATA = "PlayerData";
    private static final String TAG_PLAYER_CHOICES = "PlayerChoices";
    private static final String TAG_PLAYER_PROMPT_COUNT = "PlayerPromptCount";
    // NEW KEYS
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
                    for (int i = 0; i < choicesTag.size(); i++) {
                        choices.add(choicesTag.getString(i));
                    }
                    data.playerChoices.put(uuid, choices);
                }

                if (playerTag.contains(TAG_PLAYER_PROMPT_COUNT, 3)) {
                    data.playerPromptCounts.put(uuid, playerTag.getInt(TAG_PLAYER_PROMPT_COUNT));
                }

                if (playerTag.contains(TAG_PLAYER_THIRST_LEVEL, 3)) {
                    data.playerThirstLevels.put(uuid, playerTag.getInt(TAG_PLAYER_THIRST_LEVEL));
                }

                if (playerTag.contains(TAG_PLAYER_THIRST_EXHAUSTION, 5)) { // 5 = Float NBT
                    data.playerThirstExhaustion.put(uuid, playerTag.getFloat(TAG_PLAYER_THIRST_EXHAUSTION));
                }
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
        Set<UUID> allPlayerUUIDs = new HashSet<>(playerChoices.keySet());
        allPlayerUUIDs.addAll(playerPromptCounts.keySet());
        allPlayerUUIDs.addAll(playerThirstLevels.keySet());


        for (UUID uuid : allPlayerUUIDs) {
            CompoundTag playerTag = new CompoundTag();

            List<String> choices = playerChoices.get(uuid);
            if (choices != null && !choices.isEmpty()) {
                ListTag choicesTag = new ListTag();
                choices.forEach(id -> choicesTag.add(StringTag.valueOf(id)));
                playerTag.put(TAG_PLAYER_CHOICES, choicesTag);
            }

            Integer promptCount = playerPromptCounts.get(uuid);
            if (promptCount != null) {
                playerTag.putInt(TAG_PLAYER_PROMPT_COUNT, promptCount);
            }

            Integer thirstLevel = playerThirstLevels.get(uuid);
            if (thirstLevel != null) {
                playerTag.putInt(TAG_PLAYER_THIRST_LEVEL, thirstLevel);
            }

            Float thirstExhaustion = playerThirstExhaustion.get(uuid);
            if (thirstExhaustion != null) {
                playerTag.putFloat(TAG_PLAYER_THIRST_EXHAUSTION, thirstExhaustion);
            }

            allPlayerData.put(uuid.toString(), playerTag);
        }
        nbt.put(TAG_PLAYER_DATA, allPlayerData);

        return nbt;
    }

    // --- Accessor and Mutator Methods ---
    // ... (existing accessors) ...

    public int getPlayerThirst(UUID uuid) {
        return playerThirstLevels.getOrDefault(uuid, 20);
    }

    public void setPlayerThirst(UUID uuid, int thirst) {
        playerThirstLevels.put(uuid, thirst);
        setDirty();
    }

    public float getPlayerThirstExhaustion(UUID uuid) {
        return playerThirstExhaustion.getOrDefault(uuid, 0.0F);
    }

    public void setPlayerThirstExhaustion(UUID uuid, float exhaustion) {
        playerThirstExhaustion.put(uuid, exhaustion);
        setDirty();
    }

    public Set<String> getShownChoicePairs() {
        return shownChoicePairs;
    }

    public List<String> getPlayerChoices(UUID playerUUID) {
        return playerChoices.getOrDefault(playerUUID, Collections.emptyList());
    }

    public void addPlayerChoice(UUID playerUUID, String choiceId) {
        playerChoices.computeIfAbsent(playerUUID, k -> new ArrayList<>()).add(choiceId);
        setDirty();
    }

    // --- NEW METHOD ---
    public void removePlayerChoice(UUID playerUUID, String choiceId) {
        playerChoices.computeIfPresent(playerUUID, (k, v) -> {
            v.remove(choiceId);
            return v;
        });
        setDirty();
    }

    public void clearPlayerChoices(UUID playerUUID) {
        playerChoices.remove(playerUUID);
        playerPromptCounts.remove(playerUUID);
        playerThirstLevels.remove(playerUUID);
        playerThirstExhaustion.remove(playerUUID);
        setDirty();
    }

    public int getPlayerPromptCount(UUID playerUUID) {
        return playerPromptCounts.getOrDefault(playerUUID, 0);
    }

    public void incrementPlayerPromptCount(UUID playerUUID) {
        playerPromptCounts.put(playerUUID, getPlayerPromptCount(playerUUID) + 1);
        setDirty();
    }

    public void resetShownPairs() {
        shownChoicePairs.clear();
        setDirty();
    }
}