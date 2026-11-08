package com.quantumduality.tectonicmountains;

import java.util.*;

import java.util.concurrent.*;

import net.minecraft.core.BlockPos;

import net.minecraft.tags.BlockTags;

import net.minecraft.server.level.ServerLevel;

import net.minecraft.world.level.levelgen.Heightmap;

import net.minecraft.world.level.block.Blocks;

import net.neoforged.neoforge.event.tick.LevelTickEvent;



/** Bounded cleanup of newly generated water only; no recurring edits on world reload. */

public final class StreamClearance {

 private record Pending(int chunkX,int chunkZ,long due,int pass,long[] water) {}

 private static final Map<ServerLevel,Map<Long,long[]>> FOOTPRINTS=Collections.synchronizedMap(new WeakHashMap<>());

 private static final Map<ServerLevel,Queue<Pending>> QUEUES=Collections.synchronizedMap(new WeakHashMap<>());

 public static void schedule(ServerLevel world,int cx,int cz,Collection<StreamShape.Column> columns) {

  long[] water=columns.stream().filter(StreamShape.Column::wet).mapToLong(c->BlockPos.asLong(c.x(),c.waterY(),c.z())).toArray();

  if(water.length==0)return;

  synchronized(FOOTPRINTS){FOOTPRINTS.computeIfAbsent(world,k->new ConcurrentHashMap<>()).put(((long)cx<<32)^(cz&0xffffffffL),water);}

  synchronized(QUEUES){QUEUES.computeIfAbsent(world,k->new ConcurrentLinkedQueue<>()).add(new Pending(cx,cz,world.getGameTime()+20,0,water));}

 }

 public static boolean finishGeneration(net.minecraft.world.level.WorldGenLevel world,int cx,int cz) {

  long[] water; synchronized(FOOTPRINTS){var map=FOOTPRINTS.get(world.getLevel());water=map==null?null:map.get(((long)cx<<32)^(cz&0xffffffffL));}

  if(water==null)return false;

  for(long packed:water) {

   var surface=BlockPos.of(packed);int top=world.getHeight(Heightmap.Types.WORLD_SURFACE_WG,surface.getX(),surface.getZ());

   for(int y=surface.getY();y<=top;y++) {

    var pos=new BlockPos(surface.getX(),y,surface.getZ());

    if(WaterwayObstructions.removable(world.getBlockState(pos)))world.setBlock(pos,(y==surface.getY()?Blocks.WATER:Blocks.AIR).defaultBlockState(),2);

   }

  }

  return true;

 }

 public static void tick(LevelTickEvent.Post event) {

  if(!(event.getLevel() instanceof ServerLevel world))return;

  Queue<Pending> queue; synchronized(QUEUES){queue=QUEUES.get(world);}if(queue==null)return;

  int count=queue.size();long now=world.getGameTime();int budget=2;

  for(int i=0;i<count;i++) {

   Pending p=queue.poll();if(p==null)break;

   if(now<p.due()||budget==0){queue.add(p);continue;}

   // Never load or generate chunks for cleanup.

   if(world.getChunkSource().getChunkNow(p.chunkX()>>4,p.chunkZ()>>4)==null){

    if(now-p.due()<1200)queue.add(p);

    else synchronized(FOOTPRINTS){var map=FOOTPRINTS.get(world);if(map!=null)map.remove(((long)p.chunkX()<<32)^(p.chunkZ()&0xffffffffL));}

    continue;

   }

   budget--;

   for(long packed:p.water()) {

    BlockPos water=BlockPos.of(packed);int top=world.getHeight(Heightmap.Types.WORLD_SURFACE,water.getX(),water.getZ());

    for(int y=water.getY();y<=top;y++) {

     var pos=new BlockPos(water.getX(),y,water.getZ());

     var state=world.getBlockState(pos);

     if(WaterwayObstructions.removable(state))world.setBlock(pos,(y==water.getY()?Blocks.WATER:Blocks.AIR).defaultBlockState(),2);

    }

   }

   if(p.pass()<2)queue.add(new Pending(p.chunkX(),p.chunkZ(),now+(p.pass()==0?60:120),p.pass()+1,p.water()));

   else synchronized(FOOTPRINTS){var map=FOOTPRINTS.get(world);if(map!=null)map.remove(((long)p.chunkX()<<32)^(p.chunkZ()&0xffffffffL));}

  }

 }

}

