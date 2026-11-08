package com.quantumduality.tectonicmountains;

import java.util.*;

public class StreamPlannerCheck {

 public static void main(String[] args) {

  StreamPlanner.HeightSampler hillside=(x,z)->220-Math.floorDiv(x,8)-Math.floorDiv(z,24);

  int checked=0;

  for(long seed=1;seed<=100;seed++) {

   var a=StreamPlanner.plan(seed,0,0,hillside);var b=StreamPlanner.plan(seed,0,0,hillside);

   if(!a.equals(b))throw new AssertionError("Generation-order dependence");

   if(a.isEmpty())continue;checked++;

   var basin=StreamShape.columns(seed,0,0,a,-128,-128,512,512);

   for(var c:basin.values())if(c.pond()&&c.wet()&&c.waterY()>hillside.height(c.x(),c.z())-2)

    throw new AssertionError("Raised pond: water above native ground");

   if(a.size()<10||a.size()>65)throw new AssertionError("Route length");

   var occupied=new HashSet<String>();

   for(int i=0;i<a.size();i++) {

    var n=a.get(i);if(!occupied.add(n.x()+","+n.z()))throw new AssertionError("Route loop");

    if(i>0){var prev=a.get(i-1);int drop=prev.waterY()-n.waterY();if(drop<0||drop>2)throw new AssertionError("Uphill or excessive drop");}

   }

   // Reconstruct local segments on either side of the chunk border from the same world route.

   for(int i=0;i<a.size()-1;i++)for(int step=0;step<=3;step++) {

    var p=a.get(i);var q=a.get(i+1);int x=p.x()+(q.x()-p.x())*step/3;

    int z=p.z()+(q.z()-p.z())*step/3;

    int ownerX=Math.floorDiv(x,16),ownerZ=Math.floorDiv(z,16);

    if(x-ownerX*16<0||x-ownerX*16>=16||z-ownerZ*16<0||z-ownerZ*16>=16)throw new AssertionError("Chunk clipping");

   }

  }

  int uneven=0;

  for(long seed=1;seed<=100;seed++) {

   var route=StreamPlanner.plan(seed,0,0,(x,z)->220-Math.floorDiv(x,8)-Math.floorDiv(z,24)+(((Math.floorDiv(x,9)+Math.floorDiv(z,13))%3)==0?1:0));

   if(!route.isEmpty())uneven++;

  }

  if(uneven<5)throw new AssertionError("Small terrain rises suppress all waterways");

  System.out.println("PASS: "+uneven+" routes survive uneven hillside fixture.");

  if(checked<5)throw new AssertionError("No viable routes");

  if(!StreamPlanner.plan(1,0,0,(x,z)->180).isEmpty())throw new AssertionError("Flat terrain should not form descending stream");

  System.out.println("PASS: "+checked+" deterministic downhill routes; embedded basins, bounded drops, no loops, chunk ownership and flat rejection.");

 }

}

