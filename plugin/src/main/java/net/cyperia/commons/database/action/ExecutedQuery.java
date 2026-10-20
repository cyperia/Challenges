package net.cyperia.commons.database.action;

import net.cyperia.commons.common.config.Document;
import net.cyperia.commons.common.logging.ILogger;
import net.cyperia.commons.common.logging.LogLevel;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.util.*;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @see DatabaseQuery#execute()
 */
public interface ExecutedQuery extends Iterable<Document> {

  @NotNull
  @CheckReturnValue
  Optional<Document> first();

  @NotNull
  @CheckReturnValue
  default Document firstOrEmpty() {
    return first().orElse(Document.empty());
  }

  @NotNull
  @CheckReturnValue
  Optional<Document> get(int index);

  @NotNull
  @CheckReturnValue
  default Document getOrEmpty(int index) {
    return get(index).orElse(Document.empty());
  }

  @NotNull
  @CheckReturnValue
  Stream<Document> all();

  @NotNull
  @CheckReturnValue
  default List<Document> toList() {
    return toCollection((IntFunction<List<Document>>) ArrayList::new);
  }

  @NotNull
  @CheckReturnValue
  default Set<Document> toSet() {
    return toCollection((IntFunction<Set<Document>>) HashSet::new);
  }

  @NotNull
  <C extends Collection<? super Document>> C toCollection(@NotNull C collection);

  @NotNull
  @CheckReturnValue
  default <C extends Collection<? super Document>> C toCollection(@NotNull IntFunction<C> collectionSupplier) {
    return toCollection(collectionSupplier.apply(size()));
  }

  @NotNull
  Document[] toArray(@NotNull IntFunction<Document[]> arraySupplier);

  int index(@NotNull Predicate<? super Document> filter);

  boolean isEmpty();

  boolean isSet();

  int size();

  void print(@NotNull PrintStream out);

  default void print(@NotNull ILogger logger) {
    print(logger.asPrintStream(LogLevel.INFO));
  }

  default void print() {
    print(System.out);
  }

}
