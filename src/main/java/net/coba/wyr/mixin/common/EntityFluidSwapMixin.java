// package net.coba.wyr.mixin.common;

// import net.coba.wyr.ChoiceEffects;
// import net.coba.wyr.WYR;
// import net.minecraft.world.entity.Entity;
// import net.minecraft.world.entity.EntityFluidInteraction;
// import net.minecraft.world.entity.item.ItemEntity;
// import net.minecraft.world.entity.player.Player;
// import net.neoforged.neoforge.common.NeoForgeMod;
// import net.neoforged.neoforge.fluids.FluidType;
// import org.spongepowered.asm.mixin.Mixin;
// import org.spongepowered.asm.mixin.Shadow;
// import org.spongepowered.asm.mixin.Unique;
// import org.spongepowered.asm.mixin.injection.At;
// import org.spongepowered.asm.mixin.injection.Inject;
// import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// import java.lang.reflect.Field;
// import java.util.Map;

// /**
//  * lava_swim_water_damage: swaps water and lava at the source.
//  *
//  * EntityFluidInteraction keeps a per-entity map of FluidType -> Tracker, where
//  * each Tracker records submerged height and whether the eyes are inside. Every
//  * fluid query on Entity reads from this map:
//  *   isInFluid(type)        -> tracker.height > 0
//  *   isInFluidMatching(pred)-> pred applied to the KEY's FluidType where height>0
//  *   isEyeInFluid(type)     -> tracker.eyesInside
//  *   isInWater()            -> the cached wasTouchingWater field
//  *
//  * EntityFluidInteraction is loaded too early for a mixin to transform it, so we
//  * inject into Entity.updateFluidInteraction() (which IS transformable) at RETURN,
//  * after the scan has populated the trackers.
//  *
//  * A two-way swap of tracker objects fails because update() re-scans into whatever
//  * tracker sits under each key every tick, undoing the swap. Instead we MOVE the
//  * single active tracker to the opposite key: when the entity is really in lava,
//  * the lava tracker is relocated under the WATER key (so isInWater/isInFluidMatching
//  * see it as water and isInLava sees nothing), and vice versa. Then we recompute
//  * wasTouchingWater so isInWater() reflects the swapped state this same tick.
//  */
// @Mixin(Entity.class)
// public abstract class EntityFluidSwapMixin {

//     @Shadow
//     private boolean wasTouchingWater;

//     @Unique
//     private static Field wyr$trackerField;
//     @Unique
//     private static boolean wyr$fieldInit;
//     @Unique
//     private static boolean wyr$fieldFailed;
//     @Unique
//     private long wyr$lastDebug;

//     @Unique
//     @SuppressWarnings("unchecked")
//     private static Map<FluidType, Object> wyr$trackerMap(EntityFluidInteraction fi) {
//         if (!wyr$fieldInit) {
//             wyr$fieldInit = true;
//             try {
//                 wyr$trackerField = EntityFluidInteraction.class.getDeclaredField("trackerByFluid");
//                 wyr$trackerField.setAccessible(true);
//             } catch (Throwable t) {
//                 wyr$fieldFailed = true;
//                 WYR.LOGGER.error("[WYR-FLUID] reflection on trackerByFluid failed", t);
//             }
//         }
//         if (wyr$fieldFailed || wyr$trackerField == null) {
//             return null;
//         }
//         try {
//             return (Map<FluidType, Object>) wyr$trackerField.get(fi);
//         } catch (Throwable t) {
//             return null;
//         }
//     }

//     /**
//      * Whether the swap should apply to this entity: the player who owns the
//      * effect, or an ItemEntity while any player in the level has the effect
//      * (so dropped items don't burn in lava and burn in water).
//      */
//     @Unique
//     private static boolean wyr$swapApplies(Entity self) {
//         if (self instanceof Player player) {
//             return ChoiceEffects.hasEffect(player, "lava_swim_water_damage");
//         }
//         if (self instanceof ItemEntity) {
//             for (Player p : self.level().players()) {
//                 if (ChoiceEffects.hasEffect(p, "lava_swim_water_damage")) {
//                     return true;
//                 }
//             }
//         }
//         return false;
//     }

//     @Inject(method = "updateFluidInteraction", at = @At("RETURN"))
//     private void wyr$swapWaterLavaTrackers(CallbackInfoReturnable<Boolean> cir) {
//         Entity self = (Entity) (Object) this;
//         if (!wyr$swapApplies(self)) {
//             return;
//         }

//         EntityFluidInteraction fi = self.getFluidInteraction();
//         Map<FluidType, Object> map = wyr$trackerMap(fi);
//         if (map == null) {
//             return;
//         }

//         FluidType water = NeoForgeMod.WATER_TYPE.value();
//         FluidType lava = NeoForgeMod.LAVA_TYPE.value();

//         // True (pre-swap) fluid state: a tracker is "in" a fluid when height > 0.
//         boolean inWater = fi.isInFluid(water);
//         boolean inLava = fi.isInFluid(lava);

//         long now = System.currentTimeMillis();
//         if (self instanceof Player && now - wyr$lastDebug > 2000) {
//             wyr$lastDebug = now;
//             WYR.LOGGER.error("[WYR-FLUID] swap side={} inWater={} inLava={} keys={}",
//                     self.level().isClientSide() ? "client" : "server", inWater, inLava, map.keySet());
//         }

//         // Move the single active tracker to the opposite key. update() rebuilds
//         // the map each tick, so this is re-applied every tick and never
//         // oscillates. Only act when exactly one of the two is active.
//         if (inLava && !inWater) {
//             Object tracker = map.remove(lava);
//             if (tracker != null) {
//                 map.put(water, tracker);
//             }
//         } else if (inWater && !inLava) {
//             Object tracker = map.remove(water);
//             if (tracker != null) {
//                 map.put(lava, tracker);
//             }
//         }

//         // isInWater() reads the cached wasTouchingWater field, which was set
//         // earlier in updateFluidInteraction() from the unswapped map. Recompute
//         // it now against the swapped map so isInWater() is correct this tick.
//         this.wasTouchingWater = fi.isInFluidMatching(self, (e, type, h) -> type.getIsWaterLike());
//     }
// }
