package me.kurohere.autofish.mixin;

import me.kurohere.autofish.AutoFish;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingHook.class)
public class MixinFishHookEntity {
    @Shadow private int nibble;

    @Inject(method = "catchingFish",at = @At("TAIL"))
    private void catchingFish(BlockPos p_37146_, CallbackInfo ci){
        AutoFish.getInstance().tickFishingLogic(((FishingHook) (Object) this).getOwner(), nibble);
    }
}
