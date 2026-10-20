package net.cyperia.challenges.catalog;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public sealed interface Definition {

  // A class of its own; requiredPlugin is a plugin that has to be enabled for it to register
  record Implementation(@NotNull String className, @NotNull List<String> commands,
                        @Nullable String requiredPlugin) implements Definition {
  }

  // A trigger and an action; a value is set by the player and filled into a setting whose value is ${value}
  record Preset(@NotNull String trigger, @NotNull Map<String, List<String>> triggerSettings,
                @NotNull String action, @NotNull Map<String, List<String>> actionSettings,
                @Nullable Value value) implements Definition {

    // unit is the name of a ValueUnit constant
    public record Value(@NotNull String unit, int min, int max, int defaultValue) {
    }

  }

  // causes are DamageCause constants, "*" stands for every cause
  record DamageRule(@NotNull List<String> causes) implements Definition {

    public static final String EVERY_CAUSE = "*";

  }

  // materials are Material constants
  record MaterialRule(@NotNull List<String> materials) implements Definition {
  }

}
