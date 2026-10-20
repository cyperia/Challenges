package net.cyperia.commons.common.annotations;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * Used to declare alternate names which are used in used or similar libraries.
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
public @interface AlsoKnownAs {

  @NotNull
  String[] value();

}
