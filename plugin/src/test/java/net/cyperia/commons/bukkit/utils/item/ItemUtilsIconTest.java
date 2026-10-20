package net.cyperia.commons.bukkit.utils.item;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ItemUtilsIconTest {

  @Test
  void mobsShowTheirSpawnEgg() {
    assertEquals(Material.ZOMBIE_SPAWN_EGG, ItemUtils.getIcon(EntityType.ZOMBIE));
  }

  @Test
  void valuesWithoutAnItemShowPaper() {
    assertEquals(Material.PAPER, ItemUtils.getIcon(DamageCause.FALL));
  }

}
