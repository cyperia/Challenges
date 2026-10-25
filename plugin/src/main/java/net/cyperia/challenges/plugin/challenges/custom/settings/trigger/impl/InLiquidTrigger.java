package net.cyperia.challenges.plugin.challenges.custom.settings.trigger.impl;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.custom.settings.sub.SelectableKey;
import net.cyperia.challenges.plugin.challenges.custom.settings.sub.SubSettingsBuilder;
import net.cyperia.challenges.plugin.challenges.custom.settings.trigger.ChallengeTrigger;
import net.cyperia.challenges.plugin.challenges.type.helper.SubSettingsHelper;
import net.cyperia.challenges.plugin.management.scheduler.task.ScheduledTask;
import net.cyperia.challenges.plugin.utils.misc.BlockUtils;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;

public class InLiquidTrigger extends ChallengeTrigger {

  public InLiquidTrigger(String name) {
    super(name, SubSettingsBuilder.createChooseMultipleItem(SubSettingsHelper.LIQUID).fill(builder -> {
      builder.addSetting(SelectableKey.ofName("LAVA", Material.LAVA_BUCKET, "liquid.lava"));
      builder.addSetting(SelectableKey.ofName("WATER", Material.WATER_BUCKET, "liquid.water"));
    }));
    Challenges.getInstance().getScheduler().register(this);
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onMove(PlayerMoveEvent event) {
    if (BlockUtils.isSameBlockLocation(event.getTo(), event.getFrom())) return;
    if (event.getTo() == null) return;

    Material type = event.getTo().getBlock().getType();
    Material oldType = event.getFrom().getBlock().getType();
    if ((type == Material.WATER ||
      type == Material.LAVA) &&
      oldType != Material.WATER &&
      oldType != Material.LAVA) {
      createData()
        .entity(event.getPlayer())
        .data(SubSettingsHelper.LIQUID, type.name())
        .execute();
    }

  }

  @ScheduledTask(ticks = 5, async = false)
  public void onFifthTick() {
    Challenges.getInstance().getPlayerManager().forEachChallengePlayer(player -> {
      Material type = player.getLocation().getBlock().getType();
      if (type == Material.WATER ||
        type == Material.LAVA) {
        createData()
          .entity(player)
          .data("liquid", type.name())
          .execute();
      }
    });
  }

}
