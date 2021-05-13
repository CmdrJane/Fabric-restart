package ru.aiefu.fabricrestart.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import ru.aiefu.fabricrestart.FabricRestart;

public class RestartCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher){
        dispatcher.register(CommandManager.literal("restart").requires(source -> source.hasPermissionLevel(4)).executes(context ->
                execute(context.getSource())));
    }
    private static int execute(ServerCommandSource source){
        FabricRestart.shouldRestart = true;
        source.getMinecraftServer().stop(false);
        return 0;
    }
}
