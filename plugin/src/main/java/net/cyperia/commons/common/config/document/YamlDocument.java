package net.cyperia.commons.common.config.document;

import net.cyperia.commons.common.config.Document;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.YamlConstructor;
import org.bukkit.configuration.file.YamlRepresenter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.text.DateFormat;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;

public class YamlDocument extends AbstractDocument {

  protected final ConfigurationSection config;

  public YamlDocument() {
    this.config = new YamlConfiguration();
  }

  public YamlDocument(@NotNull ConfigurationSection config) {
    this.config = config;
  }

  public YamlDocument(@NotNull ConfigurationSection config, @NotNull Document root, @Nullable Document parent) {
    super(root, parent);
    this.config = config;
  }

  public YamlDocument(@NotNull File file) {
    this(YamlConfiguration.loadConfiguration(file));
  }

  public YamlDocument(@NotNull Reader reader) {
    this(YamlConfiguration.loadConfiguration(reader));
  }

  @Nullable
  @Override
  public String getString(@NotNull String path) {
    return config.getString(path);
  }

  @NotNull
  @Override
  public String getString(@NotNull String path, @NotNull String def) {
    String string = config.getString(path, def);
    return string == null ? def : string;
  }

  @Nullable
  @Override
  public Object getObject(@NotNull String path) {
    return config.get(path);
  }

  @NotNull
  @Override
  public Object getObject(@NotNull String path, @NotNull Object def) {
    Object value = config.get(path, def);
    return value == null ? def : value;
  }

  @Override
  public <T> T getInstance(@NotNull String path, @NotNull Class<T> classOfT) {
    return copyJson().getInstance(path, classOfT);
  }

  @Override
  public <T> T toInstanceOf(@NotNull Class<T> classOfT) {
    return copyJson().toInstanceOf(classOfT);
  }

  @NotNull
  @Override
  public Document getDocument0(@NotNull String path, @NotNull Document root, @Nullable Document parent) {
    ConfigurationSection section = config.getConfigurationSection(path);
    if (section == null) section = config.createSection(path);
    return new YamlDocument(section, root, parent);
  }

  @NotNull
  @Override
  public List<Document> getDocumentList(@NotNull String path) {
    List<Map<?, ?>> maps = config.getMapList(path);
    List<Document> documents = new ArrayList<>(maps.size());
    for (Map<?, ?> map : maps) {
      documents.add(new MapDocument((Map<String, Object>) map, root, this));
    }
    return documents;
  }

  @Override
  public long getLong(@NotNull String path) {
    return config.getLong(path);
  }

  @Override
  public long getLong(@NotNull String path, long def) {
    return config.getLong(path, def);
  }

  @Override
  public int getInt(@NotNull String path) {
    return config.getInt(path);
  }

  @Override
  public int getInt(@NotNull String path, int def) {
    return config.getInt(path, def);
  }

  @Override
  public short getShort(@NotNull String path) {
    return (short) config.getInt(path);
  }

  @Override
  public short getShort(@NotNull String path, short def) {
    return (short) config.getInt(path, def);
  }

  @Override
  public byte getByte(@NotNull String path) {
    return (byte) config.getInt(path);
  }

  @Override
  public byte getByte(@NotNull String path, byte def) {
    return (byte) config.getInt(path, def);
  }

  @Override
  public char getChar(@NotNull String path) {
    return getChar(path, (char) 0);
  }

  @Override
  public char getChar(@NotNull String path, char def) {
    try {
      return getString(path).charAt(0);
    } catch (NullPointerException | StringIndexOutOfBoundsException ex) {
      return def;
    }
  }

  @Override
  public double getDouble(@NotNull String path) {
    return config.getDouble(path);
  }

  @Override
  public double getDouble(@NotNull String path, double def) {
    return config.getDouble(path, def);
  }

  @Override
  public float getFloat(@NotNull String path) {
    return (float) config.getDouble(path);
  }

  @Override
  public float getFloat(@NotNull String path, float def) {
    return (float) config.getDouble(path, def);
  }

