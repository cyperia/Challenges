package net.cyperia.challenges.plugin.challenges.custom.settings;

import net.cyperia.challenges.catalog.CustomSetting;
import net.cyperia.challenges.plugin.challenges.custom.settings.sub.SubSettingsBuilder;
import net.cyperia.challenges.plugin.management.menu.format.ItemFormatter;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public abstract class ChallengeSetting implements IChallengeSetting {

  private final String name;
  private final SubSettingsBuilder subSettingsBuilder;

  public ChallengeSetting(String name, SubSettingsBuilder subSettingsBuilder) {
    this.name = name;
    this.subSettingsBuilder = subSettingsBuilder.build();
  }

  public ChallengeSetting(String name) {
    this(name, SubSettingsBuilder.createEmpty());
  }

  public ChallengeSetting(String name, Supplier<SubSettingsBuilder> builderSupplier) {
    this(name, builderSupplier.get());
  }

  @NotNull
  public abstract CustomSetting getCatalogEntry();

  @NotNull
  public Material getMaterial() {
    return Material.valueOf(getCatalogEntry().icon().material());
  }

  @NotNull
  public String getRelativeMessageKey() {
    return name.toLowerCase();
  }

  @NotNull
  @Override
  public ItemFormatter getDisplayFormat() {
    return new ItemFormatter(getMaterial())
      .name(getSettingName())
      .description(getSettingDescription());
  }

  @NotNull
  @Override
  public final String getKey() {
    return this.getUniqueName();
  }

  @NotNull
  @Override
  public SubSettingsBuilder getSubSettingsBuilder() {
    return subSettingsBuilder;
  }

  @NotNull
  @Override
  public String getUniqueName() {
    return name;
  }

}
