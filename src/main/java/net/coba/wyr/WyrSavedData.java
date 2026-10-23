package net.coba.wyr;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;

import java.util.*;

public class WyrSavedData extends SavedData {
    private static final String DATA_NAME = "wyr_saved_data";
    private int globalPromptInterval = 0;

    // Data to be saved
    private final Set<String> shownChoicePairs = new HashSet<>();
    private final Map<UUID, List<String>> playerChoices = new HashMap<>();
    private final Map<UUID, Integer> playerPromptCounts = new HashMap<>();
    private final Map<UUID, Boolean> playerPromptPending = new HashMap<>();
    private final Map<UUID, Integer> playerPromptIntervals = new HashMap<>();
    private final Map<UUID, Long> playerNextPromptTimes = new HashMap<>();
    private final Map<UUID, Integer> playerScrambleStates = new HashMap<>();
    private final Map<UUID, Integer> playerThirstLevels = new HashMap<>();
    private final Map<UUID, Float> playerThirstExhaustion = new HashMap<>();
    private final Map<UUID, Long> playerCrosshairSeeds = new HashMap<>();
    private final Map<UUID, String> playerSpawnBiomes = new HashMap<>();
    private final Map<UUID, BlockPos> playerSpawnPositions = new HashMap<>();
    private final Map<UUID, List<String>> playerPendingChoices = new HashMap<>();


    // NBT Keys for saving/loading
    private static final String TAG_SHOWN_PAIRS = "ShownChoicePairs";
    private static final String TAG_PLAYER_DATA = "PlayerData";
    private static final String TAG_PLAYER_CHOICES = "PlayerChoices";
    private static final String TAG_PLAYER_PROMPT_COUNT = "PlayerPromptCount";
    private static final String TAG_PLAYER_PROMPT_PENDING = "PlayerPromptPending";
    private static final String TAG_PLAYER_PROMPT_INTERVAL = "PlayerPromptInterval";
    private static final String TAG_PLAYER_NEXT_PROMPT_TIME = "PlayerNextPromptTime";
    private static final String TAG_PLAYER_SCRAMBLE_STATE = "PlayerScrambleState";
    private static final String TAG_PLAYER_THIRST_LEVEL = "PlayerThirstLevel";
    private static final String TAG_PLAYER_THIRST_EXHAUSTION = "PlayerThirstExhaustion";
    private static final String TAG_GLOBAL_PROMPT_INTERVAL = "GlobalPromptInterval";
    private static final String TAG_PLAYER_CROSSHAIR_SEED = "PlayerCrosshairSeed";
    private static final String TAG_PLAYER_SPAWN_BIOME = "PlayerSpawnBiome";
    private static final String TAG_PLAYER_SPAWN_POS = "PlayerSpawnPos";


    public WyrSavedData() {}

    public static final Codec<WyrSavedData> CODEC = CompoundTag.CODEC.xmap(
        nbt -> WyrSavedData.load(nbt, null),
        data -> data.save(new CompoundTag(), null)
    );

    public static final SavedDataType<WyrSavedData> TYPE = new SavedDataType<>(
        Identifier.fromNamespaceAndPath(WYR.MODID, DATA_NAME),
        WyrSavedData::new,
        CODEC
    );

    public static WyrSavedData get(ServerLevel world) {
        SavedDataStorage storage = world.getServer().overworld().getDataStorage();
        return storage.computeIfAbsent(TYPE);
    }

