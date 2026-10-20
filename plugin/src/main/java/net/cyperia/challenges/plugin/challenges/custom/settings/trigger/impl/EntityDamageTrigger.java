package net.cyperia.challenges.plugin.challenges.custom.settings.trigger.impl;

import net.cyperia.challenges.plugin.challenges.custom.settings.sub.SelectableKey;
import net.cyperia.challenges.plugin.challenges.custom.settings.trigger.ChallengeTrigger;
import net.cyperia.challenges.plugin.challenges.type.helper.SubSettingsHelper;
import net.cyperia.challenges.plugin.content.i18n.impl.format.CustomTranslatable;
import net.cyperia.commons.bukkit.utils.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class EntityDamageTrigger extends ChallengeTrigger {

  public EntityDamageTrigger(String name) {
    super(name, SubSettingsHelper.createEntityTypeSettingsBuilder(true, true).createChooseMultipleChild("damage_cause").fill(builder -> {

      List<PotionEffectType> types = new ArrayList<>(
        Arrays.asList(PotionEffectType.values()));
      Collections.shuffle(types, new Random(1));

      builder.addSetting(SelectableKey.ofName(SubSettingsHelper.ANY, Material.NETHER_STAR, "damage_cause.any"));

      DamageCause[] values = DamageCause.values();
      for (int i = 0; i < values.length; i++) {
        DamageCause cause = values[i];
        PotionEffectType effectType = types.get(i);

        ItemStack displayItemPreset = new ItemBuilder.PotionBuilder(Material.TIPPED_ARROW).color(effectType.getColor()).build();
        builder.addSetting(SelectableKey.of(cause.name(), displayItemPreset, CustomTranslatable.of(cause)));
      }

    }));
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onDeath(@NotNull EntityDamageEvent event) {
    createData()
      .entity(event.getEntity())
      .event(event)
      .entityType(event.getEntityType())
      .data("damage_cause", SubSettingsHelper.ANY, event.getCause().name())
      .execute();
  }

}
