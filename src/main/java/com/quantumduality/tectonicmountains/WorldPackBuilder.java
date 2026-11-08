package com.quantumduality.tectonicmountains;

import com.google.gson.*;

import java.nio.file.*;

import java.io.*;

import java.util.*;



/** Generates a real world-local data pack, selected after the mod's terrain pack. */

public final class WorldPackBuilder {

 public static final String FOLDER="tectonic_mountains_world";

 private static JsonObject read(Path path)throws IOException{return JsonParser.parseString(Files.readString(path)).getAsJsonObject();}

 private static void write(Path root,String rel,JsonElement json)throws IOException{Path p=root.resolve(rel);Files.createDirectories(p.getParent());Files.writeString(p,WorldSettings.JSON.toJson(json));}

 public static void build(Path terrain,Path target,WorldSettings options,JsonObject noiseSettings,JsonObject dimensionType)throws IOException {

  options.validate();Files.createDirectories(target);

  try(var files=Files.walk(terrain.resolve("data"))){for(Path p:files.filter(Files::isRegularFile).toList()){Path dest=target.resolve(terrain.relativize(p).toString());Files.createDirectories(dest.getParent());Files.copy(p,dest,StandardCopyOption.REPLACE_EXISTING);}}

  write(target,"pack.mcmeta",JsonParser.parseString("{\"pack\":{\"pack_format\":15,\"description\":\"Tectonic Mountains — saved world settings\"}}"));

  write(target,"data/tectonic_mountains/world_settings.json",WorldSettings.JSON.toJsonTree(options));

  Path inland=target.resolve("data/appalachian/worldgen/density_function/inland_base.json");var field=read(inland);

  transform(field,options);write(target,"data/appalachian/worldgen/density_function/inland_base.json",field);

  // The local snapshot can optionally restore the dependency's original ocean distribution.

  if(!options.removeOceans){

   try(var stream=WorldPackBuilder.class.getResourceAsStream("/templates/original_overworld.json")){

    if(stream==null)throw new IOException("Missing original biome template");

    write(target,"data/minecraft/dimension/overworld.json",JsonParser.parseReader(new InputStreamReader(stream,java.nio.charset.StandardCharsets.UTF_8)));

   }



   // Restore Tectonic's own continental signal without removing a file from its pack.

   write(target,"data/tectonic/worldgen/density_function/noise/raw_continents.json",JsonParser.parseString("{\"type\":\"minecraft:flat_cache\",\"argument\":{\"type\":\"minecraft:add\",\"argument1\":\"tectonic:__constants/ocean_offset\",\"argument2\":{\"type\":\"minecraft:spline\",\"spline\":{\"coordinate\":{\"type\":\"minecraft:abs\",\"argument\":\"tectonic:__constants/noise/continents\"},\"points\":[{\"location\":0,\"value\":0,\"derivative\":0},{\"location\":0.4,\"value\":0.575,\"derivative\":1},{\"location\":0.48,\"value\":0.68,\"derivative\":1}]}}}}"));

  }

  if(!options.removeOceans){

   var depth=read(target.resolve("data/tectonic/worldgen/density_function/depth.json"));

   depth.getAsJsonObject("argument").getAsJsonObject("argument1").add("argument2",JsonParser.parseString("{\"type\":\"minecraft:add\",\"argument1\":{\"type\":\"minecraft:mul\",\"argument1\":\"appalachian:land_mask\",\"argument2\":\"appalachian:inland_base\"},\"argument2\":{\"type\":\"minecraft:mul\",\"argument1\":{\"type\":\"minecraft:add\",\"argument1\":1,\"argument2\":{\"type\":\"minecraft:mul\",\"argument1\":-1,\"argument2\":\"appalachian:land_mask\"}},\"argument2\":\"tectonic:terrain_spline/offset/final\"}}"));

   write(target,"data/tectonic/worldgen/density_function/depth.json",depth);

  }

  if(options.villages){

   Map<String,List<String>> biomes=Map.of("plains",List.of("minecraft:plains","minecraft:meadow"),"desert",List.of("minecraft:desert"),"savanna",List.of("minecraft:savanna"),"snowy",List.of("minecraft:snowy_plains"),"taiga",List.of("minecraft:taiga","minecraft:old_growth_pine_taiga","minecraft:old_growth_spruce_taiga"));

   for(var entry:biomes.entrySet()){var tag=new JsonObject();tag.addProperty("replace",true);tag.add("values",WorldSettings.JSON.toJsonTree(entry.getValue()));write(target,"data/minecraft/tags/worldgen/biome/has_structure/village_"+entry.getKey()+".json",tag);}

  }

  if(noiseSettings!=null&&dimensionType!=null){

   int minY=dimensionType.get("min_y").getAsInt();int height=options.buildTop-minY;

   dimensionType.addProperty("height",height);dimensionType.addProperty("logical_height",height);dimensionType.addProperty("effects","tectonic_mountains:overworld");

   noiseSettings.getAsJsonObject("noise").addProperty("height",height);

   write(target,"data/minecraft/dimension_type/overworld.json",dimensionType);

   write(target,"data/minecraft/worldgen/noise_settings/overworld.json",noiseSettings);

   write(target,"data/tectonic/lithostitched/worldgen_modifier/set_height_limits.json",JsonParser.parseString("{\"type\":\"lithostitched:no_op\"}"));

  }

 }

 private static void transform(JsonElement element,WorldSettings s){

  if(element.isJsonArray()){for(var child:element.getAsJsonArray())transform(child,s);return;}if(!element.isJsonObject())return;

  var o=element.getAsJsonObject();

  if(o.has("noise")&&o.get("noise").isJsonPrimitive()&&o.has("xz_scale"))o.addProperty("xz_scale",o.get("xz_scale").getAsDouble()/s.mountainMass);

  if(o.has("spline")){

   var spline=o.getAsJsonObject("spline");var coord=spline.get("coordinate");

   if(coord.isJsonObject()&&coord.getAsJsonObject().has("noise")&&coord.getAsJsonObject().get("noise").getAsString().equals("appalachian:range_layout")){

    for(var p:spline.getAsJsonArray("points")){var point=p.getAsJsonObject();double t=(point.get("value").getAsDouble()-.85)/.23;double peak=s.mountainMin+(s.mountainMax-s.mountainMin)*t;point.addProperty("value",(peak-72.96)/(128*.96));point.addProperty("derivative",0);}

   }

   if(coord.isJsonObject()&&coord.getAsJsonObject().has("type")&&coord.getAsJsonObject().get("type").getAsString().equals("minecraft:abs")){

    for(var p:spline.getAsJsonArray("points")){var point=p.getAsJsonObject();point.addProperty("location",point.get("location").getAsDouble()/s.valleyLength);point.addProperty("derivative",point.get("derivative").getAsDouble()*s.valleyLength);}

   }

  }

  for(var e:o.entrySet())transform(e.getValue(),s);

 }

}

