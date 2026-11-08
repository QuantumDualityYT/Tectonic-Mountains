package com.quantumduality.tectonicmountains;
import com.quantumduality.tectonicmountains.mixin.CreateWorldAccess;











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



import com.mojang.serialization.JsonOps;

import com.mojang.datafixers.util.Pair;

import java.nio.file.*;

import java.util.*;

import java.util.function.Consumer;





public final class ClientSettingsHooks {

 private static final Set<CreateWorldScreen> PREPARED=Collections.newSetFromMap(new WeakHashMap<>());

 public static void init(net.minecraft.client.gui.screens.Screen screen){

  if(screen instanceof TitleScreen title){net.fabricmc.fabric.api.client.screen.v1.Screens.getButtons(title).add(Button.builder(Component.literal("Tectonic Mountains"),b->{try{Minecraft.getInstance().setScreen(new SettingsScreen(title,WorldSettings.loadDefaults()));}catch(Exception e){error(e);}}).bounds(8,8,154,20).build());}

  if(screen instanceof CreateWorldScreen create&&PREPARED.add(create))Minecraft.getInstance().execute(()->prepare(create));

 }

 @SuppressWarnings("unchecked") private static void prepare(CreateWorldScreen create){

  try{

   Path temp=((CreateWorldAccess)create).tm$temp();

   // Recreate retains its copied snapshot instead of replacing it with menu defaults.

   Path target=temp.resolve(WorldPackBuilder.FOLDER);

   if(Files.exists(target.resolve("data/tectonic_mountains/world_settings.json")))return;

   var context=create.getUiState().getSettings();var registry=context.worldgenLoadContext();var ops=RegistryOps.create(JsonOps.INSTANCE,registry);

   var noise=registry.registryOrThrow(Registries.NOISE_SETTINGS).getOrThrow(NoiseGeneratorSettings.OVERWORLD);

   var dimension=registry.registryOrThrow(Registries.DIMENSION_TYPE).getOrThrow(net.minecraft.world.level.dimension.BuiltinDimensionTypes.OVERWORLD);

   Path terrain=net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer(TectonicMountains.MOD_ID).orElseThrow().findPath("resourcepacks/terrain").orElseThrow();

   WorldPackBuilder.build(terrain,target,WorldSettings.loadDefaults(),NoiseGeneratorSettings.DIRECT_CODEC.encodeStart(ops,noise).getOrThrow(false,s->{}).getAsJsonObject(),DimensionType.DIRECT_CODEC.encodeStart(ops,dimension).getOrThrow(false,s->{}).getAsJsonObject());

   Pair<Path,PackRepository> pair=((CreateWorldAccess)create).tm$selection(context.dataConfiguration());

   PackRepository repo=pair.getSecond();repo.reload();var selected=new ArrayList<>(repo.getSelectedIds());String id="file/"+WorldPackBuilder.FOLDER;

   if(repo.getPack(id)==null)throw new IllegalStateException("World settings pack was not discovered");selected.remove(id);selected.add(id);repo.setSelected(selected);

   Consumer<WorldDataConfiguration> done=config->{

    if(!config.dataPacks().getEnabled().contains(id)){error(new IllegalStateException("World settings pack failed validation"));return;}

    var loaded=create.getUiState().getSettings();
    var configured=new net.minecraft.client.gui.screens.worldselection.WorldCreationContext(loaded.options(), loaded.datapackDimensions(), new net.minecraft.world.level.levelgen.WorldDimensions(loaded.datapackDimensions()), loaded.worldgenRegistries(), loaded.dataPackResources(), config);
    create.getUiState().setSettings(configured);Minecraft.getInstance().setScreen(create);

   };

   ((CreateWorldAccess)create).tm$apply(repo,true,done);

  }catch(Exception e){error(e);}

 }

 private static void error(Exception e){Minecraft.getInstance().setScreen(new AlertScreen(()->Minecraft.getInstance().setScreen(new TitleScreen()),Component.literal("Tectonic Mountains settings"),Component.literal("Could not prepare settings: "+e.getMessage())));}

}