    public static WyrSavedData load(CompoundTag nbt, HolderLookup.Provider provider) {
        WyrSavedData data = new WyrSavedData();
        if (nbt.contains(TAG_GLOBAL_PROMPT_INTERVAL)) data.globalPromptInterval = nbt.getInt(TAG_GLOBAL_PROMPT_INTERVAL).orElse(0);

        ListTag shownPairsTag = nbt.getList(TAG_SHOWN_PAIRS).orElse(new ListTag()); // 8 = String NBT
        for (int i = 0; i < shownPairsTag.size(); i++) {
            data.shownChoicePairs.add(shownPairsTag.getString(i).orElse(""));
        }

        if (nbt.contains(TAG_PLAYER_DATA)) { // 10 = Compound NBT
            CompoundTag allPlayerData = nbt.getCompound(TAG_PLAYER_DATA).orElse(new CompoundTag());
            for (String uuidString : allPlayerData.keySet()) {
                UUID uuid = UUID.fromString(uuidString);
                CompoundTag playerTag = allPlayerData.getCompound(uuidString).orElse(new CompoundTag());

                if (playerTag.contains(TAG_PLAYER_CHOICES)) { // 9 = List NBT
                    List<String> choices = new ArrayList<>();
                    ListTag choicesTag = playerTag.getList(TAG_PLAYER_CHOICES).orElse(new ListTag());
                    for (int i = 0; i < choicesTag.size(); i++) choices.add(choicesTag.getString(i).orElse(""));
                    data.playerChoices.put(uuid, choices);
                }
                if (playerTag.contains(TAG_PLAYER_PROMPT_COUNT)) data.playerPromptCounts.put(uuid, playerTag.getInt(TAG_PLAYER_PROMPT_COUNT).orElse(0));
                if (playerTag.contains(TAG_PLAYER_PROMPT_PENDING)) data.playerPromptPending.put(uuid, playerTag.getBoolean(TAG_PLAYER_PROMPT_PENDING).orElse(false));
                if (playerTag.contains(TAG_PLAYER_PROMPT_INTERVAL)) data.playerPromptIntervals.put(uuid, playerTag.getInt(TAG_PLAYER_PROMPT_INTERVAL).orElse(0));
                if (playerTag.contains(TAG_PLAYER_NEXT_PROMPT_TIME)) data.playerNextPromptTimes.put(uuid, playerTag.getLong(TAG_PLAYER_NEXT_PROMPT_TIME).orElse(0L));
                if (playerTag.contains(TAG_PLAYER_SCRAMBLE_STATE)) data.playerScrambleStates.put(uuid, playerTag.getInt(TAG_PLAYER_SCRAMBLE_STATE).orElse(0));
                if (playerTag.contains(TAG_PLAYER_THIRST_LEVEL)) data.playerThirstLevels.put(uuid, playerTag.getInt(TAG_PLAYER_THIRST_LEVEL).orElse(0));
                if (playerTag.contains(TAG_PLAYER_THIRST_EXHAUSTION)) data.playerThirstExhaustion.put(uuid, playerTag.getFloat(TAG_PLAYER_THIRST_EXHAUSTION).orElse(0.0F));
                if (playerTag.contains(TAG_PLAYER_CROSSHAIR_SEED)) data.playerCrosshairSeeds.put(uuid, playerTag.getLong(TAG_PLAYER_CROSSHAIR_SEED).orElse(0L));
                if (playerTag.contains(TAG_PLAYER_SPAWN_BIOME)) data.playerSpawnBiomes.put(uuid, playerTag.getString(TAG_PLAYER_SPAWN_BIOME).orElse(""));
                if (playerTag.contains(TAG_PLAYER_SPAWN_POS)) {
                    long posLong = playerTag.getLong(TAG_PLAYER_SPAWN_POS).orElse(0L);
                    if (posLong != 0L) {
                        data.playerSpawnPositions.put(uuid, BlockPos.of(posLong));
                    }
                }
            }
        }
        return data;
    }

