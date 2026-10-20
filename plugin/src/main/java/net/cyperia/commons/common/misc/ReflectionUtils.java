package net.cyperia.commons.common.misc;

import net.cyperia.commons.common.collection.ArrayWalker;
import net.cyperia.commons.common.collection.ClassWalker;
import net.cyperia.commons.common.collection.WrappedException;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public final class ReflectionUtils {

  private ReflectionUtils() {
  }

  @NotNull
  public static Collection<Method> getPublicMethodsAnnotatedWith(@NotNull Class<?> clazz, @NotNull Class<? extends Annotation> classOfAnnotation) {
    List<Method> annotatedMethods = new ArrayList<>();
    for (Method method : clazz.getMethods()) {
      if (!method.isAnnotationPresent(classOfAnnotation)) continue;
      annotatedMethods.add(method);
    }
    return annotatedMethods;
  }

  @NotNull
  public static Collection<Method> getMethodsAnnotatedWith(@NotNull Class<?> clazz, @NotNull Class<? extends Annotation> classOfAnnotation) {
    List<Method> annotatedMethods = new ArrayList<>();
    for (Class<?> currentClass : ClassWalker.walk(clazz)) {
      for (Method method : currentClass.getDeclaredMethods()) {
        if (!method.isAnnotationPresent(classOfAnnotation)) continue;
        annotatedMethods.add(method);
      }
    }
    return annotatedMethods;
  }

  @NotNull
  public static Method getInheritedPrivateMethod(@NotNull Class<?> clazz, @NotNull String name, @NotNull Class<?>... parameterTypes) throws NoSuchMethodException {
    for (Class<?> current : ClassWalker.walk(clazz)) {
      try {
        return current.getDeclaredMethod(name, parameterTypes);
      } catch (Throwable ex) {
      }
    }

    throw new NoSuchMethodException(name);
  }

  @NotNull
  public static Field getInheritedPrivateField(@NotNull Class<?> clazz, @NotNull String name) throws NoSuchFieldException {
    for (Class<?> current : ClassWalker.walk(clazz)) {
      try {
        return current.getDeclaredField(name);
      } catch (Throwable ex) {
      }
    }

    throw new NoSuchFieldException(name);
  }

  /**
   * Iterates through an array which may contain primitive data types or non primitive data types and performs the given action on each element.
   * Because we can't just cast such an array to {@code Object[]}, we have to use some reflections.
   *
   * @param array The target array, as {@link Object}; Can't use an array type here.
   * @param <T>   The type of data we will cast the content to. Use {@link Object} if the it's unknown.
   * @throws IllegalArgumentException If the {@code array} is not an actual array
   * @see Array
   * @see Array#getLength(Object)
   * @see Array#get(Object, int)
   */
  public static <T> void forEachInArray(@NotNull Object array, @NotNull Consumer<T> action) {
    ReflectionUtils.<T>iterableArray(array).forEach(action);
  }

  @NotNull
  @CheckReturnValue
  public static <T> Iterable<T> iterableArray(@NotNull Object array) {
    return ArrayWalker.walk(array);
  }

  public static Class<?> getCaller() {
    return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
      .walk(stream -> stream
        .skip(2) // skip frame 0 (getCaller) and frame 1 (the intermediate method)
        .findFirst()
        .map(StackWalker.StackFrame::getDeclaringClass)
        .orElseThrow(() -> new IllegalStateException("Stack not deep enough to find caller"))
      );
  }

  @NotNull
  public static String getCallerName(int skip) {
    StackTraceElement[] trace = Thread.currentThread().getStackTrace();
    StackTraceElement element = trace[3 + skip];

    String className = StringUtils.getAfterLastIndex(element.getClassName(), ".");
    return className + "." + element.getMethodName();
  }

  @NotNull
  public static String getCallerName() {
    return getCallerName(1);
  }

  /**
   * Takes a {@link Enum} and returns the corresponding {@link Field} using {@link Class#getField(String)}
   *
   * @see Class#getField(String)
   */
  @NotNull
  public static Field getEnumAsField(@NotNull Enum<?> enun) {
    Class<?> classOfEnum = enun.getClass();

    try {
      return classOfEnum.getField(enun.name());
    } catch (NoSuchFieldException ex) {
      throw new WrappedException(ex);
    }
  }

  /**
   * @see Field#getAnnotations()
   */
  @NotNull
  public static <E extends Enum<?>> Annotation[] getEnumAnnotations(@NotNull E enun) {
    Field field = getEnumAsField(enun);
    return field.getAnnotations();
  }

  /**
   * @return Returns {@code null} if no annotation of this class is present
   * @see Field#getAnnotation(Class)
   */
  public static <E extends Enum<?>, A extends Annotation> A getEnumAnnotation(@NotNull E enun, Class<A> classOfAnnotation) {
    Field field = getEnumAsField(enun);
    return field.getAnnotation(classOfAnnotation);
  }

  @Nullable
  public static <E extends Enum<E>> E getEnumOrNull(@Nullable String name, @NotNull Class<E> classOfEnum) {
    try {
      if (name == null) return null;
      return Enum.valueOf(classOfEnum, name);
    } catch (Exception ex) {
      return null;
    }
  }

  @Nullable
  @SuppressWarnings("unchecked")
  public static <T> Class<T> getClassOrNull(@Nullable String name) {
    try {
      if (name == null) return null;
      return (Class<T>) Class.forName(name);
    } catch (Exception ex) {
      return null;
    }
  }

  @Nullable
  @SuppressWarnings("unchecked")
  public static <T> Class<T> getClassOrNull(@Nullable String name, boolean initialize, @NotNull ClassLoader classLoader) {
    try {
      if (name == null) return null;
      return (Class<T>) Class.forName(name, initialize, classLoader);
    } catch (Exception ex) {
      return null;
    }
  }

  @Nullable
  @SuppressWarnings("unchecked")
  public static <T> T invokeMethodOrNull(@Nullable Object instance, @NotNull Method method) {
    try {
      if (!method.isAccessible()) method.setAccessible(true);
      return (T) method.invoke(instance);
    } catch (Throwable ex) {
      return null;
    }
  }

  @Nullable
  public static <T> T invokeStaticMethodOrNull(@NotNull Class<?> clazz, @NotNull String method) {
    try {
      return invokeMethodOrNull(null, clazz.getMethod(method));
    } catch (NoSuchMethodException ex) {
      return null;
    }
  }

  @Nullable
  public static <T> T invokeMethodOrNull(@NotNull Object instance, @NotNull String method) {
    try {
      return invokeMethodOrNull(instance, instance.getClass().getDeclaredMethod(method));
    } catch (NoSuchMethodException ex) {
      return null;
    }
  }

  @Nullable
  public static <T> T getAnnotationValue(@NotNull Annotation annotation) {
    return invokeMethodOrNull(annotation, "value");
  }

  @Nullable
  public static <E extends Enum<?>> E getEnumByAlternateNames(@NotNull Class<E> classOfE, @NotNull String input) {
    E[] values = invokeStaticMethodOrNull(classOfE, "values");
    String[] methodNames = {"getName", "getNames", "getAlias", "getAliases", "getKey", "getKeys", "name", "toString", "ordinal", "getId", "id"};
    for (E value : values) {
      for (String method : methodNames) {
        if (check(input, invokeMethodOrNull(value, method)))
          return value;
      }
    }

    return null;
  }

  private static boolean check(@NotNull String input, @Nullable Object value) {
    if (value == null) return false;
    if (value.getClass().isArray()) {
      for (Object key : iterableArray(value)) {
        if (input.equalsIgnoreCase(String.valueOf(key)))
          return true;
      }
    }
    return input.equalsIgnoreCase(String.valueOf(value));
  }

}
