package net.coba.wyr;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent.Applicable.Result;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ChoiceEffects {

    private static final Map<UUID, String> activeEffects = new HashMap<>();
    private static final Map<UUID, Integer> playerTickCounters = new HashMap<>();
    private static final Map<UUID, Long> teleportCooldowns = new HashMap<>();

    private static final ResourceLocation MAX_HEALTH_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(WYR.MODID, "max_health_modifier");

    public static void applyEffect(Player player, String effectId) {
        clearEffect(player);
        activeEffects.put(player.getUUID(), effectId);
        playerTickCounters.remove(player.getUUID());

        if (player instanceof ServerPlayer) {
            AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
            if (maxHealth != null) {
                maxHealth.removeModifier(MAX_HEALTH_MODIFIER_ID);
                if ("insta_heal".equals(effectId)) {
                    maxHealth.addPermanentModifier(new AttributeModifier(MAX_HEALTH_MODIFIER_ID, -14.0, AttributeModifier.Operation.ADD_VALUE));
                } else if ("extra_hearts".equals(effectId)) {
                    maxHealth.addPermanentModifier(new AttributeModifier(MAX_HEALTH_MODIFIER_ID, 20.0, AttributeModifier.Operation.ADD_VALUE));
                }
            }
            player.getAbilities().mayfly = "creative_flight".equals(effectId);
            player.onUpdateAbilities();
        }
    }

    public static void clearEffect(Player player) {
        String oldEffectId = activeEffects.remove(player.getUUID());
        if (oldEffectId == null) return;

        if (player instanceof ServerPlayer) {
            AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
            if (maxHealth != null) {
                maxHealth.removeModifier(MAX_HEALTH_MODIFIER_ID);
            }
            if (player.getHealth() > player.getMaxHealth()) {
                player.setHealth(player.getMaxHealth());
            }

            if (!player.isCreative()) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }
        }
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        clearEffect(event.getEntity());
    }

    @SubscribeEvent
    public void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        activeEffects.remove(event.getEntity().getUUID());
        playerTickCounters.remove(event.getEntity().getUUID());
        teleportCooldowns.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        String effectId = activeEffects.get(player.getUUID());
        if (effectId == null) return;

        int ticks = playerTickCounters.getOrDefault(player.getUUID(), 0);

        switch (effectId) {
            case "creative_flight":
                if (player.getAbilities().flying) {
                    if (ticks >= 200) {
                        List<ItemStack> allItems = new ArrayList<>();
                        allItems.addAll(player.getInventory().items);
                        allItems.addAll(player.getInventory().armor);
                        allItems.addAll(player.getInventory().offhand);
                        allItems.removeIf(ItemStack::isEmpty);

                        if (!allItems.isEmpty()) {
                            int randomIndex = player.getRandom().nextInt(allItems.size());
                            allItems.get(randomIndex).setCount(0);
                        }
                        playerTickCounters.put(player.getUUID(), 0);
                    } else {
                        playerTickCounters.put(player.getUUID(), ticks + 1);
                    }
                } else {
                    playerTickCounters.put(player.getUUID(), 0);
                }
                break;
            case "no_hunger":
                if (player.getFoodData().needsFood()) {
                    player.getFoodData().setFoodLevel(20);
                    player.getFoodData().setSaturation(5.0F);
                }
                if (ticks % 20 == 0 && player.hasEffect(MobEffects.REGENERATION)) {
                    player.heal(1.0F);
                }
                break;
            case "speed_iv":
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 2, 3, true, false));
                if (player.getFoodData().getFoodLevel() > 0) {
                    player.getFoodData().addExhaustion(0.015f * 3);
                }
                break;
            case "water_breathing":
                player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 2, 0, true, false));
                ItemStack helmet = player.getInventory().getArmor(3);
                if (!helmet.isEmpty()) {
                    player.getInventory().add(helmet);
                    player.getInventory().armor.set(3, ItemStack.EMPTY);
                }
                break;
            case "haste_ii":
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 2, 1, true, false));
                break;
            case "high_jump":
                player.addEffect(new MobEffectInstance(MobEffects.JUMP, 2, 3, true, false));
                break;
            case "mob_invisibility":
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 2, 0, true, false));
                break;
            case "strength_ii":
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 2, 1, true, false));
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    if (player.getInventory().getItem(i).getItem() instanceof ShieldItem) {
                        player.getInventory().removeItem(player.getInventory().getItem(i));
                    }
                }
                break;
            case "night_vision":
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 220, 0, true, false));
                if (ticks % 20 == 0) {
                    List<EnderMan> endermen = player.level().getEntitiesOfClass(EnderMan.class, player.getBoundingBox().inflate(32));
                    for (EnderMan enderman : endermen) {
                        if (enderman.getTarget() == null) {
                            enderman.setTarget(player);
                        }
                    }
                }
                break;
            case "extra_hearts":
                ItemStack chestplate = player.getInventory().getArmor(2);
                if (!chestplate.isEmpty()) {
                    player.getInventory().add(chestplate);
                    player.getInventory().armor.set(2, ItemStack.EMPTY);
                }
                break;
            case "water_walk":
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 2, 0, true, false));
                if (!player.level().isClientSide && !player.isCrouching()) {
                    BlockPos playerPos = player.blockPosition();
                    for (BlockPos pos : BlockPos.betweenClosed(playerPos.offset(-2, -1, -2), playerPos.offset(2, -1, 2))) {
                        if (!player.level().getFluidState(pos).isEmpty() && player.level().isEmptyBlock(pos.above())) {
                            player.level().setBlockAndUpdate(pos, Blocks.FROSTED_ICE.defaultBlockState());
                        }
                    }
                }
                break;
            case "dolphin_grace":
                if (player.isInWater()) {
                    player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 2, 0, true, false));
                } else if (!player.isCreative() && !player.isSpectator()){
                    if (ticks % 20 == 0) {
                        player.hurt(player.damageSources().drown(), 2.0F);
                    }
                }
                break;
            case "wall_climb":
                if(player.horizontalCollision && player.isCrouching()) {
                    Vec3 motion = player.getDeltaMovement();
                    player.setDeltaMovement(motion.x, 0.2D, motion.z);
                }
                break;
            case "triple_damage":
                ItemStack boots = player.getInventory().getArmor(0);
                if (!boots.isEmpty()) {
                    player.getInventory().add(boots);
                    player.getInventory().armor.set(0, ItemStack.EMPTY);
                }
                break;
            case "resistance_ii":
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 2, 1, true, false));
                if (player.isSprinting()) {
                    player.setSprinting(false);
                }
                break;
        }

        playerTickCounters.put(player.getUUID(), ticks + 1);
    }

    @SubscribeEvent
    public void onFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player) {
            String effectId = activeEffects.get(player.getUUID());
            if ("creative_flight".equals(effectId)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onHeal(LivingHealEvent event) {
        if (event.getEntity() instanceof Player player) {
            String effectId = activeEffects.get(player.getUUID());
            if ("insta_heal".equals(effectId)) {
                event.setAmount(player.getMaxHealth());
            } else if ("no_hunger".equals(effectId)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onLivingHurt(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            String effectId = activeEffects.get(player.getUUID());
            if (effectId == null) return;

            switch (effectId) {
                case "fire_immunity":
                    if (event.getSource().is(DamageTypeTags.IS_FIRE)) {
                        event.setCanceled(true);
                    } else if (event.getSource().getDirectEntity() instanceof LivingEntity) {
                        event.setAmount(event.getAmount() * 2);
                    }
                    break;
                case "wall_climb":
                    if (event.getSource().is(DamageTypeTags.IS_FALL)) {
                        event.setAmount(event.getAmount() * 2);
                    }
                    break;
            }
        }
        if(event.getSource().getEntity() instanceof Player player) {
            String effectId = activeEffects.get(player.getUUID());
            if ("triple_damage".equals(effectId)) {
                if (player.getRandom().nextInt(4) == 0) {
                    event.setAmount(event.getAmount() * 3);
                }
            }
        }
    }

    private void applyHasteDurability(Player player, ItemStack item) {
        String effectId = activeEffects.get(player.getUUID());
        if ("haste_ii".equals(effectId) && !player.isCreative() && player instanceof ServerPlayer serverPlayer) {
            if (item.isDamageableItem()) {
                item.hurtAndBreak(1, serverPlayer, EquipmentSlot.MAINHAND);
            }
        }
    }

    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        String effectId = activeEffects.get(player.getUUID());
        if ("mob_invisibility".equals(effectId)) {
            event.setCanceled(true);
        }
        applyHasteDurability(player, player.getMainHandItem());
    }

    @SubscribeEvent
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        applyHasteDurability(event.getEntity(), event.getItemStack());
    }

    @SubscribeEvent
    public void onSetTarget(LivingChangeTargetEvent event) {
        if (event.getTargetType() instanceof Player player) {
            String effectId = activeEffects.get(player.getUUID());
            if ("mob_invisibility".equals(effectId)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        String effectId = activeEffects.get(player.getUUID());
        if ("strength_ii".equals(effectId) && event.getItemStack().getItem() instanceof ShieldItem) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        String effectId = activeEffects.get(player.getUUID());
        if (effectId == null) return;

        long currentTime = System.currentTimeMillis();
        if ("ender_teleport".equals(effectId)) {
            if (currentTime > teleportCooldowns.getOrDefault(player.getUUID(), 0L)) {
                BlockHitResult rayTrace = player.level().clip(new ClipContext(player.getEyePosition(1.0f), player.getEyePosition(1.0f).add(player.getLookAngle().scale(15)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
                if (rayTrace.getType() == HitResult.Type.BLOCK) {
                    BlockPos pos = rayTrace.getBlockPos().relative(rayTrace.getDirection());
                    player.teleportTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                    player.hurt(player.damageSources().magic(), 4.0F);
                    teleportCooldowns.put(player.getUUID(), currentTime + 5000);
                }
                event.setCanceled(true);
            }
        } else if ("phasing".equals(effectId) && player.level().getBlockState(event.getPos()).getBlock() instanceof DoorBlock) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPotionEffect(MobEffectEvent.Applicable event) {
        if (event.getEntity() instanceof Player player) {
            String effectId = activeEffects.get(player.getUUID());
            if ("potion_immunity".equals(effectId)) {
                event.setResult(Result.DO_NOT_APPLY);
            }
        }
    }
}