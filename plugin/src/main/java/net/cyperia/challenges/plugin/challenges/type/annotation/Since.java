package net.cyperia.challenges.plugin.challenges.type.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Since {

  @NotNull
  String value();

}
