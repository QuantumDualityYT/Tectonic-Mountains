package com.quantumduality.tectonicmountains;

import java.util.*;

public class StreamShapeCheck {

 public static void main(String[] args) {

  var route=List.of(new StreamPlanner.Node(-7,9,150),new StreamPlanner.Node(-4,9,149),new StreamPlanner.Node(-1,12,148),new StreamPlanner.Node(2,15,148),new StreamPlanner.Node(5,18,147));

  Set<Integer> tiers=new HashSet<>(); Set<Integer> pondClasses=new HashSet<>(); boolean found50=false,foundDeep=false;

  for(long seed=0;seed<100;seed++) {

   var style=StreamShape.style(seed,0,0);tiers.add(style.tier());

   pondClasses.add(style.pondRadius()<10?0:style.pondRadius()<20?1:2);

   if(style.pondRadius()==25)found50=true;

   var whole=StreamShape.columns(seed,0,0,route,-64,-64,64,64);

   var tiled=new HashMap<Long,StreamShape.Column>();

   for(int x=-64;x<64;x+=16)for(int z=-64;z<64;z+=16)tiled.putAll(StreamShape.columns(seed,0,0,route,x,z,x+16,z+16));

   if(!whole.equals(tiled))throw new AssertionError("Chunk boundary changes geometry");

   if(whole.values().stream().anyMatch(c->c.pond()&&c.wet()&&c.depth()>style.depth()+1))foundDeep=true;

   var pond=whole.values().stream().filter(c->c.pond()&&c.wet()).toList();

   int span=pond.stream().mapToInt(StreamShape.Column::x).max().orElseThrow()-pond.stream().mapToInt(StreamShape.Column::x).min().orElseThrow()+1;

   if(span>50)throw new AssertionError("Pond exceeds 50-block cap");

   long sand=whole.values().stream().filter(c->c.pond()&&c.sand()).count();

   if(sand<10)throw new AssertionError("Missing sand deposit");

   if(whole.values().stream().noneMatch(c->c.pond()&&c.wet()))throw new AssertionError("No source pond");

   if(whole.values().stream().anyMatch(c->c.pond()&&c.wet()&&c.waterY()!=150))throw new AssertionError("Pond is not level");

  }

  if(pondClasses.size()!=3||!found50||!foundDeep)throw new AssertionError("Missing pond sizes or deeper basins");

  for(int boundary=-90;boundary<=90;boundary+=9) {

   double left=StreamShape.noise(91,boundary-.00001,7,9),right=StreamShape.noise(91,boundary+.00001,7,9);

   if(!Double.isFinite(left)||Math.abs(left-right)>.0001)throw new AssertionError("Material noise seam");

  }

  if(!WaterwayObstructions.rockName("minecraft:mossy_cobblestone")||!WaterwayObstructions.rockName("minecraft:andesite"))throw new AssertionError("Boulder debris omitted");

  if(WaterwayObstructions.rockName("minecraft:water")||WaterwayObstructions.rockName("minecraft:sand")||WaterwayObstructions.rockName("minecraft:chest"))throw new AssertionError("Cleanup filter too broad");

  var overlap=new HashMap<Long,StreamShape.Column>();

  var near=new StreamShape.Column(64,0,150,3,true,false,false,1,.1);

  var distant=new StreamShape.Column(64,0,140,3,true,false,false,1,.9);

  StreamShape.merge(overlap,near);StreamShape.merge(overlap,distant);

  if(!overlap.values().iterator().next().equals(near))throw new AssertionError("Distant low section cuts shelf into nearest channel");

  var straight=new ArrayList<StreamPlanner.Node>();for(int x=0;x<=120;x+=3)straight.add(new StreamPlanner.Node(x,0,150));

  boolean profileChecked=false;

  for(long seed=0;seed<100;seed++) {

   if(StreamShape.style(seed,0,0).tier()!=2)continue;

   var section=StreamShape.columns(seed,0,0,straight,50,-24,75,24);

   var center=section.get(((long)64<<32));

   int shallow=section.values().stream().filter(c->c.x()==64&&c.wet()&&!c.pond()).mapToInt(StreamShape.Column::depth).min().orElseThrow();

   if(center.depth()<=shallow)throw new AssertionError("Box-section bed: missing shallows");

   if(section.values().stream().noneMatch(c->!c.wet()&&c.bankBlend()>0&&c.bankBlend()<.5))throw new AssertionError("Missing gradual bank apron");

   profileChecked=true;break;

  }

  if(!profileChecked)throw new AssertionError("Cross-section fixture missing");

  if(tiers.size()!=3)throw new AssertionError("Missing waterway size class");

  System.out.println("PASS: 100 pond/stream shapes; all channel/pond size classes, 50-block footprint cap, deeper basins, sand deposits, level source ponds and identical chunk-tiled geometry.");

 }

}



