package net.coba.wyr.mixin.client;

import net.coba.wyr.ChoiceEffects;
import net.coba.wyr.ClientEffectCache;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerMixin {

    /**
     * This Mixin injects code at the end ("TAIL") of the Player's main tick() method.
     * This ensures our climbing logic runs after most other physics calculations.
     */
    @Inject(method = "tick", at = @At("TAIL"))
    private void wyr_applyWallClimb(CallbackInfo ci) {
        Player player = (Player)(Object)this;

        if (ChoiceEffects.hasEffect(player, "wall_climb")) {

            if (player.horizontalCollision && !player.getAbilities().flying) {

                Vec3 motion = player.getDeltaMovement();

                if (motion.y < 0.2D) {
                    player.setDeltaMovement(motion.x, 0.2D, motion.z);

                    player.fallDistance = 0;
                }
            }
        }

        if (ChoiceEffects.hasEffect(player, "fast_climb_short") && player.onClimbable()) {
            Vec3 motion = player.getDeltaMovement();
            // Only boost upward movement when actively climbing (holding forward)
            if (motion.y > 0 && player.zza > 0) {
                player.setDeltaMovement(motion.x, Math.min(motion.y * 2.0, 0.3), motion.z);
            } else if (motion.y > 0) {
                // Limit upward velocity when not actively climbing to prevent launching
                player.setDeltaMovement(motion.x, Math.min(motion.y, 0.1), motion.z);
            }
        }
    }

    @Inject(method = "updateSwimming", at = @At("HEAD"), cancellable = true)
    private void wyr_allowLavaSwimming(CallbackInfo ci) {
        Player player = (Player)(Object)this;
        if (ClientEffectCache.hasEffect("lava_swim_water_damage") && player.isInLava()) {
            player.setSwimming(true);
            ci.cancel();
        }
    }

    @Inject(
            method = "tickRegeneration",
            at = @At("HEAD"),
            cancellable = true
    )
    private void wyr_preventNaturalRegen(CallbackInfo ci) {
        Player player = (Player)(Object)this;
        if (!player.level().isClientSide() && ChoiceEffects.hasEffect(player, "no_hunger_no_regen")) {
            ci.cancel();
        }
    }
}