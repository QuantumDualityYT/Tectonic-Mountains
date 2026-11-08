package com.quantumduality.tectonicmountains;

import net.neoforged.api.distmarker.Dist;

import net.neoforged.fml.common.Mod;

import net.neoforged.bus.api.SubscribeEvent;

import net.neoforged.neoforge.client.event.ScreenEvent;

import net.neoforged.fml.util.ObfuscationReflectionHelper;

import net.minecraft.client.Minecraft;

import net.minecraft.client.gui.screens.*;

import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;

import net.minecraft.client.gui.components.Button;

import net.minecraft.network.chat.Component;

import net.minecraft.world.level.WorldDataConfiguration;

import net.minecraft.server.packs.repository.PackRepository;

import net.minecraft.core.registries.Registries;

import net.minecraft.resources.RegistryOps;

import net.minecraft.world.level.dimension.DimensionType;

import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

import net.neoforged.fml.ModList;

import com.mojang.serialization.JsonOps;

import com.mojang.datafixers.util.Pair;

import java.nio.file.*;

import java.util.*;

import java.util.function.Consumer;



@net.neoforged.fml.common.EventBusSubscriber(modid=TectonicMountains.MOD_ID,value=Dist.CLIENT)

public final class ClientSettingsHooks {

 private static final Set<CreateWorldScreen> PREPARED=Collections.newSetFromMap(new WeakHashMap<>());

 @SubscribeEvent public static void init(ScreenEvent.Init.Post event){

  if(event.getScreen() instanceof TitleScreen title){event.addListener(Button.builder(Component.literal("Tectonic Mountains"),b->{try{Minecraft.getInstance().setScreen(new SettingsScreen(title,WorldSettings.loadDefaults()));}catch(Exception e){error(e);}}).bounds(8,8,154,20).build());}

  if(event.getScreen() instanceof CreateWorldScreen create&&PREPARED.add(create))Minecraft.getInstance().execute(()->prepare(create));

 }

 @SuppressWarnings("unchecked") private static void prepare(CreateWorldScreen create){

  try{

   Path temp=(Path)ObfuscationReflectionHelper.findMethod(CreateWorldScreen.class,"getTempDataPackDir").invoke(create);

   // Recreate retains its copied snapshot instead of replacing it with menu defaults.

   Path target=temp.resolve(WorldPackBuilder.FOLDER);

   if(Files.exists(target.resolve("data/tectonic_mountains/world_settings.json")))return;

   var context=create.getUiState().getSettings();var registry=context.worldgenLoadContext();var ops=RegistryOps.create(JsonOps.INSTANCE,registry);

   var noise=registry.registryOrThrow(Registries.NOISE_SETTINGS).getOrThrow(NoiseGeneratorSettings.OVERWORLD);

   var dimension=registry.registryOrThrow(Registries.DIMENSION_TYPE).getOrThrow(net.minecraft.world.level.dimension.BuiltinDimensionTypes.OVERWORLD);

   Path terrain=ModList.get().getModFileById(TectonicMountains.MOD_ID).getFile().findResource("terrain");

   WorldPackBuilder.build(terrain,target,WorldSettings.loadDefaults(),NoiseGeneratorSettings.DIRECT_CODEC.encodeStart(ops,noise).getOrThrow().getAsJsonObject(),DimensionType.DIRECT_CODEC.encodeStart(ops,dimension).getOrThrow().getAsJsonObject());

   Pair<Path,PackRepository> pair=(Pair<Path,PackRepository>)ObfuscationReflectionHelper.findMethod(CreateWorldScreen.class,"getDataPackSelectionSettings",WorldDataConfiguration.class).invoke(create,context.dataConfiguration());

   PackRepository repo=pair.getSecond();repo.reload();var selected=new ArrayList<>(repo.getSelectedIds());String id="file/"+WorldPackBuilder.FOLDER;

   if(repo.getPack(id)==null)throw new IllegalStateException("World settings pack was not discovered");selected.remove(id);selected.add(id);repo.setSelected(selected);

   Consumer<WorldDataConfiguration> done=config->{

    if(!config.dataPacks().getEnabled().contains(id)){error(new IllegalStateException("World settings pack failed validation"));return;}

    var loaded=create.getUiState().getSettings();
    var configured=loaded.withSettings(loaded.options(),new net.minecraft.world.level.levelgen.WorldDimensions(loaded.datapackDimensions())).withDataConfiguration(config);
    create.getUiState().setSettings(configured);Minecraft.getInstance().setScreen(create);

   };

   ObfuscationReflectionHelper.findMethod(CreateWorldScreen.class,"tryApplyNewDataPacks",PackRepository.class,boolean.class,Consumer.class).invoke(create,repo,true,done);

  }catch(Exception e){error(e);}

 }

 private static void error(Exception e){Minecraft.getInstance().setScreen(new AlertScreen(()->Minecraft.getInstance().setScreen(new TitleScreen()),Component.literal("Tectonic Mountains settings"),Component.literal("Could not prepare settings: "+e.getMessage())));}

}

