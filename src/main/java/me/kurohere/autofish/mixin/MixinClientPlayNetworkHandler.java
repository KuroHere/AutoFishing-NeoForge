package me.kurohere.autofish.mixin;

import me.kurohere.autofish.AutoFish;
import me.kurohere.autofish.interfaces.ILocal;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class MixinClientPlayNetworkHandler implements ILocal {

    @Inject(method = "handleSoundEvent", at = @At("HEAD"))
    public void onPlaySound(ClientboundSoundPacket playSoundS2CPacket_1, CallbackInfo ci) {
        if (mc.isSameThread()) AutoFish.getInstance().handlePacket(playSoundS2CPacket_1);
    }

    @Inject(method = "handleSetEntityMotion", at = @At("HEAD"))
    public void onVelocityUpdate(ClientboundSetEntityMotionPacket entityVelocityUpdateS2CPacket_1, CallbackInfo ci) {
        if (mc.isSameThread()) AutoFish.getInstance().handlePacket(entityVelocityUpdateS2CPacket_1);
    }

    @Inject(method = "handleSystemChat", at = @At("HEAD"))
    public void onSysChatMessage(ClientboundSystemChatPacket p_233708_, CallbackInfo ci) {
        if (mc.isSameThread()) AutoFish.getInstance().handleChat(p_233708_);
    }
}
