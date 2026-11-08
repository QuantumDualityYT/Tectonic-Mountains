package com.quantumduality.tectonicmountains;

import java.util.Set;

import net.minecraft.core.registries.BuiltInRegistries;

import net.minecraft.tags.BlockTags;

import net.minecraft.world.level.block.state.BlockState;



/** Limited to generated vegetation and rock debris inside the reserved open-water prism. */

public final class WaterwayObstructions {

 private static final Set<String> ROCKS=Set.of("minecraft:stone","minecraft:cobblestone","minecraft:mossy_cobblestone","minecraft:andesite","minecraft:diorite","minecraft:granite");

 public static boolean rockName(String name){return ROCKS.contains(name);}

 public static boolean removable(BlockState state){

  return state.is(BlockTags.LOGS)||state.is(BlockTags.LEAVES)||rockName(BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString())||BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString().equals("minecraft:vine");

 }

}

