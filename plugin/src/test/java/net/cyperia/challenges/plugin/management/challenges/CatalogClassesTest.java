package net.cyperia.challenges.plugin.management.challenges;

import net.cyperia.challenges.catalog.Challenge;
import net.cyperia.challenges.catalog.ChallengeCatalog;
import net.cyperia.challenges.catalog.CustomSetting;
import net.cyperia.challenges.catalog.Definition;
import net.cyperia.challenges.catalog.Icon;
import net.cyperia.challenges.plugin.challenges.custom.settings.action.ChallengeAction;
import net.cyperia.challenges.plugin.challenges.custom.settings.trigger.ChallengeTrigger;
import net.cyperia.challenges.plugin.challenges.implementation.LockedChallenge;
import net.cyperia.challenges.plugin.challenges.implementation.damage.DamageRuleSetting;
import net.cyperia.challenges.plugin.challenges.implementation.material.BlockMaterialSetting;
import net.cyperia.challenges.plugin.challenges.implementation.preset.PresetChallenge;
import net.cyperia.challenges.plugin.challenges.implementation.preset.PresetModifierChallenge;
import net.cyperia.challenges.plugin.challenges.type.IChallenge;
import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import org.bukkit.Material;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CatalogClassesTest {

  private static final String IMPLEMENTATION = "net.cyperia.challenges.plugin.challenges.implementation";
  // Registered once per catalog entry of their own kind, so no entry names them
  private static final Set<Class<?>> MULTI_INSTANCE = Set.of(LockedChallenge.class, PresetChallenge.class,
    PresetModifierChallenge.class, DamageRuleSetting.class, BlockMaterialSetting.class);

  private static final boolean PUBLIC_RELEASE = Boolean.getBoolean("challenges.publicRelease");

  private final ChallengeCatalog catalog = ChallengeCatalog.load().requireComplete();

  @Test
  void everyChallengeClassExistsWithAConstructorWithoutParameters() throws ReflectiveOperationException {
    for (Challenge entry : catalog.challenges()) {
      if (!(entry.definition() instanceof Definition.Implementation implementation)) continue;
      if (PUBLIC_RELEASE && entry.release().beta()) continue;
      Class<?> type = load(implementation.className());
      assertTrue(IChallenge.class.isAssignableFrom(type), type + " is no challenge");
      assertFalse(Modifier.isAbstract(type.getModifiers()), type + " is abstract");
      type.getDeclaredConstructor();
    }
  }

  @Test
  void everyTriggerAndActionClassExistsWithAnIdConstructor() throws ReflectiveOperationException {
    for (CustomSetting entry : catalog.triggers()) {
      if (PUBLIC_RELEASE && entry.release().beta()) continue;
      assertTrue(ChallengeTrigger.class.isAssignableFrom(load(entry.className())), entry.id());
      load(entry.className()).getConstructor(String.class);
    }
    for (CustomSetting entry : catalog.actions()) {
      if (PUBLIC_RELEASE && entry.release().beta()) continue;
      assertTrue(ChallengeAction.class.isAssignableFrom(load(entry.className())), entry.id());
      load(entry.className()).getConstructor(String.class);
    }
  }

  @Test
  void everyConcreteChallengeClassHasAnEntry() throws IOException, URISyntaxException, ClassNotFoundException {
    Path classes = Path.of(IChallenge.class.getProtectionDomain().getCodeSource().getLocation().toURI());
    Path root = classes.resolve(IMPLEMENTATION.replace('.', '/'));
    List<String> names;
    try (Stream<Path> files = Files.walk(root)) {
      names = files.map(file -> classes.relativize(file).toString())
        .filter(file -> file.endsWith(".class") && !file.contains("$"))
        .map(file -> file.substring(0, file.length() - ".class".length()).replace('/', '.').replace('\\', '.'))
        .toList();
    }
    for (String name : names) {
      Class<?> type = load(name);
      boolean concreteChallenge = IChallenge.class.isAssignableFrom(type) && !type.isInterface()
        && !Modifier.isAbstract(type.getModifiers()) && !MULTI_INSTANCE.contains(type);
      if (concreteChallenge) assertTrue(catalog.challengeOf(name).isPresent(), name + " has no catalog entry");
    }
  }

  @Test
  void everyNamedConstantExists() throws NoSuchFieldException {
    Stream.of(catalog.challenges().stream().map(Challenge::icon), catalog.triggers().stream().map(CustomSetting::icon),
        catalog.actions().stream().map(CustomSetting::icon), catalog.categories().stream().map(category -> category.icon()))
      .flatMap(icons -> icons)
      .map(Icon::material)
      .forEach(Material::valueOf);
    for (Challenge entry : catalog.challenges()) {
      switch (entry.definition()) {
        case Definition.DamageRule rule -> rule.causes().stream()
          .filter(cause -> !cause.equals(Definition.DamageRule.EVERY_CAUSE)).forEach(DamageCause::valueOf);
        case Definition.MaterialRule rule -> rule.materials().forEach(Material::valueOf);
        case Definition.Preset preset when preset.value() != null ->
          assertEquals(ValueUnit.class, ValueUnit.class.getField(preset.value().unit()).getType());
        default -> {
        }
      }
    }
  }

  private static Class<?> load(String name) throws ClassNotFoundException {
    return Class.forName(name, false, CatalogClassesTest.class.getClassLoader());
  }

}
