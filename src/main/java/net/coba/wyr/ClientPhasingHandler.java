package net.coba.wyr;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class ClientPhasingHandler {
    private int cooldown = 0;

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Pre event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        // A small cooldown to prevent spamming the server with packets
        if (cooldown > 0) {
            cooldown--;
            return;
        }

        // The client checks if the player is pressing the forward key and is on the ground.
        boolean isMovingForward = Minecraft.getInstance().options.keyUp.isDown();
        if (isMovingForward && player.onGround()) {
            Direction facing = player.getDirection();
            BlockPos wallPos = player.blockPosition().relative(facing);
            BlockState wallState = player.level().getBlockState(wallPos);
            BlockPos wallPosHead = player.blockPosition().above().relative(facing);
            BlockState wallStateHead = player.level().getBlockState(wallPosHead);

            // If there's a solid wall in front of the player...
            if (wallState.isCollisionShapeFullBlock(player.level(), wallPos) ||
                    wallStateHead.isCollisionShapeFullBlock(player.level(), wallPosHead)) {

                // ...send a packet to the server and start the cooldown.
                PacketDistributor.sendToServer(new PacketAttemptPhase());
                cooldown = 5; // 5-tick cooldown (quarter of a second)
            }
        }
    }
}