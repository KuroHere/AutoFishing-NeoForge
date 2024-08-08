package me.kurohere.autofish.gui;

import me.kurohere.autofish.AutoFish;
import me.kurohere.autofish.config.Config;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Function;

public class AutoFishConfigScreen {
    private static final Function<Boolean, Component> yesNoTextSupplier = bool -> {
        if (bool) return (Component.translatable("options.autofish.toggle.on"));
        else return (Component.translatable("options.autofish.toggle.off"));
    };

    public static Screen buildScreen(AutoFish modAutofish, Screen parentScreen) {
        Config defaults = new Config();
        Config config = modAutofish.getConfig();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parentScreen)
                .setTitle(Component.translatable("options.autofish.title"))
                .transparentBackground()
                .setDoesConfirmSave(true)
                .setSavingRunnable(() -> {
                    modAutofish.getConfig().enforceConstraints();
                    modAutofish.getConfigManager().writeConfig(true);
                });

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory configCat = builder.getOrCreateCategory(Component.translatable("options.autofish.config"));


        //Enable Autofish
        AbstractConfigListEntry toggleAutofish = entryBuilder.startBooleanToggle(Component.translatable("options.autofish.enable.title"), config.isAutofishEnabled())
                .setDefaultValue(defaults.isAutofishEnabled())
                .setTooltip(Component.translatable("options.autofish.enable.tooltip"))
                .setSaveConsumer(newValue -> {
                    modAutofish.getConfig().setAutofishEnabled(newValue);
                })
                .setYesNoTextSupplier(yesNoTextSupplier)
                .build();

        AbstractConfigListEntry toggleVanillaFishingRod = entryBuilder.startBooleanToggle(Component.translatable("options.autofish.vallina.title"), config.isUseOnlyFishingRod())
                .setDefaultValue(defaults.isUseOnlyFishingRod())
                .setTooltip(Component.translatable("options.autofish.vallina.tooltip"))
                .setSaveConsumer(newValue -> {
                    modAutofish.getConfig().setUseOnlyFishingRod(newValue);
                })
                .setYesNoTextSupplier(yesNoTextSupplier)
                .build();

        //Enable MultiRod
        AbstractConfigListEntry toggleMultiRod = entryBuilder.startBooleanToggle(Component.translatable("options.autofish.multirod.title"), config.isMultiRod())
                .setDefaultValue(defaults.isMultiRod())
                .setTooltip(
                        Component.translatable("options.autofish.multirod.tooltip_0"),
                        Component.translatable("options.autofish.multirod.tooltip_1"),
                        Component.translatable("options.autofish.multirod.tooltip_2")
                )
                .setSaveConsumer(newValue -> {
                    modAutofish.getConfig().setMultiRod(newValue);
                })
                .setYesNoTextSupplier(yesNoTextSupplier)
                .build();

        //Enable Open Water Detection
        AbstractConfigListEntry toggleOpenWaterDetection = entryBuilder.startBooleanToggle(Component.translatable("options.autofish.open_water_detection.title"), config.isOpenWaterDetectEnabled())
                .setDefaultValue(defaults.isOpenWaterDetectEnabled())
                .setTooltip(
                        Component.translatable("options.autofish.open_water_detection.tooltip_0"),
                        Component.translatable("options.autofish.open_water_detection.tooltip_1"),
                        Component.translatable("options.autofish.open_water_detection.tooltip_2")
                )
                .setSaveConsumer(newValue -> {
                    modAutofish.getConfig().setOpenWaterDetectEnabled(newValue);
                })
                .setYesNoTextSupplier(yesNoTextSupplier)
                .build();
        //Enable Break Protection
        AbstractConfigListEntry toggleBreakProtection = entryBuilder.startBooleanToggle(Component.translatable("options.autofish.break_protection.title"), config.isNoBreak())
                .setDefaultValue(defaults.isNoBreak())
                .setTooltip(
                        Component.translatable("options.autofish.break_protection.tooltip_0"),
                        Component.translatable("options.autofish.break_protection.tooltip_1")
                )
                .setSaveConsumer(newValue -> {
                    modAutofish.getConfig().setNoBreak(newValue);
                })
                .setYesNoTextSupplier(yesNoTextSupplier)
                .build();

        //Enable Persistent Mode
        AbstractConfigListEntry togglePersistentMode = entryBuilder.startBooleanToggle(Component.translatable("options.autofish.persistent.title"), config.isPersistentMode())
                .setDefaultValue(defaults.isPersistentMode())
                .setTooltip(
                        Component.translatable("options.autofish.persistent.tooltip_0"),
                        Component.translatable("options.autofish.persistent.tooltip_1"),
                        Component.translatable("options.autofish.persistent.tooltip_2"),
                        Component.translatable("options.autofish.persistent.tooltip_3"),
                        Component.translatable("options.autofish.persistent.tooltip_4"),
                        Component.translatable("options.autofish.persistent.tooltip_5")
                )
                .setSaveConsumer(newValue -> {
                    modAutofish.getConfig().setPersistentMode(newValue);
                })
                .setYesNoTextSupplier(yesNoTextSupplier)
                .build();


