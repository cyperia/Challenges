package net.cyperia.challenges.plugin.challenges.type;

import net.cyperia.challenges.catalog.Release;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.custom.CustomChallenge;
import net.cyperia.challenges.plugin.challenges.custom.settings.ChallengeSetting;
import net.cyperia.challenges.plugin.challenges.type.abstraction.AbstractChallenge;
import net.cyperia.challenges.plugin.challenges.type.annotation.Since;
import net.cyperia.commons.common.version.Version;
import org.jetbrains.annotations.NotNull;

// A challenge, trigger or action has its release in the catalog; anything else shown in a menu may carry @Since
public final class ReleaseInfo {

  private static final Release NONE = new Release(null, null, false);

  private ReleaseInfo() {
  }

  @NotNull
  public static Release of(@NotNull Object menuEntry) {
    if (menuEntry instanceof CustomChallenge) return NONE;
    if (menuEntry instanceof AbstractChallenge challenge) return challenge.getCatalogEntry().release();
    if (menuEntry instanceof ChallengeSetting setting) return setting.getCatalogEntry().release();
    Since since = menuEntry.getClass().getAnnotation(Since.class);
    return since == null ? NONE : new Release(since.value(), null, false);
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
    Version pluginVersion = Challenges.getInstance().getVersion();
    return challengeVersion.isNewerOrEqualThan(pluginVersion);
  }

  @NotNull
  private static Version parse(String version) {
    return version == null ? Version.FALLBACK : Version.parse(version);
  }

}
