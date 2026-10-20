package net.cyperia.challenges.plugin.challenges.implementation.damage;

import net.cyperia.challenges.catalog.Challenge;
import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class DamageRuleSetting extends Setting {

  private final List<DamageCause> causes;

  public DamageRuleSetting(@NotNull Challenge catalogEntry, @NotNull DamageCause... causes) {
    super(catalogEntry, true);
    this.causes = Arrays.asList(causes);
  }

  @NotNull
  @Override
  public LocalizableMessage getDescription() {
    // the causes themselves are translated by CustomTranslatable once embedded as message arguments
    return super.getDescription().withArgs(LocalizableMessage.joinList(2, causes));
  }

  @EventHandler(priority = EventPriority.NORMAL)
  public void onDamage(@NotNull EntityDamageEvent event) {
    if (ChallengeAPI.isWorldInUse()) return;
    if (isEnabled()) return;
    if (!(event.getEntity() instanceof Player)) return;
    if (event.getCause() == DamageCause.VOID || event.getCause() == DamageCause.CUSTOM)
      return; // Never ignore void or custom to prevent different issues
    if (!causes.contains(event.getCause())) return;
    event.setCancelled(true);
  }

}
