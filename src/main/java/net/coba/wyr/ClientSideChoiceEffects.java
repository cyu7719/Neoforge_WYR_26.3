package net.coba.wyr;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

// This annotation ensures this class ONLY loads on the client
@EventBusSubscriber(modid = WYR.MODID, value = Dist.CLIENT)
public class ClientSideChoiceEffects {

    // This method has been moved from ChoiceEffects.java
    @SubscribeEvent
    public static void onClientPlayerTick(ClientTickEvent.Pre event) {
         Minecraft mc = Minecraft.getInstance();
         Player player = mc.player;
         if (player == null || mc.level == null) {
             return;
         }
        
         // Enforce render distance lock for no_fall_damage_low_render_distance
         if (ClientEffectCache.hasLowRenderDistance()) {
             if (mc.options.renderDistance().get() != 5) {
                 mc.options.renderDistance().set(5);
                 mc.levelRenderer.clearVisibleSections();
             }
         }
        
         if (ClientEffectCache.hasWaterWalk()) {
             BlockPos pos = BlockPos.containing(player.getX(), player.getY() - 0.2D, player.getZ());
             BlockState state = player.level().getBlockState(pos);
        
             if (state.getFluidState().is(FluidTags.WATER) || state.getFluidState().is(FluidTags.LAVA)) {
                 Vec3 motion = player.getDeltaMovement();
                 if (motion.y < 0.0D) {
                     player.setDeltaMovement(motion.x, 0.0D, motion.z);
                     player.setOnGround(true);
                     player.fallDistance = 0F;
                 }
             }
         }
    }
}