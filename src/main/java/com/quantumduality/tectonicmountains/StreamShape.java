package com.quantumduality.tectonicmountains;

import java.util.*;



/** Shared deterministic geometry for source ponds, banks and differently sized waterways. */

public final class StreamShape {

 public record Style(int tier,int radius,int depth,int pondRadius) {}

 public record Column(int x,int z,int waterY,int depth,boolean wet,boolean pond,boolean sand,double bankBlend,double distance) {}

 public static Style style(long seed,int cx,int cz){return style(seed,cx,cz,new WorldSettings());}

 public static Style style(long seed,int cx,int cz,WorldSettings settings) {

  long h=StreamPlanner.hash(seed^94513,cx,cz);int roll=(int)Math.floorMod(h,100);

  long pondHash=StreamPlanner.hash(seed^782379,cx,cz);int pondRoll=(int)Math.floorMod(pondHash,100);

  int radius=pondRoll<55?5+(int)Math.floorMod(pondHash>>>8,4):pondRoll<90?10+(int)Math.floorMod(pondHash>>>8,6):20+(int)Math.floorMod(pondHash>>>8,6);

  int tier=roll<55?0:roll<90?1:2;int channel=tier==0?1:tier==1?2:5;

  return new Style(tier,Math.max(1,Math.min(10,(int)Math.round(channel*settings.riverWidth))),tier+2,Math.max(3,Math.min(25,(int)Math.round(radius*settings.pondSize))));

 }

 public static Map<Long,Column> columns(long seed,int cx,int cz,List<StreamPlanner.Node> nodes,int minX,int minZ,int maxX,int maxZ) {

  return columns(seed,cx,cz,nodes,minX,minZ,maxX,maxZ,new WorldSettings());

 }

 public static Map<Long,Column> columns(long seed,int cx,int cz,List<StreamPlanner.Node> nodes,int minX,int minZ,int maxX,int maxZ,WorldSettings settings) {

  var out=new HashMap<Long,Column>();if(nodes.isEmpty())return out;var style=style(seed,cx,cz,settings);var start=nodes.get(0);

  // An asymmetric basin cut into terrain, with a flat water surface and shallow sand shelves.

  int radius=style.pondRadius();

  for(int dx=-radius-9;dx<=radius+9;dx++)for(int dz=-radius-9;dz<=radius+9;dz++) {

   double warpX=noise(seed^421,start.x()+dx,start.z()+dz,11);

   double warpZ=noise(seed^422,start.x()+dx,start.z()+dz,13);

   double nx=(dx+warpX)/(double)radius,nz=(dz+warpZ)/(radius*.8),angle=Math.atan2(nz,nx);

   double phase=Math.floorMod(StreamPlanner.hash(seed,cx,cz),17);

   double edge=.9+.035*Math.sin(angle*3+phase)+.02*Math.cos(angle*5-phase);

   double dist=Math.sqrt(nx*nx+nz*nz)/edge;

   double shore=1+8.0/radius;

   if(dist>shore)continue;boolean wet=dist<=1;

   int centerDepth=(int)Math.round((radius<10?5:radius<20?8:12)*settings.pondDepth);

   int depth=dist>.8?1:2+(int)(Math.max(0,1-dist/.8)*(centerDepth-2));

   double blend=Math.max(0,Math.min(1,(shore-dist)/(shore-1)));blend=blend*blend*(3-2*blend);

   // Curving sediment arcs rather than a quadrant or square hash cells.

   boolean sand=dist>.55 && Math.sin(angle*2+phase)+.35*noise(seed^981,start.x()+dx,start.z()+dz,7)>-.2;

   put(out,new Column(start.x()+dx,start.z()+dz,start.waterY(),depth,wet,true,sand,blend,dist),minX,minZ,maxX,maxZ);

  }

  for(int i=0;i<nodes.size()-1;i++) {

   var a=nodes.get(i);var b=nodes.get(i+1);

   var before=nodes.get(Math.max(0,i-1));var after=nodes.get(Math.min(nodes.size()-1,i+2));

   // Sample a rounded centerline, then project columns onto each small segment.

   for(int sample=0;sample<6;sample++) {

    double t0=sample/6.0,t1=(sample+1)/6.0;

    double ax=curve(before.x(),a.x(),b.x(),after.x(),t0),az=curve(before.z(),a.z(),b.z(),after.z(),t0);

    double bx=curve(before.x(),a.x(),b.x(),after.x(),t1),bz=curve(before.z(),a.z(),b.z(),after.z(),t1);

    double midX=(ax+bx)*.5,midZ=(az+bz)*.5;

    double width=style.radius()+.75+.5*noise(seed^851,midX,midZ,17);

    double apron=7+style.radius()*1.2;

    int x0=Math.max(minX,(int)Math.floor(Math.min(ax,bx)-width-apron)),x1=Math.min(maxX-1,(int)Math.ceil(Math.max(ax,bx)+width+apron));

    int z0=Math.max(minZ,(int)Math.floor(Math.min(az,bz)-width-apron)),z1=Math.min(maxZ-1,(int)Math.ceil(Math.max(az,bz)+width+apron));

    double vx=bx-ax,vz=bz-az,length=vx*vx+vz*vz;

    for(int x=x0;x<=x1;x++)for(int z=z0;z<=z1;z++) {

     double t=length<1e-9?0:Math.max(0,Math.min(1,((x-ax)*vx+(z-az)*vz)/length));

     double dx=x-(ax+vx*t),dz=z-(az+vz*t),dist=Math.sqrt(dx*dx+dz*dz);

     if(dist>width+apron)continue;

     double along=t0+(t1-t0)*t;

     int water=a.waterY()+(int)Math.floor((b.waterY()-a.waterY())*along);

     boolean wet=dist<=width;

     double profile=Math.max(0,1-Math.pow(dist/width,1.6));

     int depth=Math.max(1,(int)Math.round(1+(style.depth()-1)*profile+.35*noise(seed^852,x,z,9)*profile));

     double blend=wet?1:Math.max(0,1-(dist-width)/apron);blend=blend*blend*(3-2*blend);

     put(out,new Column(x,z,water,depth,wet,false,false,blend,dist/width),minX,minZ,maxX,maxZ);

    }

   }

  }

  return out;

 }

