package net.cyperia.commons.common.collection;

import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class FontBuilder {

  private Font font;

  public FontBuilder(@NotNull File file) throws IOException, FontFormatException {
    this(file, Font.TRUETYPE_FONT);
  }

  public FontBuilder(@NotNull File file, int type) throws IOException, FontFormatException {
    this.font = Font.createFont(type, file);
  }

  public FontBuilder(@NotNull String resource) throws IOException, FontFormatException {
    this(resource, Font.TRUETYPE_FONT);
  }

  public FontBuilder(@NotNull String resource, int type) throws IOException, FontFormatException {
    this.font = Font.createFont(type, Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(resource)));
  }

  @NotNull
  @CheckReturnValue
  public FontBuilder bold() {
    return style(Font.BOLD);
  }

  @NotNull
  @CheckReturnValue
  public FontBuilder italic() {
    return style(Font.ITALIC);
  }

  @NotNull
  @CheckReturnValue
  public FontBuilder style(int style) {
    font = font.deriveFont(style);
    return this;
  }

  @NotNull
  @CheckReturnValue
  public FontBuilder size(float size) {
    font = font.deriveFont(size);
    return this;
  }

  @NotNull
  @CheckReturnValue
  public FontBuilder derive(int style, float size) {
    font = font.deriveFont(style, size);
    return this;
  }

  @NotNull
  public Font build() {
    registerFont(font);
    return font;
  }

  public static void registerFont(@NotNull Font font) {
    GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
  }

  @NotNull
  @CheckReturnValue
  public static FontBuilder fromFile(@NotNull String filename) {
    try {
      return new FontBuilder(new File(filename));
    } catch (Exception ex) {
      throw new WrappedException(ex);
    }
  }

  @NotNull
  @CheckReturnValue
  public static FontBuilder fromResource(@NotNull String resource) {
    try {
      return new FontBuilder(resource);
    } catch (Exception ex) {
      throw new WrappedException(ex);
    }
  }

}