    public CompoundTag save(CompoundTag nbt, HolderLookup.Provider provider) {
        ListTag shownPairsTag = new ListTag();
        shownChoicePairs.forEach(pair -> shownPairsTag.add(StringTag.valueOf(pair)));
        nbt.put(TAG_SHOWN_PAIRS, shownPairsTag);
        nbt.putInt(TAG_GLOBAL_PROMPT_INTERVAL, this.globalPromptInterval);

        CompoundTag allPlayerData = new CompoundTag();
        Set<UUID> allPlayerUUIDs = new HashSet<>();
        allPlayerUUIDs.addAll(playerChoices.keySet());
        allPlayerUUIDs.addAll(playerPromptCounts.keySet());
        allPlayerUUIDs.addAll(playerPromptPending.keySet());
        allPlayerUUIDs.addAll(playerPromptIntervals.keySet());
        allPlayerUUIDs.addAll(playerNextPromptTimes.keySet());
        allPlayerUUIDs.addAll(playerScrambleStates.keySet());
        allPlayerUUIDs.addAll(playerThirstLevels.keySet());
        allPlayerUUIDs.addAll(playerThirstExhaustion.keySet());
        allPlayerUUIDs.addAll(playerCrosshairSeeds.keySet());
        allPlayerUUIDs.addAll(playerSpawnBiomes.keySet());
        allPlayerUUIDs.addAll(playerSpawnPositions.keySet());

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
            if (playerScrambleStates.containsKey(uuid)) playerTag.putInt(TAG_PLAYER_SCRAMBLE_STATE, playerScrambleStates.get(uuid));
            if (playerThirstLevels.containsKey(uuid)) playerTag.putInt(TAG_PLAYER_THIRST_LEVEL, playerThirstLevels.get(uuid));
            if (playerThirstExhaustion.containsKey(uuid)) playerTag.putFloat(TAG_PLAYER_THIRST_EXHAUSTION, playerThirstExhaustion.get(uuid));
            if (playerCrosshairSeeds.containsKey(uuid)) playerTag.putLong(TAG_PLAYER_CROSSHAIR_SEED, playerCrosshairSeeds.get(uuid));
            if (playerSpawnBiomes.containsKey(uuid)) playerTag.putString(TAG_PLAYER_SPAWN_BIOME, playerSpawnBiomes.get(uuid));
            if (playerSpawnPositions.containsKey(uuid)) playerTag.putLong(TAG_PLAYER_SPAWN_POS, playerSpawnPositions.get(uuid).asLong());

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

    public int getPlayerScrambleState(UUID uuid) {
        return playerScrambleStates.getOrDefault(uuid, 0);
    }
    public void setPlayerScrambleState(UUID uuid, int state) {
        if (state > 0) playerScrambleStates.put(uuid, state);
        else playerScrambleStates.remove(uuid);
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
        playerScrambleStates.remove(playerUUID);
        playerThirstLevels.remove(playerUUID);
        playerThirstExhaustion.remove(playerUUID);
        playerSpawnBiomes.remove(playerUUID);
        playerSpawnPositions.remove(playerUUID);
        setDirty();
    }

    public String getPlayerSpawnBiome(UUID uuid) {
        return playerSpawnBiomes.getOrDefault(uuid, "");
    }
    public void setPlayerSpawnBiome(UUID uuid, String biome) {
        if (biome != null && !biome.isEmpty()) {
            playerSpawnBiomes.put(uuid, biome);
        } else {
            playerSpawnBiomes.remove(uuid);
        }
        setDirty();
    }
    public BlockPos getPlayerSpawnPosition(UUID uuid) {
        return playerSpawnPositions.get(uuid);
    }
    public void setPlayerSpawnPosition(UUID uuid, BlockPos pos) {
        if (pos != null) {
            playerSpawnPositions.put(uuid, pos);
        } else {
            playerSpawnPositions.remove(uuid);
        }
        setDirty();
    }

    public int getGlobalPromptInterval() {
        return this.globalPromptInterval;
    }

    public void setGlobalPromptInterval(int interval) {
        this.globalPromptInterval = interval;
        setDirty();
    }

    public long getPlayerCrosshairSeed(UUID uuid) {
        return playerCrosshairSeeds.getOrDefault(uuid, 0L);
    }
    public void setPlayerCrosshairSeed(UUID uuid, long seed) {
        playerCrosshairSeeds.put(uuid, seed);
        setDirty();
    }

    public void setPendingChoices(UUID uuid, List<Choice> choices) {
        if (choices != null && !choices.isEmpty()) {
            List<String> choiceIds = choices.stream().map(Choice::getId).toList();
            playerPendingChoices.put(uuid, choiceIds);
        } else {
            playerPendingChoices.remove(uuid);
        }
        setDirty();
    }

    public List<String> getPendingChoices(UUID uuid) {
        return playerPendingChoices.getOrDefault(uuid, Collections.emptyList());
    }
}