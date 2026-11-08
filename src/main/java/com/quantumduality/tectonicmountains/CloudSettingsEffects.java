package com.quantumduality.tectonicmountains;

import net.minecraftforge.fml.common.Mod;

import net.minecraftforge.api.distmarker.Dist;

import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;

import net.minecraft.client.renderer.DimensionSpecialEffects;

import net.minecraft.client.Minecraft;

import net.minecraft.resources.ResourceLocation;

@Mod.EventBusSubscriber(modid=TectonicMountains.MOD_ID,value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)

public final class CloudSettingsEffects {

 @SubscribeEvent public static void register(RegisterDimensionSpecialEffectsEvent event){

  event.register(new ResourceLocation("tectonic_mountains","overworld"),new DimensionSpecialEffects.OverworldEffects(){

   @Override public float getCloudHeight(){var server=Minecraft.getInstance().getSingleplayerServer();return server==null?256:WorldSettingsStore.forWorld(server.overworld()).cloudHeight;}

  });

 }

}

