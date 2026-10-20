package net.cyperia.commons.common.config.document;

import net.cyperia.commons.common.config.Document;
import net.cyperia.commons.common.config.document.readonly.ReadOnlyDocumentWrapper;
import net.cyperia.commons.common.config.exceptions.ConfigReadOnlyException;
import net.cyperia.commons.common.misc.BukkitReflectionSerializationUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractDocument extends AbstractConfig implements Document {

  protected final Document root, parent;

  public AbstractDocument(@NotNull Document root, @Nullable Document parent) {
    this.root = root;
    this.parent = parent;
  }

  public AbstractDocument() {
    this.root = this;
    this.parent = null;
  }

  @NotNull
  @Override
  public <T> T getSerializable(@NotNull String path, @NotNull T def) {
    T value = getSerializable(path, (Class<T>) def.getClass());
    return value == null ? def : value;
  }

  @Nullable
  @Override
  public <T> T getSerializable(@NotNull String path, @NotNull Class<T> classOfT) {
    if (!contains(path)) return null;
    return BukkitReflectionSerializationUtils.deserializeObject(getDocument(path).values(), classOfT);
  }

  @NotNull
  @Override
  public <T> List<T> getSerializableList(@NotNull String path, @NotNull Class<T> classOfT) {
    return getDocumentList(path).stream()
      .map(Document::values)
      .map(map -> BukkitReflectionSerializationUtils.deserializeObject(map, classOfT))
      .collect(Collectors.toList());
  }

  @NotNull
  @Override
  public <K, V> Map<K, V> mapDocuments(@NotNull Function<? super String, ? extends K> keyMapper, @NotNull Function<? super Document, ? extends V> valueMapper) {
    return map(children(), keyMapper, valueMapper);
  }

  @NotNull
  @Override
  public <R> R mapDocument(@NotNull String path, @NotNull Function<? super Document, ? extends R> mapper) {
    Document document = getDocument(path);
    return mapper.apply(document);
  }

  @Nullable
  @Override
  public <R> R mapDocumentNullable(@NotNull String path, @NotNull Function<? super Document, ? extends R> mapper) {
    if (!contains(path)) return null;
    return mapDocument(path, mapper);
  }

  @NotNull
  @Override
  public Document getDocument(@NotNull String path) {
    Document document = getDocument0(path, root, this);
    return isReadonly() && !document.isReadonly() ? new ReadOnlyDocumentWrapper(document) : document;
  }

  @NotNull
  @Override
  public Document set(@NotNull String path, @Nullable Object value) {
    if (isReadonly()) throw new ConfigReadOnlyException("set");

    if (value instanceof byte[])
      value = Base64.getEncoder().encodeToString((byte[]) value);

    set0(path, value);
    return this;
  }

  @NotNull
  @Override
  public Document set(@NotNull Object object) {
    if (isReadonly()) throw new ConfigReadOnlyException("set");

    Document.of(object).forEach(this::set);
    return this;
  }

  @NotNull
  @Override
  public Document remove(@NotNull String path) {
    if (isReadonly()) throw new ConfigReadOnlyException("remove");
    remove0(path);
    return this;
  }

  @NotNull
  @Override
  public Document clear() {
    if (isReadonly()) throw new ConfigReadOnlyException("clear");
    clear0();
    return this;
  }

  @NotNull
  @Override
  public Document readonly() {
    return isReadonly() ? this : new ReadOnlyDocumentWrapper(this);
  }

  @NotNull
  protected abstract Document getDocument0(@NotNull String path, @NotNull Document root, @Nullable Document parent);

  protected abstract void set0(@NotNull String path, @Nullable Object value);

  protected abstract void remove0(@NotNull String path);

  protected abstract void clear0();

  @NotNull
  @Override
  public Map<String, Document> children() {
    Map<String, Document> map = new HashMap<>();
    keys().forEach(key -> {
      if (!isDocument(key)) return;
      map.put(key, getDocument(key));
    });
    return map;
  }

  @NotNull
  @Override
  public Set<String> deepKeys() {
    Set<String> deepKeys = new HashSet<>();
    appendDeepKeys(this, "", deepKeys);
    return deepKeys;
  }

  protected void appendDeepKeys(@NotNull Document root, @NotNull String pathUntil, @NotNull Collection<String> into) {
    for (String key : root.keys()) {
      if (root.isDocument(key)) {
        appendDeepKeys(root.getDocument(key), pathUntil + key + ".", into);
      } else {
        into.add(pathUntil + key);
      }
    }
  }

  @Override
  public boolean hasChildren(@NotNull String path) {
    return !getDocument(path).isEmpty();
  }

  @NotNull
  @Override
  public Document getRoot() {
    return root;
  }

  @Nullable
  @Override
  public Document getParent() {
    return parent;
  }

}
