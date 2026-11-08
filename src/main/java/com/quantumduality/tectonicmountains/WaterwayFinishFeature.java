package com.quantumduality.tectonicmountains;

import net.minecraft.world.level.levelgen.feature.*;

import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** Runs after ordinary vegetation/decorations without changing the channel bed or banks. */

public final class WaterwayFinishFeature extends Feature<NoneFeatureConfiguration> {

 public WaterwayFinishFeature(){super(NoneFeatureConfiguration.CODEC);}

 @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {

  return StreamClearance.finishGeneration(context.level(),context.origin().getX()&~15,context.origin().getZ()&~15);

 }

}

