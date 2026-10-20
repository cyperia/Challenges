package net.cyperia.challenges.catalog;

import org.jetbrains.annotations.NotNull;

import java.util.List;

// A trigger or an action custom challenges and presets are built from; fallbackIds are ids it was saved under before
public record CustomSetting(@NotNull String id, @NotNull String className, @NotNull Icon icon,
                            @NotNull List<String> fallbackIds, @NotNull Release release) {
}
