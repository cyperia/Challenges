package net.cyperia.challenges.plugin.challenges.implementation.goal.forcebattle;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.implementation.goal.forcebattle.targets.*;
import net.cyperia.challenges.plugin.challenges.type.abstraction.ForceBattleDisplayGoal;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKeySpace;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.scheduler.policy.TimerPolicy;
import net.cyperia.challenges.plugin.management.scheduler.task.ScheduledTask;
import net.cyperia.challenges.plugin.utils.misc.InventoryUtils;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.common.config.Document;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

public class ExtremeForceBattleGoal extends ForceBattleDisplayGoal<ForceTarget<?>> {

  private final BooleanSubSetting giveItemSetting, giveBlockSetting;

  public ExtremeForceBattleGoal() {

    giveItemSetting = registerSetting("give-item", new BooleanSubSetting(
      new ItemStack(Material.CHEST), MessageKeySpace.of("challenge.force-item-battle.sub.give-item"),
      false
    ));
    giveBlockSetting = registerSetting("give-block", new BooleanSubSetting(
      new ItemStack(Material.CHEST), MessageKeySpace.of("challenge.force-block-battle.sub.give-block"),
      false
    ));
  }

  @Override
  protected ForceTarget<?>[] getTargetsPossibleToFind() {
    //Currently not needed as the setRandomTarget logic of the ForceBattleGoal class is not used
    return new ForceTarget<?>[0];
  }

  @ScheduledTask(ticks = 5, async = false, timerPolicy = TimerPolicy.STARTED)
  public void checkTargets() {
    for (Map.Entry<UUID, ForceTarget<?>> entry : currentTarget.entrySet()) {
      UUID uuid = entry.getKey();
      ForceTarget<?> target = entry.getValue();
      Player player = Bukkit.getPlayer(uuid);
      if (player == null || ignorePlayer(player)) continue;

      if (target.check(player)) {
        handleTargetFound(player);
      }
    }
  }

  @Override
  public ForceTarget<?> getTargetFromDocument(Document document, String path) {
    Document targetDocument = document.getDocument(path);
    String targetTypeString = targetDocument.getString("type");
    Object value = targetDocument.getObject("value");

    TargetType targetType = TargetType.valueOf(targetTypeString);
    return targetType.parse().apply(value);
  }

  @Override
  public List<ForceTarget<?>> getListFromDocument(Document document, String path) {
    List<Document> targetDocuments = document.getDocumentList(path);
    List<ForceTarget<?>> targets = new ArrayList<>();
    for (Document targetDocument : targetDocuments) {
      String targetTypeString = targetDocument.getString("type");
      Object value = targetDocument.getObject("value");

      TargetType targetType = TargetType.valueOf(targetTypeString);
      targets.add(targetType.parse().apply(value));
    }
    return targets;
  }

  @Override
  public void setTargetInDocument(Document document, String path, ForceTarget<?> target) {
    document.set(path, Document.create().set("type", target.getType().name()).set("value", target.getTargetSaveObject()));
  }

  @Override
  public void setFoundListInDocument(Document document, String path, List<ForceTarget<?>> targets) {
    List<Document> documents = new ArrayList<>();
    for (ForceTarget<?> forceTarget : targets) {
      documents.add(Document.create().set("type", forceTarget.getType().name()).set("value", forceTarget.getTargetSaveObject()));
    }
    document.set(path, documents);
  }

  @Override
  public void setRandomTarget(Player player) {
    updateDisplayStand(player);

    TargetType targetType = globalRandom.choose(TargetType.values());
    ForceTarget<?> newTarget = targetType.getRandomTarget().apply(player);

    if (newTarget instanceof AdvancementTarget advancementTarget) {
      advancementTarget.resetIfDone(player);
    }

    currentTarget.put(player.getUniqueId(), newTarget);
    getNewTargetMessage(newTarget).send(player, Prefix.CHALLENGES, newTarget.getDisplayName());
    SoundSample.PLING.play(player);

    updateScoreboard();
  }

  @NotNull
  @Override
  protected LocalizableMessage getScoreboardTargetDisplay(@NotNull ForceTarget<?> target) {
    // targets are drawn from every type here, so the sidebar has to say which kind it is
    return target.getTypedDisplayName();
  }

  @Override
  protected boolean shouldRegisterDupedTargetsSetting() {
    return false;
  }

