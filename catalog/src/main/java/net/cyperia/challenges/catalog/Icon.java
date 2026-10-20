package net.cyperia.challenges.catalog;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// material is a Material constant; rgb tints a potion or leather item
public record Icon(@NotNull String material, @Nullable Integer rgb) {
}
