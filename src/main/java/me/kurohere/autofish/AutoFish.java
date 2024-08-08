package me.kurohere.autofish;

import com.mojang.blaze3d.platform.InputConstants;
import me.kurohere.autofish.config.Config;
import me.kurohere.autofish.config.ConfigManager;
import me.kurohere.autofish.gui.AutoFishConfigScreen;
import me.kurohere.autofish.scheduler.AutoFishScheduler;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.LoggerFactory;

import static me.kurohere.autofish.interfaces.ILocal.mc;

@Mod(AutoFish.MODID)
public class AutoFish {
    public static final String MODID = "autofish";
    private static final Logger LOGGER = LoggerFactory.getLogger(AutoFish.class);
    private static AutoFish instance;
    private static AutoFishClient AUTO_FISH;
    private static AutoFishScheduler SCHEDULER;
    private static KeyMapping AUTO_FISH_GUI_KEY;
    private static ConfigManager CONFIG_MANAGER;

    public AutoFish(IEventBus bus) {
        NeoForge.EVENT_BUS.register(this);
        AUTO_FISH_GUI_KEY = new KeyMapping("key.autofish.open_gui", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_COMMA, "Autofish");

        bus.addListener(FMLClientSetupEvent.class, (fmlClientSetupEvent -> {
            fmlClientSetupEvent.enqueueWork(() -> {
                ModList.get().getModContainerById(MODID).ifPresent(modContainer -> {
                    LOGGER.info("Loaded {}, using version {}", modContainer.getModInfo().getDisplayName(), modContainer.getModInfo().getVersion());
                    init();
                });
            });
        }));
        bus.addListener(RegisterKeyMappingsEvent.class, (registerKeyMappingsEvent -> {
            registerKeyMappingsEvent.register(getAutoFishGuiKey());
        }));
    }

    @SubscribeEvent
    public void tick(ClientTickEvent.Post event) {
        if (mc.player == null || mc.level == null) return;
        if (AUTO_FISH_GUI_KEY.isDown()) {
            mc.setScreen(AutoFishConfigScreen.buildScreen(AutoFish.getInstance(), mc.screen));
        }
        AUTO_FISH.tick(mc);
        SCHEDULER.tick(mc);
    }

    public void init() {
        if (instance == null) instance = this;

        CONFIG_MANAGER = new ConfigManager();
        SCHEDULER = new AutoFishScheduler(getInstance());
        AUTO_FISH = new AutoFishClient(getInstance());
    }

    public void handlePacket(Packet<?> packet) {
        AUTO_FISH.handlePacket(packet);
    }

    public void handleChat(ClientboundSystemChatPacket packet) {
        AUTO_FISH.handleChat(packet);
    }

    public void tickFishingLogic(Entity owner, int ticksCatchable) {
        AUTO_FISH.tickFishingLogic(owner, ticksCatchable);
    }

    public static AutoFish getInstance() {
        return instance;
    }

    public AutoFishClient getAutofish() {
        return AUTO_FISH;
    }

    public ConfigManager getConfigManager() {
        return CONFIG_MANAGER;
    }

    public Config getConfig() {
        return CONFIG_MANAGER.getConfig();
    }

    public AutoFishScheduler getScheduler() {
        return SCHEDULER;
    }

    public KeyMapping getAutoFishGuiKey() {
        return AUTO_FISH_GUI_KEY;
    }
}

