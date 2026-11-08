package com.quantumduality.tectonicmountains;
import java.nio.file.*;
import com.google.gson.*;
public class TerrainPackCheck {
 public static void main(String[] args)throws Exception {
  Path root=Path.of("build/resources/main/terrain");
  var meta=JsonParser.parseString(Files.readString(root.resolve("pack.mcmeta"))).getAsJsonObject();
  if(meta.getAsJsonObject("pack").get("pack_format").getAsInt()!=48)throw new AssertionError("Wrong 1.21.1 data-pack format");
  for(String feature:new String[]{"mountain_springs","waterway_finish"}){
   var v=JsonParser.parseString(Files.readString(root.resolve("data/tectonic_mountains/neoforge/biome_modifier/"+feature+".json"))).getAsJsonObject();
   if(!v.get("type").getAsString().equals("neoforge:add_features"))throw new AssertionError("Wrong modifier loader");
  }
  if(!Files.exists(root.resolve("data/tectonic/worldgen/density_function/depth.json")))throw new AssertionError("Terrain absent");
  System.out.println("PASS: NeoForge terrain packaging, pack format 48 and biome modifier schemas (file check, not runtime priority).");
 }
}
