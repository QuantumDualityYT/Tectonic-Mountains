package com.quantumduality.tectonicmountains;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
public final class CloudSettingsEffects implements ClientModInitializer {
 public void onInitializeClient(){
  ScreenEvents.AFTER_INIT.register((client,screen,width,height)->ClientSettingsHooks.init(screen));
  DimensionRenderingRegistry.registerDimensionEffects(new ResourceLocation("tectonic_mountains","overworld"),new DimensionSpecialEffects.OverworldEffects(){
   @Override public float getCloudHeight(){var server=Minecraft.getInstance().getSingleplayerServer();return server==null?256:WorldSettingsStore.forWorld(server.overworld()).cloudHeight;}
  });
 }
}
