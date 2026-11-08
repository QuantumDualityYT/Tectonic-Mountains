package com.quantumduality.tectonicmountains;

import java.nio.file.Path;

import java.util.List;

import net.minecraft.network.chat.Component;

import net.minecraft.resources.ResourceLocation;

import net.minecraft.server.packs.*;

import net.minecraft.server.packs.repository.*;

import net.minecraft.server.packs.resources.MultiPackResourceManager;



public class TerrainPackCheck {

 public static void main(String[] args) throws Exception {

  net.minecraft.SharedConstants.tryDetectVersion();

  Path root = Path.of("build/resources/main/terrain");

  Pack baseline = Pack.readMetaAndCreate("tectonic/tectonic", Component.literal("baseline"), false,

    id -> new PathPackResources(id, root, false), PackType.SERVER_DATA, Pack.Position.TOP, PackSource.BUILT_IN);

  Pack own = Pack.readMetaAndCreate(TectonicMountains.TERRAIN_PACK, Component.literal("test"), true,

    id -> new PathPackResources(id, root, false), PackType.SERVER_DATA, Pack.Position.TOP, PackSource.BUILT_IN);

  if (baseline == null || own == null) throw new AssertionError("Invalid pack metadata");

  PackRepository repo = new PackRepository(c -> c.accept(baseline), c -> c.accept(own)); {

   repo.reload(); repo.setSelected(List.of("tectonic/tectonic"));

   var packs = repo.getSelectedPacks().stream().toList();

   if (!packs.get(packs.size()-1).getId().equals(TectonicMountains.TERRAIN_PACK)) throw new AssertionError("Priority incorrect");

   try (var resources = new MultiPackResourceManager(PackType.SERVER_DATA, repo.openAllSelected())) {

    var depth = resources.getResource(new ResourceLocation("tectonic", "worldgen/density_function/depth.json")).orElseThrow();

    if (!depth.sourcePackId().equals(TectonicMountains.TERRAIN_PACK)) throw new AssertionError("Wrong terrain source");

   }

  }

  System.out.println("PASS: required terrain pack auto-enabled last; resource winner verified.");

 }

}





