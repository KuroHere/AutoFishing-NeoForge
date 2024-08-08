package me.kurohere.autofish.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.kurohere.autofish.AutoFish;
import net.neoforged.fml.loading.FMLPaths;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ConfigManager {
    private Config config;
    private final Gson gson;
    private final File configFile;

    private final Executor executor = Executors.newSingleThreadExecutor();

    public ConfigManager() {
        this.gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
        this.configFile = new File(FMLPaths.CONFIGDIR.get().toFile(), "autofish.config");
        //run synchronously on first run so our options are available for the Autofish instance
        readConfig(false);
    }

    public void readConfig(boolean async) {
        Runnable task = () -> {
            try {
                if (configFile.exists()) {
                    String fileContents = FileUtils.readFileToString(configFile, Charset.defaultCharset());
                    config = gson.fromJson(fileContents, Config.class);
                    if (config.enforceConstraints()) writeConfig(true);
                } else {
                    writeNewConfig();
                }

            } catch (Exception e) {
                e.printStackTrace();
                writeNewConfig();
            }
        };

        if (async) executor.execute(task);
        else task.run();
    }

    public void writeNewConfig() {
        config = new Config();
        writeConfig(false);
    }

    public void writeConfig(boolean async) {
        Runnable task = () -> {
            try {
                if (config != null) {
                    String serialized = gson.toJson(config);
                    FileUtils.writeStringToFile(configFile, serialized, Charset.defaultCharset());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        if (async) executor.execute(task);
        else task.run();
    }

    public Config getConfig() {
        return config;
    }
}