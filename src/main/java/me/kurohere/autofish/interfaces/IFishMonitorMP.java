package me.kurohere.autofish.interfaces;

import me.kurohere.autofish.AutoFishClient;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.projectile.FishingHook;

public interface IFishMonitorMP {
    void hookTick(AutoFishClient autofish, Minecraft minecraft, FishingHook hook);

    void handleHookRemoved();

    void handlePacket(AutoFishClient autofish, Packet<?> packet, Minecraft minecraft);
}