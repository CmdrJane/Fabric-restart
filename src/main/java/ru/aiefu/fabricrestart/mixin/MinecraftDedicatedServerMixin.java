package ru.aiefu.fabricrestart.mixin;

import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.aiefu.fabricrestart.FabricRestart;

@Mixin(MinecraftDedicatedServer.class)
public class MinecraftDedicatedServerMixin {
    @Inject(method = "exit", at =@At("TAIL"))
    private void runRestartScript(CallbackInfo ci){
        if(FabricRestart.shouldRestart){
            try {
                new ProcessBuilder(FabricRestart.pathToScript).start();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
