package com.quantumduality.tectonicmountains;



import java.util.*;

import net.minecraft.core.BlockPos;

import net.minecraft.tags.BlockTags;

import net.minecraft.world.level.block.Blocks;

import net.minecraft.world.level.material.Fluids;

import net.minecraft.world.level.levelgen.Heightmap;

import net.minecraft.world.level.levelgen.feature.*;

import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;





import org.slf4j.LoggerFactory;



/** Carved multi-chunk streams. The registry id is kept for development-world compatibility. */

public final class MountainSpringFeature extends Feature<NoneFeatureConfiguration> {

 private final Map<Object,Map<Long,List<StreamPlanner.Node>>> worlds=Collections.synchronizedMap(new WeakHashMap<>());

 public MountainSpringFeature(){super(NoneFeatureConfiguration.CODEC);}

 @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {

  var world=context.level();var server=world.getLevel();var settings=WorldSettingsStore.forWorld(server);if(!settings.streams)return false;long seed=world.getSeed();

  int chunkX=context.origin().getX()&~15,chunkZ=context.origin().getZ()&~15;

  Map<Long,List<StreamPlanner.Node>> cache;

  synchronized(worlds){cache=worlds.computeIfAbsent(server,k->Collections.synchronizedMap(new LinkedHashMap<>(256,.75f,true){

   @Override protected boolean removeEldestEntry(Map.Entry<Long,List<StreamPlanner.Node>> e){return size()>512;}

  }));}

  var columns=new HashMap<Long,StreamShape.Column>();

  int radius=StreamPlanner.REACH;

  for(int cx=Math.floorDiv(chunkX-radius,256);cx<=Math.floorDiv(chunkX+15+radius,256);cx++)

  for(int cz=Math.floorDiv(chunkZ-radius,256);cz<=Math.floorDiv(chunkZ+15+radius,256);cz++) {

   long key=((long)cx<<32)^(cz&0xffffffffL);List<StreamPlanner.Node> nodes=cache.get(key);

   if(nodes==null){

    var heights=new HashMap<Long,Integer>();

    nodes=StreamPlanner.plan(seed,cx,cz,(x,z)->heights.computeIfAbsent(((long)x<<32)^(z&0xffffffffL),k->

     context.chunkGenerator().getBaseHeight(x,z,Heightmap.Types.OCEAN_FLOOR_WG,world,server.getChunkSource().randomState())),settings);

    cache.put(key,nodes);

   }

   if(nodes.isEmpty())continue;

   for(var col:StreamShape.columns(seed,cx,cz,nodes,chunkX,chunkZ,chunkX+16,chunkZ+16,settings).values())StreamShape.merge(columns,col);

   var start=nodes.get(0);

   if(start.x()>=chunkX&&start.x()<chunkX+16&&start.z()>=chunkZ&&start.z()<chunkZ+16)

    LoggerFactory.getLogger(TectonicMountains.MOD_ID).info("CARVED STREAM source x={} y={} z={}, route {} blocks",start.x(),start.waterY(),start.z(),(nodes.size()-1)*3);

  }

  // Only own-chunk writes; adjacent chunks independently reconstruct identical routes.

  for(StreamShape.Column col:columns.values()) {

   int top=world.getHeight(Heightmap.Types.WORLD_SURFACE_WG,col.x(),col.z());

   int floor=col.waterY()-col.depth();

   int nativeGround=!col.wet()?context.chunkGenerator().getBaseHeight(col.x(),col.z(),Heightmap.Types.OCEAN_FLOOR_WG,world,server.getChunkSource().randomState()):top;

   int bankTop=col.waterY()+1+(int)Math.round(Math.max(0,nativeGround-col.waterY()-1)*(1-col.bankBlend()));

   // Dry banks may be cut down, but never raised above pre-existing ground.

   if(!col.wet())bankTop=Math.min(bankTop,nativeGround-1);

   if(!col.wet()&&col.bankBlend()<.08)continue;

   double sediment=StreamShape.noise(seed^711,col.x(),col.z(),9);

   var material=col.sand()?Blocks.SAND:sediment>.3?Blocks.GRAVEL:Blocks.ANDESITE;

   var bank=col.sand()?Blocks.SAND:sediment<-.6?Blocks.STONE:sediment>.65?Blocks.GRAVEL:Blocks.DIRT;

   var originalSurface=world.getBlockState(new BlockPos(col.x(),nativeGround-1,col.z()));

   boolean nativeSoil=originalSurface.is(Blocks.GRASS_BLOCK)||originalSurface.is(Blocks.PODZOL)||originalSurface.is(Blocks.COARSE_DIRT)||originalSurface.is(Blocks.DIRT)||originalSurface.is(Blocks.MOSS_BLOCK);

   // Anchor the bed into real terrain instead of leaving a thin shell over a cave.

   int solidRun=0;

   for(int y=floor-1;y>=world.getMinBuildHeight()&&solidRun<3;y--) {

    var support=new BlockPos(col.x(),y,col.z());var state=world.getBlockState(support);

    if(state.isSolidRender(world,support))solidRun++;

    else {solidRun=0;world.setBlock(support,Blocks.STONE.defaultBlockState(),2);}

   }

   for(int y=floor-1;y<=Math.max(top+2,col.waterY()+3);y++) {

    BlockPos pos=new BlockPos(col.x(),y,col.z());

    if(y<=floor)world.setBlock(pos,(y==floor?material:Blocks.STONE).defaultBlockState(),2);

    else if(col.wet()&&y<=col.waterY())world.setBlock(pos,Blocks.WATER.defaultBlockState(),2);

    else if(!col.wet()&&y<=bankTop) {

     if(y<bankTop-2) {

      if(!world.getBlockState(pos).isSolidRender(world,pos))world.setBlock(pos,Blocks.STONE.defaultBlockState(),2);

     } else if(bank==Blocks.DIRT&&y==bankTop) {

      world.setBlock(pos,nativeSoil?originalSurface:Blocks.GRASS_BLOCK.defaultBlockState(),2);

     } else world.setBlock(pos,bank.defaultBlockState(),2);

    }

    else world.setBlock(pos,Blocks.AIR.defaultBlockState(),2);

   }

   if(col.wet())world.scheduleTick(new BlockPos(col.x(),col.waterY(),col.z()),Fluids.WATER,1);

  }

  // Basins are carved before trees; late cleanup is limited to vegetation over wet columns.

  StreamClearance.schedule(server,chunkX,chunkZ,columns.values());

  return !columns.isEmpty();

 }

}

