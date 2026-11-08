package com.quantumduality.tectonicmountains.mixin;
import com.quantumduality.tectonicmountains.TectonicMountains;
import com.quantumduality.tectonicmountains.WorldPackBuilder;
import java.util.*;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Required terrain follows other built-ins; the world snapshot follows terrain. */
@Mixin(PackRepository.class)
public abstract class PackPriorityMixin {
 @Inject(method="rebuildSelected",at=@At("RETURN"),cancellable=true)
 private void tm$order(Collection<String> ids, CallbackInfoReturnable<List<Pack>> cir){
  List<Pack> packs=new ArrayList<>(cir.getReturnValue());
  Pack terrain=null,world=null;
  for(Pack pack:packs){if(pack.getId().equals(TectonicMountains.TERRAIN_PACK))terrain=pack;if(pack.getId().equals("file/"+WorldPackBuilder.FOLDER))world=pack;}
  if(terrain==null)return;
  packs.remove(terrain);packs.add(terrain);
  if(world!=null){packs.remove(world);packs.add(world);}
  cir.setReturnValue(List.copyOf(packs));
 }
}
