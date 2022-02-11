package ru.aiefu.fabricrestart.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.aiefu.fabricrestart.FabricRestart;
import ru.aiefu.fabricrestart.PlayerCountTracker;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerMixins {
    @Inject(method = "disconnect", at =@At("HEAD"))
    private void captureTime(Component component, CallbackInfo ci){
        PlayerCountTracker t = FabricRestart.tracker;
        if(t != null ){
            t.setTargetTime(System.currentTimeMillis());
        }
    }
}
