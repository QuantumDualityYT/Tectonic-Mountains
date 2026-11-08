package com.quantumduality.tectonicmountains;

import net.neoforged.fml.common.Mod;

import net.neoforged.api.distmarker.Dist;

import net.neoforged.bus.api.SubscribeEvent;

import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;

import net.minecraft.client.renderer.DimensionSpecialEffects;

import net.minecraft.client.Minecraft;

import net.minecraft.resources.ResourceLocation;

@net.neoforged.fml.common.EventBusSubscriber(modid=TectonicMountains.MOD_ID,value=Dist.CLIENT)

public final class CloudSettingsEffects {

 @SubscribeEvent public static void register(RegisterDimensionSpecialEffectsEvent event){

  event.register(ResourceLocation.fromNamespaceAndPath("tectonic_mountains","overworld"),new DimensionSpecialEffects.OverworldEffects(){

   @Override public float getCloudHeight(){var server=Minecraft.getInstance().getSingleplayerServer();return server==null?256:WorldSettingsStore.forWorld(server.overworld()).cloudHeight;}

  });

 }

}

