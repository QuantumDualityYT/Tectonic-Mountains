package com.quantumduality.tectonicmountains;

import com.google.gson.*;

import java.nio.file.*;



/** Values are copied into each new world's data pack; menu defaults are never live world state. */

public final class WorldSettings {

 public double mountainMass=1, valleyLength=1, riverWidth=1, pondSize=1, pondDepth=1;

 public int mountainMin=177, mountainMax=205, buildTop=320, cloudHeight=256;

 public boolean streams=true, villages=false, removeOceans=true;

 public static final Gson JSON=new GsonBuilder().setPrettyPrinting().create();

 public WorldSettings copy(){return JSON.fromJson(JSON.toJson(this),WorldSettings.class);}

 public void validate(){

  for(double n:new double[]{mountainMass,valleyLength,riverWidth,pondSize,pondDepth})if(!Double.isFinite(n)||n<.5||n>2)throw new IllegalArgumentException("Scale must be between 0.5 and 2");

  if(buildTop<320||buildTop>1024||buildTop%16!=0)throw new IllegalArgumentException("Build ceiling must be 320–1024 in multiples of 16");

  if(mountainMin<100||mountainMax<mountainMin||mountainMax>buildTop-32)throw new IllegalArgumentException("Peak targets must be ordered and below the build ceiling");

  if(cloudHeight<128||cloudHeight>buildTop+256)throw new IllegalArgumentException("Cloud height outside supported range");

 }

 public static Path defaultsPath(){return net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir().resolve("tectonic-mountains-new-world.json");}

 public static WorldSettings loadDefaults() throws java.io.IOException {

  if(!Files.exists(defaultsPath()))return new WorldSettings();

  var s=JSON.fromJson(Files.readString(defaultsPath()),WorldSettings.class);if(s==null)throw new IllegalArgumentException("Settings file is empty");s.validate();return s;

 }

 public void saveDefaults() throws java.io.IOException {

  validate();Path path=defaultsPath();Files.createDirectories(path.getParent());Path temp=path.resolveSibling(path.getFileName()+".tmp");Files.writeString(temp,JSON.toJson(this));

  try{Files.move(temp,path,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}catch(AtomicMoveNotSupportedException e){Files.move(temp,path,StandardCopyOption.REPLACE_EXISTING);}

 }

}

