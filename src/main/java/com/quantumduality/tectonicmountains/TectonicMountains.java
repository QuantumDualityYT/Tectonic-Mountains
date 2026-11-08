package com.quantumduality.tectonicmountains;



import net.minecraft.network.chat.Component;

import net.minecraft.server.packs.PackType;

import net.minecraft.server.packs.PathPackResources;

import net.minecraft.server.packs.repository.Pack;

import net.minecraft.server.packs.repository.PackSource;

import net.minecraftforge.event.AddPackFindersEvent;

import net.minecraftforge.event.AddReloadListenerEvent;

import net.minecraftforge.common.MinecraftForge;

import net.minecraft.resources.ResourceLocation;

import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

import org.slf4j.LoggerFactory;

import net.minecraftforge.fml.ModList;

import net.minecraftforge.fml.common.Mod;

import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;



/** Keeps our terrain resources separate from the ordinary mod resource pack. */

@Mod(TectonicMountains.MOD_ID)

public final class TectonicMountains {

    public static final String MOD_ID = "tectonic_mountains";

    public static final String TERRAIN_PACK = "tectonic_mountains/terrain";



    public TectonicMountains() {

        MountainSpringFeature.FEATURES.register(FMLJavaModLoadingContext.get().getModEventBus());

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::addTerrainPack);

        MinecraftForge.EVENT_BUS.addListener(this::verifyTerrainPack);

        MinecraftForge.EVENT_BUS.addListener(StreamClearance::tick);

    }



    // Refuse a test world with silently replaced terrain rules; log proof of the winner.

    private void verifyTerrainPack(AddReloadListenerEvent event) {

        event.addListener((ResourceManagerReloadListener) manager -> {

            var key = new ResourceLocation("tectonic", "worldgen/density_function/depth.json");

            var resource = manager.getResource(key).orElseThrow();

            if (!TERRAIN_PACK.equals(resource.sourcePackId()) && !("file/"+WorldPackBuilder.FOLDER).equals(resource.sourcePackId())) {

                throw new IllegalStateException("Tectonic Mountains terrain was overridden by " + resource.sourcePackId());

            }

            LoggerFactory.getLogger(MOD_ID).info("TERRAIN CONTROL VERIFIED: {} is supplied by {}", key, resource.sourcePackId());

        });

    }



    private void addTerrainPack(AddPackFindersEvent event) {

        if (event.getPackType() != PackType.SERVER_DATA) return;

        var root = ModList.get().getModFileById(MOD_ID).getFile().findResource("terrain");

        event.addRepositorySource(consumer -> {

            Pack pack = Pack.readMetaAndCreate(TERRAIN_PACK,

                Component.literal("Tectonic Mountains terrain — development"), true,

                id -> new PathPackResources(id, root, false), PackType.SERVER_DATA,

                Pack.Position.TOP, PackSource.BUILT_IN);

            if (pack != null) consumer.accept(pack);

        });

    }

}