        //Enable Sound Detection
        AbstractConfigListEntry toggleSoundDetection = entryBuilder.startBooleanToggle(Component.translatable("options.autofish.sound.title"), config.isUseSoundDetection())
                .setDefaultValue(defaults.isUseSoundDetection())
                .setTooltip(
                        Component.translatable("options.autofish.sound.tooltip_0"),
                        Component.translatable("options.autofish.sound.tooltip_1"),
                        Component.translatable("options.autofish.sound.tooltip_2"),
                        Component.translatable("options.autofish.sound.tooltip_3"),
                        Component.translatable("options.autofish.sound.tooltip_4"),
                        Component.translatable("options.autofish.sound.tooltip_5"),
                        Component.translatable("options.autofish.sound.tooltip_6"),
                        Component.translatable("options.autofish.sound.tooltip_7"),
                        Component.translatable("options.autofish.sound.tooltip_8"),
                        Component.translatable("options.autofish.sound.tooltip_9")
                )
                .setSaveConsumer(newValue -> {
                    modAutofish.getConfig().setUseSoundDetection(newValue);
                    modAutofish.getAutofish().setDetection();
                })
                .setYesNoTextSupplier(yesNoTextSupplier)
                .build();

        //Enable Force MP Detection
        AbstractConfigListEntry toggleForceMPDetection = entryBuilder.startBooleanToggle(Component.translatable("options.autofish.multiplayer_compat.title"), config.isForceMPDetection())
                .setDefaultValue(defaults.isPersistentMode())
                .setTooltip(
                        Component.translatable("options.autofish.multiplayer_compat.tooltip_0"),
                        Component.translatable("options.autofish.multiplayer_compat.tooltip_1"),
                        Component.translatable("options.autofish.multiplayer_compat.tooltip_2")
                )
                .setSaveConsumer(newValue -> {
                    modAutofish.getConfig().setForceMPDetection(newValue);
                })
                .setYesNoTextSupplier(yesNoTextSupplier)
                .build();

        //Recast Delay
        AbstractConfigListEntry recastDelaySlider = entryBuilder.startLongSlider(Component.translatable("options.autofish.recast_delay.title"), config.getRecastDelay(), 500, 5000)
                .setDefaultValue(defaults.getRecastDelay())
                .setTooltip(
                        Component.translatable("options.autofish.recast_delay.tooltip_0"),
                        Component.translatable("options.autofish.recast_delay.tooltip_1")
                )
                .setTextGetter(value -> Component.translatable("options.autofish.recast_delay.value", value))
                .setSaveConsumer(newValue -> {
                    modAutofish.getConfig().setRecastDelay(newValue);
                })
                .build();
        AbstractConfigListEntry randomDelaySlider = entryBuilder.startLongSlider(Component.translatable("options.autofish.random_delay.title"), config.getRandomDelay(), 0, 75)
                .setDefaultValue(defaults.getRandomDelay())
                .setTooltip(
                        Component.translatable("options.autofish.random_delay.tooltip_0"),
                        Component.translatable("options.autofish.random_delay.tooltip_1"),
                        Component.translatable("options.autofish.random_delay.tooltip_2"),
                        Component.translatable("options.autofish.random_delay.tooltip_3")
                )
                .setTextGetter(value -> Component.translatable("options.autofish.random_delay.value", value))
                .setSaveConsumer(newValue -> {
                    modAutofish.getConfig().setRandomDelay(newValue);
                })
                .build();

        AbstractConfigListEntry reelInDelay = entryBuilder.startLongSlider(Component.translatable("options.autofish.reel_in_delay.title"), config.getReelInDelay(), 1, 2000)
                .setDefaultValue(defaults.getReelInDelay())
                .setTooltip(
                        Component.translatable("options.autofish.reel_in_delay.tooltip_0"),
                        Component.translatable("options.autofish.reel_in_delay.tooltip_1")
                )
                .setTextGetter(value -> Component.translatable("options.autofish.reel_in_delay.value", value))
                .setSaveConsumer(newValue -> {
                    modAutofish.getConfig().setReelInDelay(newValue);
                })
                .build();

        //ClearLag Regex
        AbstractConfigListEntry clearLagRegexField = entryBuilder.startTextField(Component.translatable("options.autofish.clear_regex.title"), config.getClearLagRegex())
                .setDefaultValue(defaults.getClearLagRegex())
                .setTooltip(
                        Component.translatable("options.autofish.clear_regex.tooltip_0"),
                        Component.translatable("options.autofish.clear_regex.tooltip_1"),
                        Component.translatable("options.autofish.clear_regex.tooltip_2")
                )
                .setSaveConsumer(newValue -> {
                    modAutofish.getConfig().setClearLagRegex(newValue);
                })
                .build();

        SubCategoryBuilder subCatBuilderBasic = entryBuilder.startSubCategory(Component.translatable("options.autofish.basic.title"));
        subCatBuilderBasic.add(toggleAutofish);
        subCatBuilderBasic.add(toggleVanillaFishingRod);
        subCatBuilderBasic.add(toggleMultiRod);
        subCatBuilderBasic.add(toggleOpenWaterDetection);
        subCatBuilderBasic.add(toggleBreakProtection);
        subCatBuilderBasic.add((togglePersistentMode));
        subCatBuilderBasic.setExpanded(true);

        SubCategoryBuilder subCatBuilderAdvanced = entryBuilder.startSubCategory(Component.translatable("options.autofish.advanced.title"));
        subCatBuilderAdvanced.add(toggleSoundDetection);
        subCatBuilderAdvanced.add(toggleForceMPDetection);
        subCatBuilderAdvanced.add(recastDelaySlider);
        subCatBuilderAdvanced.add(randomDelaySlider);
        subCatBuilderAdvanced.add(reelInDelay);
        subCatBuilderAdvanced.add(clearLagRegexField);
        subCatBuilderAdvanced.setExpanded(true);

        configCat.addEntry(subCatBuilderBasic.build());
        configCat.addEntry(subCatBuilderAdvanced.build());

        return builder.build();

    }
}