 private static double curve(double p0,double p1,double p2,double p3,double t) {

  return .5*((2*p1)+(-p0+p2)*t+(2*p0-5*p1+4*p2-p3)*t*t+(-p0+3*p1-3*p2+p3)*t*t*t);

 }

 // Smooth world-coordinate value noise; interpolation hides lattice/material patch boundaries.

 public static double noise(long seed,double x,double z,int scale) {

  double fx=x/scale,fz=z/scale;int ix=(int)Math.floor(fx),iz=(int)Math.floor(fz);

  double tx=fx-ix,tz=fz-iz;tx=tx*tx*(3-2*tx);tz=tz*tz*(3-2*tz);

  double a=value(seed,ix,iz),b=value(seed,ix+1,iz),c=value(seed,ix,iz+1),d=value(seed,ix+1,iz+1);

  return (a+(b-a)*tx)*(1-tz)+(c+(d-c)*tx)*tz;

 }

 private static double value(long seed,int x,int z){return (StreamPlanner.hash(seed,x,z)>>>11)*0x1.0p-53*2-1;}

 public static void merge(Map<Long,Column> out,Column col) {

  long key=((long)col.x()<<32)^(col.z()&0xffffffffL);Column old=out.get(key);

  if(old==null||(col.wet()&&!old.wet())||(col.wet()==old.wet()&&col.pond()&&!old.pond())||

    (col.wet()==old.wet()&&col.pond()==old.pond()&&(col.distance()<old.distance()-1e-8 || Math.abs(col.distance()-old.distance())<1e-8 && col.waterY()<old.waterY())))out.put(key,col);

 }

 private static void put(Map<Long,Column> out,Column col,int minX,int minZ,int maxX,int maxZ) {

  if(col.x()>=minX&&col.x()<maxX&&col.z()>=minZ&&col.z()<maxZ)merge(out,col);

 }

}

