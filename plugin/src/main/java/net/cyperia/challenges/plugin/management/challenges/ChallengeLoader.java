package net.cyperia.challenges.plugin.management.challenges;

import net.cyperia.challenges.catalog.Challenge;
import net.cyperia.challenges.catalog.ChallengeCatalog;
import net.cyperia.challenges.catalog.Definition;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.implementation.LockedChallenge;
import net.cyperia.challenges.plugin.challenges.implementation.damage.DamageRuleSetting;
import net.cyperia.challenges.plugin.challenges.implementation.material.BlockMaterialSetting;
import net.cyperia.challenges.plugin.challenges.type.IChallenge;
import net.cyperia.commons.bukkit.utils.logging.Logger;
import org.bukkit.Material;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class ChallengeLoader extends ModuleChallengeLoader {

  public ChallengeLoader() {
    super(Challenges.getInstance());
  }

  public void enable() {
    register(Challenges.getInstance().getCatalog(), getClass().getClassLoader());
  }

  // A module bundles a catalog of its own and registers the challenges in it with this, passing its class loader
  public void registerModule(@NotNull ClassLoader moduleLoader) {
    ChallengeCatalog module = ChallengeCatalog.load(moduleLoader);
    Challenges.getInstance().extendCatalog(module);
    register(module, moduleLoader);
  }

  private void register(@NotNull ChallengeCatalog catalog, @NotNull ClassLoader loader) {
    PresetChallengeLoader presets = Challenges.getInstance().getPresetChallengeLoader();
    for (Challenge entry : catalog.challenges()) {
      if (!entry.registered()) continue;
      switch (entry.definition()) {
        case Definition.Implementation implementation -> register(entry, implementation, loader);
        case Definition.Preset preset -> presets.preset(entry, preset);
        case Definition.DamageRule rule -> register(new DamageRuleSetting(entry, causes(rule)));
        case Definition.MaterialRule rule -> register(new BlockMaterialSetting(entry, materials(rule)));
      }
    }
  }

  private void register(@NotNull Challenge entry, @NotNull Definition.Implementation implementation, @NotNull ClassLoader loader) {
    String requiredPlugin = implementation.requiredPlugin();
    if (requiredPlugin != null && !plugin.getServer().getPluginManager().isPluginEnabled(requiredPlugin)) {
      Logger.debug("Did not register challenge {}, requires plugin {}", entry.id(), requiredPlugin);
      return;
    }
    Class<?> type;
    try {
      type = Class.forName(implementation.className(), true, loader);
    } catch (ClassNotFoundException ex) {
      // The public release ships without beta classes and shows their entries locked
      if (entry.release().beta()) {
        register(new LockedChallenge(entry));
      } else {
        Logger.error("The class {} of challenge {} does not exist", implementation.className(), entry.id());
      }
      return;
    }
    try {
      IChallenge challenge = (IChallenge) type.getDeclaredConstructor().newInstance();
      register(challenge);
    } catch (Throwable ex) {
      Logger.error("Could not create challenge {}", entry.id(), ex);
    }
  }

  // A rule may name materials of a newer version than the server runs, those are left out
  @NotNull
  private static Material[] materials(@NotNull Definition.MaterialRule rule) {
    return rule.materials().stream().map(Material::matchMaterial).filter(Objects::nonNull).toArray(Material[]::new);
  }

  @NotNull
  private static DamageCause[] causes(@NotNull Definition.DamageRule rule) {
    if (rule.causes().contains(Definition.DamageRule.EVERY_CAUSE)) return DamageCause.values();
    return rule.causes().stream().map(DamageCause::valueOf).toArray(DamageCause[]::new);
  }

}
