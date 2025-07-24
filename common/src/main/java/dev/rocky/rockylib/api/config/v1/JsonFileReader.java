package dev.rocky.rockylib.api.config.v1;

import com.google.gson.*;
import dev.rocky.rockylib.Constants;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * A generic JSON file reader that reads key-value pairs from a JSON file.
 * If the file does not exist, it is created with default values.
 * This class can transform the keys and values of the JSON data using provided functions.
 * It can also create a backup of the JSON file in the specified directory.
 *
 * @param <T> The type of values stored in the JSON file.
 *
 * @deprecated Use {@link dev.rocky.rockylib.api.config.v1.JsonConfigManager} instead.
 */
@Deprecated(since = "6.0.1")
public class JsonFileReader<T> {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Type type;
    private final String fileName;
    private final Path directoryPath;
    private final Map<String, T> defaultValues;

    /**
     * Constructs a JsonFileReader with the specified file name, type, and default values.
     *
     * @param fileName      The name of the JSON file.
     * @param type          The type of the values in the JSON file.
     * @param defaultValues The default values to write if the file does not exist.
     */
    public JsonFileReader(String fileName, Path directoryPath, Type type, Map<String, T> defaultValues) {
        this.fileName = fileName;
        this.directoryPath = directoryPath;
        this.type = type;
        this.defaultValues = defaultValues;
    }

    /**
     * Reads the JSON file from the specified directory and returns its content as a Map.
     *
     * @return A Map containing key-value pairs from the JSON file.
     */
    public Map<String, T> read() {
        Map<String, T> data = new HashMap<>();
        File directory = new File(String.valueOf(directoryPath));
        File file = new File(directory, fileName);

        if (!directory.exists()) {
            directory.mkdirs();
        }

        if (!file.exists()) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(gson.toJson(defaultValues));
            } catch (Exception e) {
                Constants.LOG.error(e.getLocalizedMessage());
                return data;
            }
        }

        try (FileReader reader = new FileReader(file)) {
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();

            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                data.put(entry.getKey(), gson.fromJson(entry.getValue(), type));
            }
        } catch (Exception e) {
            Constants.LOG.error(e.getLocalizedMessage());
        }

        return data;
    }

    /**
     * Transforms the keys and values of the JSON data using the provided functions.
     *
     * @param <K>              The new type for the transformed keys.
     * @param <V>              The new type for the transformed values.
     * @param data             The original data map.
     * @param keyTransformer   A function to transform the keys.
     * @param valueTransformer A function to transform the values.
     * @return A new map with transformed keys and values.
     */
    public <K, V> Map<K, V> transform(Map<String, T> data, Function<String, K> keyTransformer,
            Function<T, V> valueTransformer) {
        Map<K, V> transformedData = new HashMap<>();
        for (Map.Entry<String, T> entry : data.entrySet()) {
            transformedData.put(keyTransformer.apply(entry.getKey()), valueTransformer.apply(entry.getValue()));
        }
        return transformedData;
    }

    /**
     * Reads the JSON file from the specified directory and returns its content as a Map with keys and values
     * transformed using the provided functions.
     *
     * @param keyTransformer   A function to transform the keys.
     * @param valueTransformer A function to transform the values.
     * @return A new map with transformed keys and values.
     */
    public <K, V> Map<K, V> readTransformed(Function<String, K> keyTransformer, Function<T, V> valueTransformer) {
        return transform(read(), keyTransformer, valueTransformer);
    }


    /**
     * Creates a backup of the JSON file in the specified directory. The backup file is named with a ".bak" extension.
     * If the file has not changed since the last backup, no new backup is created. If the file does not exist, no backup
     * is created.
     */
    public void createBackup() {
        File directory = new File(String.valueOf(directoryPath));
        File file = new File(directory, fileName);
        @Nullable File mostRecentBackup = null;
        File backup = null;
        if (!directory.exists()) {
            directory.mkdirs();
        }

        int count = 0;
        do {
            mostRecentBackup = backup;
            backup = new File(directory, fileName + ".bak" + ++count);
        } while (backup.exists());

        if (areFilesEqual(file, mostRecentBackup)) {
            return;
        }

        if (file.exists()) {
            try (FileReader reader = new FileReader(file); FileWriter writer = new FileWriter(backup)) {
                char[] buffer = new char[1024];
                int bytesRead;
                while ((bytesRead = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, bytesRead);
                }
            } catch (Exception e) {
                Constants.LOG.error(e.getLocalizedMessage());
            }
        }
    }

    private boolean areFilesEqual(File fileA, File fileB) {
        if (!fileA.exists() || fileB == null || !fileB.exists()) {
            return false;
        }

        if (fileA.length() != fileB.length()) {
            return false;
        }

        try (FileReader readerA = new FileReader(fileA); FileReader readerB = new FileReader(fileB)) {
            int charA, charB;
            while ((charA = readerA.read()) != -1 && (charB = readerB.read()) != -1) {
                if (charA != charB) {
                    return false;
                }
            }
        } catch (Exception e) {
            Constants.LOG.error(e.getLocalizedMessage());
            return false;
        }

        return true;
    }

}
