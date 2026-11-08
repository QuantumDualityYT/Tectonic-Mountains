package com.quantumduality.tectonicmountains;

import java.nio.file.*;

import java.util.*;

import com.google.gson.*;



public class WorldSettingsCheck {

 static JsonObject read(Path p)throws Exception{return JsonParser.parseString(Files.readString(p)).getAsJsonObject();}

 public static void main(String[] args)throws Exception {

  Path root=Files.createTempDirectory(Path.of("build"),"settings-test-");var first=new WorldSettings();first.validate();

  Path terrain=Path.of("build/resources/main/resourcepacks/terrain");

  var noise=JsonParser.parseString("{\"noise\":{\"height\":384}}").getAsJsonObject();

  var dimension=JsonParser.parseString("{\"min_y\":-64,\"height\":384,\"logical_height\":384}").getAsJsonObject();

  WorldPackBuilder.build(terrain,root.resolve("world-a"),first,noise.deepCopy(),dimension.deepCopy());

  String snapshot=Files.readString(root.resolve("world-a/data/tectonic_mountains/world_settings.json"));

  var next=first.copy();next.mountainMass=1.7;next.valleyLength=1.4;next.mountainMin=200;next.mountainMax=350;next.buildTop=512;next.cloudHeight=400;next.villages=true;next.removeOceans=false;next.riverWidth=1.8;next.pondSize=1.8;next.pondDepth=1.7;

  WorldPackBuilder.build(terrain,root.resolve("world-b"),next,noise.deepCopy(),dimension.deepCopy());

  if(!snapshot.equals(Files.readString(root.resolve("world-a/data/tectonic_mountains/world_settings.json"))))throw new AssertionError("Old world snapshot mutated");

  var saved=WorldSettings.JSON.fromJson(snapshot,WorldSettings.class);if(saved.mountainMass!=1||saved.villages)throw new AssertionError("Snapshot roundtrip");

  if(Files.readString(root.resolve("world-a/data/appalachian/worldgen/density_function/inland_base.json")).equals(Files.readString(root.resolve("world-b/data/appalachian/worldgen/density_function/inland_base.json"))))throw new AssertionError("Terrain controls not applied");

  if(read(root.resolve("world-b/data/minecraft/dimension_type/overworld.json")).get("height").getAsInt()!=576)throw new AssertionError("Build height not applied");

  if(read(root.resolve("world-b/data/minecraft/tags/worldgen/biome/has_structure/village_plains.json")).getAsJsonArray("values").isEmpty())throw new AssertionError("Villages not enabled");

  if(!Files.readString(root.resolve("world-b/data/minecraft/dimension/overworld.json")).contains("minecraft:ocean"))throw new AssertionError("Ocean template not restored");

  if(StreamShape.style(9,0,0,next).radius()<=StreamShape.style(9,0,0,first).radius())throw new AssertionError("River control not wired");

  var disabled=first.copy();disabled.streams=false;if(!StreamPlanner.plan(3,0,0,(x,z)->200-x/8,disabled).isEmpty())throw new AssertionError("Disabled streams still plan");

  int control=0;
  for(String setting:List.of("mass","minimum","maximum","valleys")){
   var changed=first.copy();switch(setting){case "mass"->changed.mountainMass=1.5;case "minimum"->changed.mountainMin=140;case "maximum"->changed.mountainMax=240;case "valleys"->changed.valleyLength=1.5;}
   Path pack=root.resolve("control-"+(control++));WorldPackBuilder.build(terrain,pack,changed,noise.deepCopy(),dimension.deepCopy());
   if(Files.readString(pack.resolve("data/appalachian/worldgen/density_function/inland_base.json")).equals(Files.readString(root.resolve("world-a/data/appalachian/worldgen/density_function/inland_base.json"))))throw new AssertionError("Unwired control: "+setting);
  }
  boolean pondSizeChanged=false,pondDepthChanged=false;
  for(long seed=0;seed<30;seed++){
   if(StreamShape.style(seed,0,0,next).pondRadius()!=StreamShape.style(seed,0,0,first).pondRadius())pondSizeChanged=true;
   var route=List.of(new StreamPlanner.Node(0,0,150),new StreamPlanner.Node(3,0,149));
   var normal=StreamShape.columns(seed,0,0,route,-40,-40,40,40,first);var deeper=StreamShape.columns(seed,0,0,route,-40,-40,40,40,next);
   if(deeper.values().stream().mapToInt(StreamShape.Column::depth).max().orElse(0)>normal.values().stream().mapToInt(StreamShape.Column::depth).max().orElse(0))pondDepthChanged=true;
  }
  if(!pondSizeChanged||!pondDepthChanged)throw new AssertionError("Pond controls have no effect");
  var bad=first.copy();bad.mountainMin=300;try{bad.validate();throw new AssertionError("Inverted heights accepted");}catch(IllegalArgumentException expected){}

  try(var files=Files.walk(root)){for(Path f:files.filter(Files::isRegularFile).toList())if(f.toString().endsWith(".json")||f.getFileName().toString().equals("pack.mcmeta"))JsonParser.parseString(Files.readString(f));}

  System.out.println("PASS: frozen world snapshots, terrain edits, ocean/village/height settings, water controls and validation.");

 }

}

