package net.coba.wyr.ai;

import net.coba.wyr.ChoiceEffects;
import net.coba.wyr.WYR;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class DefendPlayerGoal extends TargetGoal {
    private Player owner;
    private LivingEntity target;

    public DefendPlayerGoal(Monster pMob) {
        super(pMob, false);
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        // ADDED: Log to confirm the AI is checking this goal.
        WYR.LOGGER.info("[DefendPlayerGoal] AI is checking canUse for mob {}.", this.mob.getName().getString());

        if (this.owner == null) {
            UUID ownerUUID = ChoiceEffects.getTamedMobOwner(this.mob.getUUID());
            if (ownerUUID == null) {
                // ADDED: Log for a specific failure case.
                WYR.LOGGER.info("[DefendPlayerGoal] ...Failed: Mob is not in the tamed map.");
                return false;
            }
            this.owner = this.mob.level().getPlayerByUUID(ownerUUID);
        }

        if (this.owner == null) {
            WYR.LOGGER.info("[DefendPlayerGoal] ...Failed: Owner is not online or nearby.");
            return false;
        }

        AABB searchBox = this.owner.getBoundingBox().inflate(15.0);
        List<Monster> nearbyMonsters = this.mob.level().getEntitiesOfClass(Monster.class, searchBox,
                (monster) -> monster.isAlive() &&
                        !monster.getUUID().equals(this.owner.getUUID()) &&
                        !monster.getUUID().equals(this.mob.getUUID()) &&
                        !ChoiceEffects.isMobTamed(monster.getUUID()) &&
                        !(monster instanceof Creeper)
        );

        if (nearbyMonsters.isEmpty()) {
            // ADDED: Log to show when the search finds nothing.
            WYR.LOGGER.info("[DefendPlayerGoal] ...Finished check: No valid targets found near player.");
            return false;
        }

        WYR.LOGGER.info("[DefendPlayerGoal] Found {} potential targets near player.", nearbyMonsters.size());
        nearbyMonsters.sort(Comparator.comparingDouble(monster -> monster.distanceToSqr(this.owner)));
        this.target = nearbyMonsters.get(0);
        return true;
    }

    @Override
    public void start() {
        WYR.LOGGER.info("[DefendPlayerGoal] Setting target to: {}", this.target.getName().getString());
        this.mob.setTarget(this.target);
        super.start();
    }
}