  @Override
  public boolean getBoolean(@NotNull String path) {
    return config.getBoolean(path);
  }

  @Override
  public boolean getBoolean(@NotNull String path, boolean def) {
    return config.getBoolean(path, def);
  }

  @NotNull
  @Override
  public List<String> getStringList(@NotNull String path) {
    return config.getStringList(path);
  }

  @Nullable
  @Override
  public UUID getUUID(@NotNull String path) {
    try {
      return UUID.fromString(getString(path));
    } catch (Exception ex) {
      return null;
    }
  }

  @Nullable
  @Override
  public Date getDate(@NotNull String path) {
    try {
      return DateFormat.getDateTimeInstance().parse(path);
    } catch (Exception ex) {
      return null;
    }
  }

  @Nullable
  @Override
  public OffsetDateTime getDateTime(@NotNull String path) {
    try {
      return OffsetDateTime.parse(getString(path));
    } catch (Exception ex) {
      return null;
    }
  }

  @Nullable
  @Override
  public Color getColor(@NotNull String path) {
    try {
      return Color.decode(getString(path));
    } catch (Exception ex) {
      return null;
    }
  }

  @Nullable
  @Override
  public <E extends Enum<E>> E getEnum(@NotNull String path, @NotNull Class<E> classOfEnum) {
    try {
      String name = getString(path);
      if (name == null) return null;
      return Enum.valueOf(classOfEnum, name);
    } catch (Throwable ex) {
      return null;
    }
  }

  @Override
  public boolean isList(@NotNull String path) {
    return config.isList(path);
  }

  @Override
  public boolean isObject(@NotNull String path) {
    return !isDocument(path) && !isList(path);
  }

  @Override
  public boolean isDocument(@NotNull String path) {
    return config.get(path) instanceof ConfigurationSection;
  }

  @Override
  public boolean contains(@NotNull String path) {
    return config.contains(path, true);
  }

  @Override
  public int size() {
    return config.getValues(false).size();
  }

  @Override
  public void set0(@NotNull String path, @Nullable Object value) {
    if (value instanceof Enum<?>) {
      Enum<?> enun = (Enum<?>) value;
      value = enun.name();
    }
    config.set(path, value);
  }

  @Override
  public void remove0(@NotNull String path) {
    config.set(path, null);
  }

  @Override
  public void clear0() {
    for (String key : config.getKeys(true)) {
      config.set(key, null);
    }
  }

  @NotNull
  @Override
  public Map<String, Object> values() {
    return config.getValues(true);
  }

  @NotNull
  @Override
  public Collection<String> keys() {
    return config.getKeys(false);
  }

  @NotNull
  @Override
  public Set<String> deepKeys() {
    return config.getKeys(true);
  }

  @Override
  public void forEach(@NotNull BiConsumer<? super String, ? super Object> action) {
    values().forEach(action);
  }

  @Override
  public String toString() {

    DumperOptions yamlOptions = new DumperOptions();
    LoaderOptions loaderOptions = new LoaderOptions();
    Representer yamlRepresenter = new YamlRepresenter();
    Yaml yaml = new Yaml(new YamlConstructor(), yamlRepresenter, yamlOptions, loaderOptions);

    yamlOptions.setIndent(2);
    yamlOptions.setDefaultFlowStyle(FlowStyle.BLOCK);
    yamlRepresenter.setDefaultFlowStyle(FlowStyle.BLOCK);
    String dump = yaml.dump(config.getValues(false));
    if (dump.equals("{}\n")) {
      dump = "";
    }

    return dump;

  }

  @Override
  public void write(@NotNull Writer writer) throws IOException {
    writer.write(toString());
  }

  @NotNull
  @Override
  public Document copyJson() {
    return new GsonDocument(values());
  }

  @NotNull
  @Override
  public String toJson() {
    return copyJson().toJson();
  }

  @NotNull
  @Override
  public String toPrettyJson() {
    return copyJson().toPrettyJson();
  }

  @Override
  public boolean isReadonly() {
    return false;
  }

}
