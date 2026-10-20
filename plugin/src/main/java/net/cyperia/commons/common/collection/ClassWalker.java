package net.cyperia.commons.common.collection;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author JDA | https://github.com/DV8FromTheWorld/JDA/blob/development/src/main/java/net/dv8tion/jda/internal/utils/ClassWalker.java
 */
public class ClassWalker implements Iterable<Class<?>> {

  protected final Class<?> clazz;
  protected final Class<?> end;

  protected ClassWalker(@NotNull Class<?> clazz) {
    this(clazz, Object.class);
  }

  protected ClassWalker(@NotNull Class<?> clazz, @NotNull Class<?> end) {
    this.clazz = clazz;
    this.end = end;
  }

  public static ClassWalker range(@NotNull Class<?> start, @NotNull Class<?> end) {
    return new ClassWalker(start, end);
  }

  public static ClassWalker walk(@NotNull Class<?> start) {
    return new ClassWalker(start);
  }

  @NotNull
  @Override
  public Iterator<Class<?>> iterator() {
    return new Iterator<Class<?>>() {

      private final Set<Class<?>> done = new HashSet<>();
      private final Deque<Class<?>> work = new LinkedList<>();

      {
        work.addLast(clazz);
        done.add(end);
      }

      @Override
      public boolean hasNext() {
        return !work.isEmpty();
      }

      @Override
      public Class<?> next() {
        Class<?> current = work.removeFirst();
        done.add(current);
        for (Class<?> parent : current.getInterfaces()) {
          if (!done.contains(parent))
            work.addLast(parent);
        }

        Class<?> parent = current.getSuperclass();
        if (parent != null && !done.contains(parent))
          work.addLast(parent);
        return current;
      }
    };
  }
}
