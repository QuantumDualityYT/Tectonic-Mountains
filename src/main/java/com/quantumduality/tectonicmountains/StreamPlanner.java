package com.quantumduality.tectonicmountains;

import java.util.*;



/** World-coordinate route planning: no chunk loads, writes or generation-order state. */

public final class StreamPlanner {

 public static final int CELL=256, STEP=3, STEPS=40, REACH=STEP*STEPS+72;

 public interface HeightSampler { int height(int x,int z); }

 public record Node(int x,int z,int waterY) {}

 public static long hash(long seed,int x,int z) {

  long n=seed^((long)x*341873128712L)^((long)z*132897987541L);

  n=(n^(n>>>30))*0xbf58476d1ce4e5b9L; n=(n^(n>>>27))*0x94d049bb133111ebL;return n^(n>>>31);

 }

 public static List<Node> plan(long seed,int cellX,int cellZ,HeightSampler height) {

  return plan(seed,cellX,cellZ,height,new WorldSettings());

 }

 public static List<Node> plan(long seed,int cellX,int cellZ,HeightSampler height,WorldSettings settings) {

  if(!settings.streams)return List.of();

  long h=hash(seed,cellX,cellZ); if(Math.floorMod(h,3)!=0)return List.of();

  int x=cellX*CELL+64+(int)Math.floorMod(h>>>8,128),z=cellZ*CELL+64+(int)Math.floorMod(h>>>24,128);

  int y=height.height(x,z)-2;

  if(y<100||y>225)return List.of();

  // Select a low outlet around the whole basin, rather than using the hilltop center.

  int pondRadius=StreamShape.style(seed,cellX,cellZ,settings).pondRadius();

  int outletX=x,outletZ=z,low=height.height(x,z),high=low;

  for(int ring=1;ring<=3;ring++)for(int i=0;i<20;i++) {

   double angle=i*Math.PI*2/20;double radius=(pondRadius+2)*ring/3.0;

   int px=x+(int)Math.round(Math.cos(angle)*radius),pz=z+(int)Math.round(Math.sin(angle)*radius*.8);

   int ground=height.height(px,pz);high=Math.max(high,ground);

   if(ground<low){low=ground;outletX=px;outletZ=pz;}

  }

  // The water sits below even the low bank; reject steep sites instead of building a dam.

  if(high-low>14 || outletX==x&&outletZ==z)return List.of();

  y=low-3;if(y<80)return List.of();

  int centerX=x,centerZ=z;

  var nodes=new ArrayList<Node>();var visited=new HashSet<Long>();nodes.add(new Node(x,z,y));

  int connectionSteps=(int)Math.ceil(Math.max(Math.abs(outletX-x),Math.abs(outletZ-z))/(double)STEP);

  for(int i=1;i<=connectionSteps;i++) {

   int px=centerX+(int)Math.round((outletX-centerX)*i/(double)connectionSteps);

   int pz=centerZ+(int)Math.round((outletZ-centerZ)*i/(double)connectionSteps);

   visited.add(((long)x<<32)^(z&0xffffffffL));nodes.add(new Node(px,pz,y));x=px;z=pz;

  }

  int startY=y,prevDx=0,prevDz=0;

  for(int step=0;step<STEPS;step++) {

   visited.add(((long)x<<32)^(z&0xffffffffL));

   int bestX=x,bestZ=z,bestY=y;double bestScore=-Double.MAX_VALUE;

   for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++) {

    if(dx==0&&dz==0)continue;int nx=x+dx*STEP,nz=z+dz*STEP;

    if(visited.contains(((long)nx<<32)^(nz&0xffffffffL)))continue;

    int groundHeight=height.height(nx,nz);

    // A shallow cut can pass a small rise after the low pond outlet. Requiring every

    // raw surface sample to descend rejected almost all real-world outlets.

    if(groundHeight>y+6||groundHeight<y-1)continue;

    int ny=Math.min(y,groundHeight-3),drop=y-ny;

    if(drop>2)continue;

    double turn=(dx==prevDx&&dz==prevDz)?0.25:0;

    double outward=(Math.hypot(nx-centerX,nz-centerZ)-Math.hypot(x-centerX,z-centerZ))*.12;

    double score=drop+turn+outward-(groundHeight-y)*.12+(Math.floorMod(hash(seed,nx,nz),1000)/10000.0);

    if(score>bestScore){bestScore=score;bestX=nx;bestZ=nz;bestY=ny;}

   }

   if(bestScore==-Double.MAX_VALUE)break;

   prevDx=(bestX-x)/STEP;prevDz=(bestZ-z)/STEP;

   // Reject routes that would require suspended water or a tall retaining wall.

   boolean supported=true;

   int support=StreamShape.style(seed,cellX,cellZ,settings).radius()+2;

   for(int dx=-support;dx<=support;dx+=support)for(int dz=-support;dz<=support;dz+=support)

    {int ground=height.height(bestX+dx,bestZ+dz)-1;

     if(ground<bestY-1||ground-bestY>12)supported=false;}

   if(!supported)break;

   x=bestX;z=bestZ;y=bestY;nodes.add(new Node(x,z,y));if(y<68)break;

  }

  return nodes.size()>=10&&startY-y>=3?List.copyOf(nodes):List.of();

 }

}



