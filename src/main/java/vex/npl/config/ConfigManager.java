package vex.npl.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigManager {

    // имя файла конфига
    private static final String FILE_NAME = "nether-portal-limiter.json";

    // экземпляр GSON с отступами
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // статическая ссылка на наш конфиг, к которой мы будем обращаться отовсюду
    public static ModConfig INSTANCE = new ModConfig();

    private static File getConfigFile() {
        // FabricLoader сам знает точный путь к папке config текущей сборки игры
        return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME).toFile();
    }

    // загрузка конфига из файла
    public static void load() {
        File file = getConfigFile();
        if (!file.exists()) {
            // если файла нет, сохраняем дефолт конфиг
            save();
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            INSTANCE = GSON.fromJson(reader, ModConfig.class);
            // если файл пустой или поврежден, GSON вернет null
            if (INSTANCE == null) {
                INSTANCE = new ModConfig();
            }
        } catch (IOException e) {
            System.err.println("[vex.Nether-Portal-Limiter] Не удалось загрузить конфиг, используеться дефолт!");
            e.printStackTrace();
        }
    }

    // сохранение конфига в файл
    public static void save() {
        File file = getConfigFile();

        // создание родительских папок на всякий случай, если их нет
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(INSTANCE, writer);
        } catch (IOException e) {
            System.err.println("[vex.Nether-Portal-Limiter] Не удалось сохранить конфиг!");
            e.printStackTrace();
        }
    }
}
