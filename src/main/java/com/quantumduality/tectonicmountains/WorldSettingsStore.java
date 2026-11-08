package com.quantumduality.tectonicmountains;

import java.util.*;

import net.minecraft.server.level.ServerLevel;

import net.minecraft.resources.ResourceLocation;



public final class WorldSettingsStore {

 private static final Map<ServerLevel,WorldSettings> CACHE=Collections.synchronizedMap(new WeakHashMap<>());

 public static WorldSettings forWorld(ServerLevel level){

  synchronized(CACHE){return CACHE.computeIfAbsent(level,w->{

   var resource=w.getServer().getResourceManager().getResource(new ResourceLocation("tectonic_mountains","world_settings.json"));

   if(resource.isEmpty())return new WorldSettings(); // Legacy worlds retain the baseline, never current menu defaults.

   try(var reader=resource.get().openAsReader()){var settings=WorldSettings.JSON.fromJson(reader,WorldSettings.class);settings.validate();return settings;}

   catch(Exception e){throw new IllegalStateException("Invalid saved Tectonic Mountains settings",e);}

  });}

 }

}

