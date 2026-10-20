package net.cyperia.commons.common.annotations;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * @see Deprecated
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.PACKAGE, ElementType.PARAMETER, ElementType.TYPE})
public @interface DeprecatedSince {

  @NotNull
  String value();

}
