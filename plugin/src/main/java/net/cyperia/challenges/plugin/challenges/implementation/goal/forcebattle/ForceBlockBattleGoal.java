package net.cyperia.challenges.plugin.challenges.implementation.goal.forcebattle;

import net.cyperia.challenges.plugin.challenges.implementation.goal.forcebattle.targets.BlockTarget;
import net.cyperia.challenges.plugin.challenges.type.abstraction.ForceBattleDisplayGoal;
import net.cyperia.challenges.plugin.management.scheduler.policy.TimerPolicy;
import net.cyperia.challenges.plugin.management.scheduler.task.ScheduledTask;
import net.cyperia.challenges.plugin.utils.misc.InventoryUtils;
import net.cyperia.commons.common.config.Document;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

public class ForceBlockBattleGoal extends ForceBattleDisplayGoal<BlockTarget> {

  private final BooleanSubSetting giveBlockSetting;

  public ForceBlockBattleGoal() {

    giveBlockSetting = registerSetting("give-block", new BooleanSubSetting(
      new ItemStack(Material.CHEST), getSubSettingSpace("give-block"),
      false
    ));
  }

  @Override
  protected BlockTarget[] getTargetsPossibleToFind() {
    List<Material> materials = BlockTarget.getPossibleBlocks();
    return materials.stream().map(BlockTarget::new).toArray(BlockTarget[]::new);
  }

  @Override
  public BlockTarget getTargetFromDocument(Document document, String path) {
    return new BlockTarget(document.getEnum(path, Material.class));
  }

  @Override
  public List<BlockTarget> getListFromDocument(Document document, String path) {
    return document.getEnumList(path, Material.class).stream().map(BlockTarget::new).collect(Collectors.toList());
  }

  @Override
  public void handleJokerUse(Player player) {
    super.handleJokerUse(player);
    if (shouldGiveBlockOnSkip()) {
      InventoryUtils.dropOrGiveItem(player.getInventory(), player.getLocation(), currentTarget.get(player.getUniqueId()).getTarget());
    }
  }

  @ScheduledTask(ticks = 5, async = false, timerPolicy = TimerPolicy.STARTED)
  public void checkBlocks() {
    if (!shouldExecuteEffect()) return;
    broadcastFiltered(player -> {
      BlockTarget target = currentTarget.get(player.getUniqueId());
      if (target != null && target.check(player)) {
        handleTargetFound(player);
      }
    });
  }

  private boolean shouldGiveBlockOnSkip() {
    return giveBlockSetting.getAsBoolean();
  }

}
