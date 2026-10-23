package net.coba.wyr.ai;

import net.coba.wyr.ChoiceEffects;
import net.coba.wyr.WYR;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.UUID;

public class FollowPlayerGoal extends Goal {
    private final Monster monster;
    private Player owner;
    private final Level level;
    private final double speedModifier;
    private final PathNavigation navigation;
    private int timeToRecalcPath;
    private final float stopDistance;
    private final float startDistance;
    private float oldWaterCost;

    public FollowPlayerGoal(Monster pMonster, double pSpeedModifier, float pStartDistance, float pStopDistance) {
        this.monster = pMonster;
        this.level = pMonster.level();
        this.speedModifier = pSpeedModifier;
        this.navigation = pMonster.getNavigation();
        this.startDistance = pStartDistance;
        this.stopDistance = pStopDistance;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.owner == null) {
            UUID ownerUUID = ChoiceEffects.getTamedMobOwner(this.monster.getUUID());
            if (ownerUUID == null) return false;
            this.owner = this.level.getPlayerByUUID(ownerUUID);
        }

        if (this.owner == null || this.owner.isSpectator()) {
            return false;
        }

        double distanceSq = this.monster.distanceToSqr(this.owner);
        boolean shouldStart = distanceSq > (double) (this.stopDistance * this.stopDistance);

        // ADD THIS LOG
        WYR.LOGGER.info("[FollowPlayerGoal] Checking canUse. Distance squared: {}. Should start: {}", String.format("%.2f", distanceSq), shouldStart);

        return shouldStart;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.navigation.isDone() || this.owner == null || this.owner.isSpectator()) {
            return false;
        }
        return !(this.monster.distanceToSqr(this.owner) <= (double) (this.stopDistance * this.stopDistance));
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
        this.oldWaterCost = this.monster.getPathfindingMalus(PathType.WATER);
        this.monster.setPathfindingMalus(PathType.WATER, 0.0F);
    }

    @Override
    public void stop() {
        this.owner = null;
        this.navigation.stop();
        this.monster.setPathfindingMalus(PathType.WATER, this.oldWaterCost);
    }

    @Override
    public void tick() {
        if (this.owner == null) return;
        this.monster.getLookControl().setLookAt(this.owner, 10.0F, (float) this.monster.getMaxHeadXRot());
        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = this.adjustedTickDelay(10);
            if (!this.monster.isLeashed() && !this.monster.isPassenger()) {
                if (this.monster.distanceToSqr(this.owner) >= 144.0) { // Teleport if more than 12 blocks away
                    this.teleportToOwner();
                } else {
                    this.navigation.moveTo(this.owner, this.speedModifier);
                }
            }
        }
    }

    private void teleportToOwner() {
        BlockPos ownerPos = this.owner.blockPosition();
        for (int i = 0; i < 10; ++i) {
            int dx = this.monster.getRandom().nextInt(4) - 2;
            int dy = this.monster.getRandom().nextInt(4) - 2;
            int dz = this.monster.getRandom().nextInt(4) - 2;
            if (Math.abs(dx) > 1 || Math.abs(dy) > 1 || Math.abs(dz) > 1) {
                BlockPos targetPos = ownerPos.offset(dx, dy, dz);
                if (this.canTeleportTo(targetPos)) {
                    this.monster.teleportTo(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);
                    this.navigation.stop();
                    return;
                }
            }
        }
    }

    private boolean canTeleportTo(BlockPos pos) {
        if (!this.level.getBlockState(pos.below()).isPathfindable(PathComputationType.LAND)) {
            return false;
        }

        BlockState blockState = this.level.getBlockState(pos);
        if (!blockState.isAir()) {
            return false;
        }
        return this.level.noCollision(this.monster, this.monster.getBoundingBox().move(Vec3.atBottomCenterOf(pos).subtract(this.monster.position())));
    }
}