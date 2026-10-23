package net.coba.wyr.mixin.client;

import net.coba.wyr.ClientEffectCache;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Shows a heart + numeric HP bar above every living mob while the
 * see_mob_hp_double_health effect is active. LivingEntityRenderer overrides
 * shouldShowName/extractRenderState, so the mixin must target that class —
 * injecting into EntityRenderer would never run for mobs.
 */
@Mixin(LivingEntityRenderer.class)
public class EntityRendererMixin {

    private static boolean wyr$shouldShow(LivingEntity entity) {
        Player player = Minecraft.getInstance().player;
        if (player == null || !ClientEffectCache.hasSeeMobHp()) return false;
        if (entity == player || entity instanceof EnderDragon || entity instanceof WitherBoss) return false;
        if (entity.isInvisible() || entity.isVehicle() || !entity.isAlive()) return false;
        // Show for any mob within a generous range (no line-of-sight requirement
        // so the bar is always visible like the YDM mod).
        return player.distanceToSqr(entity) <= 64.0 * 64.0;
    }

    @Inject(method = "shouldShowName", at = @At("HEAD"), cancellable = true)
    private void wyr_forceShowHpName(LivingEntity entity, double distanceSq, CallbackInfoReturnable<Boolean> cir) {
        if (wyr$shouldShow(entity)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void wyr_appendHealthToName(LivingEntity entity, LivingEntityRenderState state, float partialTick, CallbackInfo ci) {
        if (!wyr$shouldShow(entity)) {
            return;
        }

        // Numeric readout: current HP / total HP (e.g. "15/20").
        float health = entity.getHealth();
        float maxHealth = entity.getMaxHealth();
        MutableComponent numbers = Component.literal(String.format("%.0f/%.0f", health, maxHealth)).withStyle(ChatFormatting.RED);

        // Name tags can't render '\n' (it shows as a box glyph), so keep the
        // name and HP on a single line separated by a space.
        Component name = state.nameTag != null ? state.nameTag : entity.getDisplayName();
        MutableComponent customNameWithHp = Component.empty().append(name).append(" ").append(numbers);
        state.nameTag = customNameWithHp;

        // The name tag is only submitted when BOTH nameTag and nameTagAttachment
        // are non-null. extractNameTags() only fills nameTagAttachment when
        // shouldShowName() passes, so force it here to guarantee the bar draws
        // even if the vanilla gate wasn't reached.
        if (state.nameTagAttachment == null) {
            state.nameTagAttachment = new net.minecraft.world.phys.Vec3(0.0, entity.getBbHeight() + 0.5, 0.0);
        }
    }
}
