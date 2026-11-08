package com.quantumduality.tectonicmountains.mixin;
import com.quantumduality.tectonicmountains.TectonicMountains;
import com.quantumduality.tectonicmountains.WorldPackBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.server.packs.repository.PackRepository;
import com.mojang.datafixers.util.Pair;
import java.nio.file.Path;
import java.util.function.Consumer;
@Mixin(CreateWorldScreen.class)
public interface CreateWorldAccess {
 @Invoker("getTempDataPackDir") Path tm$temp();
 @Invoker("getDataPackSelectionSettings") Pair<Path,PackRepository> tm$selection(WorldDataConfiguration config);
 @Invoker("tryApplyNewDataPacks") void tm$apply(PackRepository repo, boolean safe, Consumer<WorldDataConfiguration> done);
}
