package com.quantumduality.tectonicmountains;

import net.minecraft.client.gui.screens.*;

import net.minecraft.client.gui.components.*;

import net.minecraft.client.gui.GuiGraphics;

import net.minecraft.network.chat.Component;

import java.util.function.*;



/** Only reachable from the title screen. Edits are staged until Save. */

public final class SettingsScreen extends Screen {

 private final Screen parent;private WorldSettings settings;private int page=0;private String error="";

 public SettingsScreen(Screen parent,WorldSettings settings){super(Component.literal("Tectonic Mountains"));this.parent=parent;this.settings=settings.copy();}

 @Override protected void init(){

  int left=width/2-155;for(int i=0;i<3;i++){final int tab=i;addRenderableWidget(Button.builder(Component.literal(new String[]{"Landscape","Water","World"}[i]),b->{page=tab;rebuildWidgets();}).bounds(left+i*105,40,100,20).build());}

  int y=70;

  if(page==0){

   slider("Mountain width",.5,2,settings.mountainMass,v->settings.mountainMass=v,y);y+=26;

   slider("Low peak target",100,settings.mountainMax,settings.mountainMin,v->settings.mountainMin=(int)Math.round(v),y);y+=26;

   slider("High peak target",settings.mountainMin,settings.buildTop-32,settings.mountainMax,v->settings.mountainMax=Math.max(settings.mountainMin,(int)Math.round(v)),y);y+=26;

   slider("Valley spacing",.5,2,settings.valleyLength,v->settings.valleyLength=v,y);

  }else if(page==1){

   toggle("Streams",()->settings.streams,()->settings.streams=!settings.streams,y);y+=26;

   slider("River width",.5,2,settings.riverWidth,v->settings.riverWidth=v,y);y+=26;

   slider("Pond size",.5,2,settings.pondSize,v->settings.pondSize=v,y);y+=26;

   slider("Pond depth",.5,2,settings.pondDepth,v->settings.pondDepth=v,y);

  }else{

   toggle("Villages",()->settings.villages,()->settings.villages=!settings.villages,y);y+=26;

   toggle("Remove oceans",()->settings.removeOceans,()->settings.removeOceans=!settings.removeOceans,y);y+=26;

   slider("Build ceiling (Y)",320,1024,settings.buildTop,v->{settings.buildTop=16*(int)Math.round(v/16);settings.mountainMax=Math.min(settings.mountainMax,settings.buildTop-32);settings.mountainMin=Math.min(settings.mountainMin,settings.mountainMax);settings.cloudHeight=Math.min(settings.cloudHeight,settings.buildTop+256);},y);y+=26;

   slider("Cloud height (Y)",128,settings.buildTop+256,settings.cloudHeight,v->settings.cloudHeight=(int)Math.round(v),y);

  }

  addRenderableWidget(Button.builder(Component.literal("Reset"),b->{settings=new WorldSettings();error="";rebuildWidgets();}).bounds(left,height-30,90,20).build());

  addRenderableWidget(Button.builder(Component.literal("Cancel"),b->onClose()).bounds(left+110,height-30,90,20).build());

  addRenderableWidget(Button.builder(Component.literal("Save"),b->{try{settings.validate();settings.saveDefaults();onClose();}catch(Exception e){error=e.getMessage();}}).bounds(left+220,height-30,90,20).build());

 }

 private void toggle(String name,BooleanSupplier get,Runnable change,int y){addRenderableWidget(Button.builder(Component.literal(name+": "+(get.getAsBoolean()?"On":"Off")),b->{change.run();b.setMessage(Component.literal(name+": "+(get.getAsBoolean()?"On":"Off")));}).bounds(width/2-150,y,300,20).build());}

 private void slider(String name,double min,double max,double current,DoubleConsumer set,int y){

  addRenderableWidget(new AbstractSliderButton(width/2-150,y,300,20,Component.empty(),max==min?0:(current-min)/(max-min)){

   {updateMessage();}

   protected void updateMessage(){double n=min+value*(max-min);setMessage(Component.literal(name+": "+(max<=2?String.format(java.util.Locale.ROOT,"%.2fx",n):String.valueOf(name.startsWith("Build ceiling")?16*(int)Math.round(n/16):(int)Math.round(n)))));}

   protected void applyValue(){set.accept(min+value*(max-min));updateMessage();}

   @Override public void onRelease(double x,double y){super.onRelease(x,y);SettingsScreen.this.rebuildWidgets();}

  });

 }

 @Override public void onClose(){minecraft.setScreen(parent);}

 @Override public void render(GuiGraphics g,int x,int y,float partial){renderBackground(g);super.render(g,x,y,partial);g.drawCenteredString(font,title,width/2,16,0xffffff);g.drawCenteredString(font,"New worlds only. Existing worlds keep their settings.",width/2,height-65,0xdddddd);g.drawCenteredString(font,error.isEmpty()?"Peak heights are terrain targets, not hard caps.":error,width/2,height-52,error.isEmpty()?0xaaaaaa:0xff6666);}

}

