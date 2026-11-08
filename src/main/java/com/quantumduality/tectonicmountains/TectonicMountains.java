package com.quantumduality.tectonicmountains;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.GenerationStep;
public final class TectonicMountains implements ModInitializer {
 public static final String MOD_ID="tectonic_mountains";
 public static final String TERRAIN_PACK="tectonic_mountains:terrain";
 public void onInitialize(){
  Registry.register(BuiltInRegistries.FEATURE,new ResourceLocation(MOD_ID,"mountain_spring"),new MountainSpringFeature());
  Registry.register(BuiltInRegistries.FEATURE,new ResourceLocation(MOD_ID,"waterway_finish"),new WaterwayFinishFeature());
  if(!ResourceManagerHelper.registerBuiltinResourcePack(new ResourceLocation(MOD_ID,"terrain"),FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow(),ResourcePackActivationType.ALWAYS_ENABLED))throw new IllegalStateException("Terrain pack missing");
  BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),GenerationStep.Decoration.LOCAL_MODIFICATIONS,ResourceKey.create(Registries.PLACED_FEATURE,new ResourceLocation(MOD_ID,"mountain_spring")));
  BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),GenerationStep.Decoration.TOP_LAYER_MODIFICATION,ResourceKey.create(Registries.PLACED_FEATURE,new ResourceLocation(MOD_ID,"waterway_finish")));
  ResourceManagerHelper.get(net.minecraft.server.packs.PackType.SERVER_DATA).registerReloadListener(new net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener(){
   public ResourceLocation getFabricId(){return new ResourceLocation(MOD_ID,"terrain_verification");}
   public void onResourceManagerReload(net.minecraft.server.packs.resources.ResourceManager manager){
    var key=new ResourceLocation("tectonic","worldgen/density_function/depth.json");
    var resource=manager.getResource(key).orElseThrow();
    if(!TERRAIN_PACK.equals(resource.sourcePackId()) && !("file/"+WorldPackBuilder.FOLDER).equals(resource.sourcePackId()))throw new IllegalStateException("Terrain overridden by "+resource.sourcePackId());
    org.slf4j.LoggerFactory.getLogger(MOD_ID).info("TERRAIN CONTROL VERIFIED: {} supplied by {}",key,resource.sourcePackId());
   }
  });
  ServerTickEvents.END_WORLD_TICK.register(StreamClearance::tick);
 }
}
