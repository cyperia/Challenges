package net.cyperia.challenges.plugin.challenges.custom.settings.action;

import net.cyperia.challenges.plugin.challenges.custom.settings.sub.SubSettingsBuilder;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.function.Supplier;

public abstract class PlayerTargetAction extends EntityTargetAction {

  public PlayerTargetAction(String name, SubSettingsBuilder subSettingsBuilder) {
    super(name, subSettingsBuilder);
  }

  public PlayerTargetAction(String name) {
    super(name);
  }

  public PlayerTargetAction(String name, Supplier<SubSettingsBuilder> builderSupplier) {
    super(name, builderSupplier);
  }

  @Override
  public void executeFor(Entity entity, Map<String, String[]> subActions) {
    // Entity is null if the target entity is the console
    if (entity instanceof Player || entity == null) {
      executeForPlayer(((Player) entity), subActions);
    }
  }

  public abstract void executeForPlayer(Player player, Map<String, String[]> subActions);

}
