package net.coba.wyr.util;

import java.util.UUID;

/**
 * Implemented by AbstractFurnaceBlockEntityMixin so the furnace's owner UUID
 * can be read/written from outside the mixin (event handlers, other mixins).
 * Casting to a mixin class directly fails at runtime because mixins aren't
 * part of the target class hierarchy - an interface is required.
 */
public interface FurnaceOwnerAccess {
    UUID wyr$getOwner();
    void wyr$setOwner(UUID uuid);
}