package net.cyperia.challenges.plugin.challenges.custom.settings.trigger.impl;

import net.cyperia.challenges.plugin.challenges.custom.settings.trigger.ChallengeTrigger;
import net.cyperia.challenges.plugin.challenges.type.helper.SubSettingsHelper;
import net.cyperia.challenges.plugin.utils.misc.BlockUtils;
import net.cyperia.challenges.plugin.utils.misc.ExperimentalUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class StandsNotOnSpecificBlockTrigger extends ChallengeTrigger {

  public StandsNotOnSpecificBlockTrigger(String name) {
    super(name, SubSettingsHelper.createBlockSettingsBuilder());
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onMove(PlayerMoveEvent event) {
    if (BlockUtils.isSameBlockLocation(event.getTo(), event.getFrom())) return;
    if (event.getTo() == null) return;
    Block blockBelow = BlockUtils.getBlockBelow(event.getTo());
    if (blockBelow == null) return;

    Material type = blockBelow.getType();

    List<Material> materials = new LinkedList<>(Arrays.asList(ExperimentalUtils.getMaterials()));
    materials.remove(type);
    materials.removeIf(material -> !material.isBlock() || !material.isItem());

    List<String> names = new LinkedList<>();
    for (Material material : materials) {
      names.add(material.name());
    }

    createData()
      .entity(event.getPlayer())
      .event(event)
      .data("block", names)
      .execute();
  }

}
