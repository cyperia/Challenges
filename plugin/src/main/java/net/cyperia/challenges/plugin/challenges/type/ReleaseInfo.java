package net.cyperia.challenges.plugin.challenges.type;

import net.cyperia.challenges.catalog.Challenge;
import net.cyperia.challenges.catalog.Release;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.custom.CustomChallenge;
import net.cyperia.challenges.plugin.challenges.custom.settings.ChallengeSetting;
import net.cyperia.challenges.plugin.challenges.type.abstraction.AbstractChallenge;
import net.cyperia.challenges.plugin.challenges.type.abstraction.menu.MenuSetting;
import net.cyperia.challenges.plugin.challenges.type.annotation.Since;
import net.cyperia.commons.common.version.Version;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// A challenge, trigger or action has its release in the catalog; anything else shown in a menu may carry @Since
public final class ReleaseInfo {

  private static final Release NONE = new Release(null, null, false);

  private ReleaseInfo() {
  }

  @NotNull
  public static Release of(@NotNull Object menuEntry) {
    if (menuEntry instanceof CustomChallenge) return NONE;
    if (menuEntry instanceof AbstractChallenge challenge) {
      Challenge entry = challenge.getCatalogEntry();
      return challenge instanceof MenuSetting setting ? withSubSettings(entry.release(), setting.getNewestSubSettingSince()) : entry.release();
    }
    if (menuEntry instanceof ChallengeSetting setting) return setting.getCatalogEntry().release();
    if (menuEntry instanceof MenuSetting.SubSetting subSetting) return new Release(subSetting.getSince(), null, false);
    Since since = menuEntry.getClass().getAnnotation(Since.class);
    return since == null ? NONE : new Release(since.value(), null, false);
  }

  // A sub setting that came with its setting is no update of it
  @NotNull
  static Release withSubSettings(@NotNull Release release, @Nullable String newestSubSetting) {
    if (newestSubSetting == null) return release;
    Version subSetting = Version.parse(newestSubSetting);
    if (!subSetting.isNewerThan(parse(release.since())) || !subSetting.isNewerThan(parse(release.updated()))) return release;
    return new Release(release.since(), newestSubSetting, release.beta());
  }

  @NotNull
  public static Version getSince(@NotNull Object menuEntry) {
    return parse(of(menuEntry).since());
  }

  @NotNull
  public static Version getUpdated(@NotNull Object menuEntry) {
    return parse(of(menuEntry).updated());
  }

  public static boolean isBeta(@NotNull Object menuEntry) {
    return of(menuEntry).beta();
  }

  public static boolean isNew(@NotNull Object menuEntry) {
    return of(menuEntry).isNew();
  }

  public static boolean isUpdated(@NotNull Object menuEntry) {
    return of(menuEntry).isUpdated();
  }

  public static boolean isVersionNew(@NotNull Version challengeVersion) {
    return Release.reached(challengeVersion.toString(), Challenges.getInstance().getVersion().toString());
  }

  @NotNull
  private static Version parse(String version) {
    return version == null ? Version.FALLBACK : Version.parse(version);
  }

}
