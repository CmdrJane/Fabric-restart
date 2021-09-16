package ru.aiefu.fabricrestart.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import ru.aiefu.fabricrestart.ITPS;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixins implements ITPS {
    private long timeRefMs = 0L;
    private final List<Long> samples = new ArrayList<>();
    private final List<Double> tpsSamples = new ArrayList<>();

    private long nanoTimeStart;

    @Inject(method = "tick", at = @At(value = "FIELD", target = "net/minecraft/server/MinecraftServer.ticks:I", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    private void captureMSTimeCall(BooleanSupplier shouldKeepTicking, CallbackInfo ci, long l){
        this.nanoTimeStart = l;
    }


    @Inject(method = "tick", at = @At("TAIL"))
    private void invokeTPSFRCalc(BooleanSupplier shouldKeepTicking, CallbackInfo ci){
        this.calculateTPSFR(nanoTimeStart, Util.getMeasuringTimeNano());
    }

    private void calculateTPSFR(long l, long n){
        samples.add(n - l);
        if(n >= timeRefMs || samples.size() >= 20){
            timeRefMs = n + 1_000_000_000;
            tpsSamples.add(Math.min(20.0D / (samples.stream().mapToLong(Long::longValue).sum() / 1000_000_000.0D), 20.0D));
            samples.clear();
            if(tpsSamples.size() > 30){
                tpsSamples.remove(0);
            }
        }
    }
    public double getAverageTPS(){
        return Math.min(tpsSamples.stream().mapToDouble(Double::doubleValue).sum() / tpsSamples.size(), 20.0D);
    }
}
