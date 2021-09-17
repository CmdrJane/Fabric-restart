package ru.aiefu.fabricrestart.mixin;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.dedicated.command.StopCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.aiefu.fabricrestart.FabricRestart;

@Mixin(StopCommand.class)
public class StopCommandMixin {
    @Inject(method = "method_13676(Lcom/mojang/brigadier/context/CommandContext;)I", at = @At("HEAD"))
    private static void preventRestartScript(CommandContext<ServerCommandSource> context, CallbackInfoReturnable<Integer> cir){
        FabricRestart.CONFIG.enableRestartScript = false;
    }
}
