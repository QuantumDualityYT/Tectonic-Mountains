package com.quantumduality.tectonicmountains;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("tectonic_mountains")
@PrefixGameTestTemplate(false)
public final class GenerationSmokeCheck {
 @GameTest(template="smoke_empty",templateNamespace="tectonic_mountains",timeoutTicks=100)
 public static void terrainLoaded(GameTestHelper helper){
  var level=helper.getLevel();
  var resource=level.getServer().getResourceManager().getResource(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("tectonic","worldgen/density_function/depth.json")).orElseThrow();
  if(!resource.sourcePackId().equals(TectonicMountains.TERRAIN_PACK))throw new AssertionError("Wrong terrain winner: "+resource.sourcePackId());
  var features=level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.PLACED_FEATURE);
  if(features.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("tectonic_mountains","mountain_spring"))==null)throw new AssertionError("Feature absent");
  helper.succeed();
 }
}
