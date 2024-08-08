package me.kurohere.autofish;

import me.kurohere.autofish.interfaces.ILocal;
import me.kurohere.autofish.interfaces.IFishMonitorMP;

import me.kurohere.autofish.mointor.FishMonitorMPMotion;
import me.kurohere.autofish.mointor.FishMonitorMPSound;
import me.kurohere.autofish.scheduler.ActionType;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoFishClient implements ILocal {
    private final AutoFish autoFish;
    private IFishMonitorMP fishMonitorMP;

    private boolean hookExists = false;
    private long hookRemovedAt = 0L;
    private boolean alreadyAlertOP = false;
    private boolean alreadyPassOP = false;

    public long timeMillis = 0L;

    public AutoFishClient(AutoFish autoFish) {
        this.autoFish = autoFish;
        setDetection();

        //Initiate the repeating action for persistent mode casting
        if (mc.player == null || mc.level == null) return;
        autoFish.getScheduler().scheduleRepeatingAction(10000, () -> {
            if (!autoFish.getConfig().isPersistentMode()) return;
            //if (!isHoldingFishingRod()) return;
            if (hookExists) return;
            if (autoFish.getScheduler().isRecastQueued()) return;

            useRod();
        });
    }

    public void tick(Minecraft mc) {
        if (mc.level != null && mc.player != null && autoFish.getConfig().isAutofishEnabled()) {
            timeMillis = Util.getMillis(); //update current working time for this tick

            //if (isHoldingFishingRod()) {
            if (mc.player.fishing != null) {
                hookExists = true;
                //MP catch listener
                if (shouldUseMPDetection()) {//multiplayer only, send tick event to monitor
                    fishMonitorMP.hookTick(this, mc, mc.player.fishing);
                }
            } else {
                removeHook();
            }
            //} else { //not holding fishing rod
            //    removeHook();
            //}
        }
    }

    /**
     * Callback from mixin for the catchingFish method of the EntityFishHook
     * for singleplayer detection only
     */
    public void tickFishingLogic(Entity owner, int ticksCatchable) {
        mc.execute(() ->{
            if (autoFish.getConfig().isAutofishEnabled() && !shouldUseMPDetection()) {
                //null checks for sanity
                if (mc.player != null && mc.player.fishing != null) {
                    //hook is catchable and player is correct
                    if (ticksCatchable > 0 && owner.getUUID().compareTo(mc.player.getUUID()) == 0) {
                        catchFish();
                    }
                }
            }
        });

    }

    /**
     * Callback from mixin when sound and motion packets are received
     * For multiplayer detection only
     */
    public void handlePacket(Packet<?> packet) {
        if (autoFish.getConfig().isAutofishEnabled()) {
            if (shouldUseMPDetection()) {
                fishMonitorMP.handlePacket(this, packet, mc);
            }
        }
    }

    /**
     * Callback from mixin when chat packets are received
     * For multiplayer detection only
     */
    public void handleChat(ClientboundSystemChatPacket packet) {
        if (autoFish.getConfig().isAutofishEnabled()) {
            if (!mc.isLocalServer()) {
                //if (isHoldingFishingRod()) {
                //check that either the hook exists, or it was just removed
                //this prevents false casts if we are holding a rod but not fishing
                if (hookExists || (timeMillis - hookRemovedAt < 2000)) {
                    //make sure there is actually something there in the regex field
                    if (org.apache.commons.lang3.StringUtils.deleteWhitespace(autoFish.getConfig().getClearLagRegex()).isEmpty())
                        return;
                    //check if it matches
                    Matcher matcher = Pattern.compile(autoFish.getConfig().getClearLagRegex(), Pattern.CASE_INSENSITIVE).matcher(StringUtil.stripColor(packet.content().getString()));
                    if (matcher.find()) {
                        queueRecast();
                    }
                }
                //}
            }
        }
    }

    public void catchFish() {
        if(!autoFish.getScheduler().isRecastQueued()) { //prevents double reels
            detectOpenWater();

            //queue actions
            queueRodSwitch();
            queueRecast();

            //reel in
            autoFish.getScheduler().scheduleAction(ActionType.REEL_IN,
                                                   autoFish.getConfig().getReelInDelay(),
                                                   this::useRod);
        }
    }

    public void queueRecast() {
        autoFish.getScheduler().scheduleAction(ActionType.RECAST,
                                               getRandomDelay() + autoFish.getConfig().getReelInDelay(), () -> {
                    //State checks to ensure we can still fish once this runs
                    if(hookExists) return;
                    //if(!isHoldingFishingRod()) return;
                    if(autoFish.getConfig().isNoBreak() && Objects.requireNonNull(getHeldItem()).getDamageValue() >= 63) return;

                    useRod();
                });
    }

    private void queueRodSwitch() {
        autoFish.getScheduler().scheduleAction(ActionType.ROD_SWITCH,
                                               (long) (getRandomDelay() * 0.83) + autoFish.getConfig().getReelInDelay(), () -> {
                    if(!autoFish.getConfig().isMultiRod()) return;

                    switchToFirstRod(mc.player);
                });
    }

    /**
     * Call this when the hook disappears
     */
    private void removeHook() {
        if (hookExists) {
            hookExists = false;
            hookRemovedAt = timeMillis;
            fishMonitorMP.handleHookRemoved();
        }
    }

    public void switchToFirstRod(LocalPlayer player) {
        if(player != null) {
            Inventory inventory = player.getInventory();
            for (int i = 0; i < inventory.items.size(); i++) {
                ItemStack slot = inventory.items.get(i);
                if (slot.getItem() == Items.FISHING_ROD) {
                    if (i < 9) { //hotbar only
                        if (autoFish.getConfig().isNoBreak()) {
                            if (slot.getDamageValue() < 63) {
                                inventory.selected = i;
                                return;
                            }
                        } else {
                            inventory.selected = i;
                            return;
                        }
                    }
                }
            }
        }
    }

    public void useRod() {
        if(mc.player != null && mc.level != null) {
            InteractionHand hand = getCorrectHand();
            InteractionResult actionResult = null;
            if (mc.gameMode != null) {
                actionResult = mc.gameMode.useItem(mc.player, hand);
            }
            if (actionResult != null && actionResult.consumesAction()) {
                if (actionResult.shouldSwing()) {
                    mc.player.swing(hand);
                }
                mc.gameRenderer.itemInHandRenderer.itemUsed(hand);
            }
        }
    }

    /**
     * @Reason: To work seamlessly with any fishing rod from other mods, without the need for APIs or reflection.
     **/
    public boolean isHoldingFishingRod() {
        return isItemFishingRod(Objects.requireNonNull(getHeldItem()).getItem());
    }

    private InteractionHand getCorrectHand() {
        if (!autoFish.getConfig().isMultiRod()) {
            if (mc.player != null/* && isItemFishingRod(mc.player.getOffhandItem().getItem())*/)
                return InteractionHand.OFF_HAND;
        }
        return InteractionHand.MAIN_HAND;
    }

    private void detectOpenWater(){
        /*
         * To catch items in the treasure category, the bobber must be in open water,
         * defined as the 5×4×5 vicinity around the bobber resting on the water surface
         * (2 blocks away horizontally, 2 blocks above the water surface, and 2 blocks deep).
         * Each horizontal layer in this area must consist only of air and lily pads or water source blocks,
         * waterlogged blocks without collision (such as signs, kelp, or coral fans), and bubble columns.
         * (from Minecraft wiki)
         * */
        if (mc.player == null || mc.player.fishing == null) return;
        if (!autoFish.getConfig().isOpenWaterDetectEnabled()) return;

        FishingHook bobber = mc.player.fishing;

        int x = bobber.getBlockX();
        int y = bobber.getBlockY();
        int z = bobber.getBlockZ();
        boolean flag = true;
        for(int yi = -2; yi <= 2; yi++){
            if(!(BlockPos.betweenClosedStream(x - 2, y + yi, z - 2, x + 2, y + yi, z + 2).allMatch((blockPos ->
                    // every block is water
                    bobber.getCommandSenderWorld().getBlockState(blockPos).getBlock() == Blocks.WATER
                                                                                                   )) || BlockPos.betweenClosedStream(x - 2, y + yi, z - 2, x + 2, y + yi, z + 2).allMatch((blockPos ->
                    // or every block is air or lily pad
                    bobber.getCommandSenderWorld().getBlockState(blockPos).getBlock() == Blocks.AIR
                            || bobber.getCommandSenderWorld().getBlockState(blockPos).getBlock() == Blocks.LILY_PAD
                                                                                                                                                                                           )))){
                // didn't pass the check
                if(!alreadyAlertOP){
                    Objects.requireNonNull(bobber.getPlayerOwner()).displayClientMessage(Component.translatable("info.autoFish.open_water_detection.fail"),true);
                    alreadyAlertOP = true;
                    alreadyPassOP = false;
                }
                flag = false;
            }
        }
        if(flag && !alreadyPassOP) {
            Objects.requireNonNull(bobber.getPlayerOwner()).displayClientMessage(Component.translatable("info.autoFish.open_water_detection.success"),true);
            alreadyPassOP = true;
            alreadyAlertOP = false;
        }


    }

    private ItemStack getHeldItem() {
        if (!autoFish.getConfig().isMultiRod()) {
            if (mc.player != null /*&& isItemFishingRod(mc.player.getOffhandItem().getItem())*/)
                return mc.player.getOffhandItem();
        }
        if (mc.player != null) {
            return mc.player.getMainHandItem();
        }
        return null;
    }

    private boolean isItemFishingRod(Item item) {
        return item == Items.FISHING_ROD || item instanceof FishingRodItem;
    }

    public void setDetection() {
        if (autoFish.getConfig().isUseSoundDetection()) {
            fishMonitorMP = new FishMonitorMPSound();
        } else {
            fishMonitorMP = new FishMonitorMPMotion();
        }
    }

    private boolean shouldUseMPDetection(){
        if(autoFish.getConfig().isForceMPDetection()) return true;
        return !mc.isLocalServer();
    }

    private long getRandomDelay(){
        return Math.random() >=0.5 ?
               (long) (autoFish.getConfig().getRecastDelay() * (1 - (Math.random() * autoFish.getConfig().getRandomDelay() * 0.01))) :
               (long) (autoFish.getConfig().getRecastDelay() * (1 + (Math.random() * autoFish.getConfig().getRandomDelay() * 0.01)));

    }
}
