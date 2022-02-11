package ru.aiefu.fabricrestart;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ModCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal("restart").executes(context -> restart(context.getSource())));

        dispatcher.register(Commands.literal("restart-when").executes(context -> getTimeUntilRestart(context.getSource())));
        dispatcher.register(Commands.literal("memory-stat").requires(source -> source.hasPermission(4))
                .then(Commands.argument("type", StringArgumentType.string()).executes(context -> memoryStat(context.getSource(), StringArgumentType.getString(context,"type")))));
        dispatcher.register(Commands.literal("getTPS").executes(context -> getTPS(context.getSource())));
    }

    private static int restart(CommandSourceStack source){
        if (FabricRestart.rdata == null || !FabricRestart.rdata.isScriptEnabled()) {
            FabricRestart.registerShutdownHook();
        }
        source.getServer().halt(false);
        return 0;
    }

    private static int getTimeUntilRestart(CommandSourceStack source){
        if(FabricRestart.rdata != null)
            source.sendSuccess(new TextComponent("Restart time: " + LocalDateTime.
                    ofEpochSecond(FabricRestart.rdata.getRestartTimeNoOffset() / 1000,0, OffsetDateTime.
                            now().getOffset()).format(DateTimeFormatter.ofPattern("HH:mm"))
            ), false);
        else source.sendSuccess(new TextComponent("Auto-restart is disabled"), false);
        return 0;
    }

    private static int memoryStat(CommandSourceStack source, String arg){
        MemoryMXBean mxMem = ManagementFactory.getMemoryMXBean();
        switch (arg) {
            case "offheap" -> {
                MemoryUsage memoryUsage = mxMem.getNonHeapMemoryUsage();
                long used = memoryUsage.getUsed();
                long committed = memoryUsage.getCommitted();
                com.sun.management.OperatingSystemMXBean sys = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
                source.sendSuccess(new TextComponent("Offheap Usage: "), false);
                source.sendSuccess(new TextComponent("Used: " + formatBytesToReadable(used)), false);
                source.sendSuccess(new TextComponent("Reserved by JVM: " + formatBytesToReadable(committed)), false);
                source.sendSuccess(new TextComponent("Total memory usage exclude heap: " + formatBytesToReadable(sys.getCommittedVirtualMemorySize() - mxMem.getHeapMemoryUsage().getCommitted())), false);
            }
            case "heap" -> {
                MemoryUsage memoryUsage = mxMem.getHeapMemoryUsage();
                long used = memoryUsage.getUsed();
                long committed = memoryUsage.getCommitted();
                long max = memoryUsage.getMax();
                com.sun.management.OperatingSystemMXBean sys = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
                source.sendSuccess(new TextComponent("Heap Usage: "), false);
                source.sendSuccess(new TextComponent("Used: " + formatBytesToReadable(used)), false);
                source.sendSuccess(new TextComponent("Reserved by JVM: " + formatBytesToReadable(committed)), false);
                source.sendSuccess(new TextComponent("Maximum Possible: " + formatBytesToReadable(max)), false);
                source.sendSuccess(new TextComponent("Total JVM Process Consumption: " + formatBytesToReadable(sys.getCommittedVirtualMemorySize())), false);
            }
            case "system" -> {
                com.sun.management.OperatingSystemMXBean sys = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
                long totalMemory = sys.getTotalMemorySize();
                long freeMemory = sys.getFreeMemorySize();
                source.sendSuccess(new TextComponent("Memory: " + formatBytesToReadable(totalMemory - freeMemory) + "/" + formatBytesToReadable(totalMemory)), false);
                source.sendSuccess(new TextComponent("Free Memory: " + formatBytesToReadable(freeMemory)), false);
            }
            default -> source.sendFailure(new TextComponent("Wrong argument, available arguments are: heap, offheap, system"));
        }
        return 0;
    }

    private static int getTPS(CommandSourceStack source){
        String formatted = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.ENGLISH)).format(((ITPS)source.getServer()).getAverageTPS());
        source.sendSuccess(new TextComponent("TPS: " + formatted), false);
        return 0;
    }

    private static final long K = 1024;
    private static final long M = K * K;
    private static final long G = M * K;

    private static String formatBytesToReadable(long bytes){
        final long[] dividers = new long[] {G, M, K, 1 };
        final String[] units = new String[] {"GB", "MB", "KB", "B" };
        if(bytes < 1){
            return "Unable to parse bytes to text";
        }
        for(int i = 0; i < dividers.length; i++){
            final long divider = dividers[i];
            if(bytes >= divider){
                return new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.ENGLISH)).format((double) bytes / divider) + " " + units[i];
            }
        }
        return "Unable to parse bytes to text";
    }
}