  @Override
  public void handleJokerUse(Player player) {
    ForceTarget<?> target = currentTarget.get(player.getUniqueId());
    if (shouldGiveItemOnSkip() && target instanceof ItemTarget itemTarget) {
      InventoryUtils.dropOrGiveItem(player.getInventory(), player.getLocation(), itemTarget.getTarget());
    } else if (shouldBlockOnSkip() && target instanceof BlockTarget blockTarget) {
      InventoryUtils.dropOrGiveItem(player.getInventory(), player.getLocation(), blockTarget.getTarget());
    }
    super.handleJokerUse(player);
  }

  public enum TargetType {
    ITEM(object -> {
      return new ItemTarget(Material.valueOf((String) object));
    }, player -> {
      return new ItemTarget(globalRandom.choose(ItemTarget.getPossibleItems()));
    }),
    BLOCK(object -> {
      return new BlockTarget(Material.valueOf((String) object));
    }, player -> {
      return new BlockTarget(globalRandom.choose(BlockTarget.getPossibleBlocks()));
    }),
    HEIGHT(object -> {
      return new HeightTarget(Integer.valueOf((String) object));
    }, player -> {
      World world = ChallengeAPI.getGameWorld(World.Environment.NORMAL);
      int height = globalRandom.range(world.getMinHeight(), world.getMaxHeight());
      return new HeightTarget(height);
    }),
    MOB(object -> {
      return new MobTarget(EntityType.valueOf((String) object));
    }, player -> {
      return new MobTarget(globalRandom.choose(MobTarget.getPossibleMobs()));
    }),
    BIOME(BiomeTarget::fromSaved, player -> {
      return new BiomeTarget(globalRandom.choose(BiomeTarget.getPossibleBiomes()));
    }),
    DAMAGE(object -> {
      return new DamageTarget(Integer.valueOf((String) object));
    }, player -> {
      return new DamageTarget(globalRandom.range(1, 19));
    }),
    ADVANCEMENT(object -> {
      return new AdvancementTarget(Bukkit.getAdvancement(Objects.requireNonNull(NamespacedKey.fromString((String) object))));
    }, player -> {
      return new AdvancementTarget(globalRandom.choose(AdvancementTarget.getPossibleAdvancements()));
    }),
    POSITION(object -> {
      Map<?, ?> position = (Map<?, ?>) object;
      return new PositionTarget(((Number) position.get("x")).doubleValue(), ((Number) position.get("z")).doubleValue());
    }, PositionTarget::new);

    private final Function<Object, ForceTarget<?>> parseFunction;
    private final Function<Player, ForceTarget<?>> randomTargetFunction;

    TargetType(Function<Object, ForceTarget<?>> parseFunction, Function<Player, ForceTarget<?>> randomTargetFunction) {
      this.parseFunction = parseFunction;
      this.randomTargetFunction = randomTargetFunction;
    }

    public Function<Object, ForceTarget<?>> parse() {
      return parseFunction;
    }

    public Function<Player, ForceTarget<?>> getRandomTarget() {
      return randomTargetFunction;
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onKill(@NotNull EntityDeathEvent event) {
    LivingEntity entity = event.getEntity();
    Player killer = entity.getKiller();
    if (killer == null) return;
    if (!shouldExecuteEffect(killer)) return;
    if (currentTarget.get(killer.getUniqueId()) == null) return;
    if (currentTarget.get(killer.getUniqueId()) instanceof MobTarget) {
      MobTarget target = (MobTarget) currentTarget.get(killer.getUniqueId());
      if (target.getTarget() != entity.getType()) return;
      handleTargetFound(killer);
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onDamage(@NotNull EntityDamageEvent event) {
    if (!(event.getEntity() instanceof Player player)) return;
    if (!shouldExecuteEffect(player)) return;
    if (currentTarget.get(player.getUniqueId()) == null) return;
    if (currentTarget.get(player.getUniqueId()) instanceof DamageTarget) {
      DamageTarget target = (DamageTarget) currentTarget.get(player.getUniqueId());
      int damage = (int) ChallengeHelper.getFinalDamage(event);
      if (damage != target.getTarget()) return;
      handleTargetFound(player);
    }
  }

  private boolean shouldGiveItemOnSkip() {
    return giveItemSetting.getAsBoolean();
  }

  private boolean shouldBlockOnSkip() {
    return giveBlockSetting.getAsBoolean();
  }

}
