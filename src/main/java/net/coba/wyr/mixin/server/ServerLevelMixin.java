package net.coba.wyr.mixin.server;

// This mixin has been removed because getMaxBuildHeight is a server-wide setting
// The build limit restriction is now handled properly in the block placement event
// in ChoiceEffects.onBlockPlace() which checks per-player permissions
