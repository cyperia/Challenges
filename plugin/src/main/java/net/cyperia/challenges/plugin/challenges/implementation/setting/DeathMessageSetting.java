package net.cyperia.challenges.plugin.challenges.implementation.setting;

import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.Modifier;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.challenges.plugin.content.i18n.ArgumentFormat;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.content.i18n.impl.format.rewrite.ComponentPlayerNames;
import net.cyperia.challenges.plugin.content.i18n.impl.format.rewrite.ComponentRewriter;
import net.cyperia.challenges.plugin.utils.item.DefaultItems;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class DeathMessageSetting extends Modifier {

  public static final int
    ENABLED = 2,
    VANILLA = 3;

  private boolean hide;

  public DeathMessageSetting() {
    super(1, 3, VANILLA);
    setValueUnit(ValueUnit.named(value -> switch (value) {
      case VANILLA -> getChallengeMessageKey("vanilla");
      case ENABLED -> ChallengeHelper.getChallengeEnabledName();
      default -> ChallengeHelper.getChallengeDisabledName();
    }));
  }

  @NotNull
  @Override
  public ItemStack getSettingsItemPreset() {
    return switch (getValue()) {
      case VANILLA -> new ItemStack(Material.YELLOW_DYE);
      case ENABLED -> DefaultItems.createEnabledPreset();
      default -> DefaultItems.createDisabledPreset();
    };
  }

  @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
  public void onDeath(@NotNull PlayerDeathEvent event) {
    if (hide) {
      event.deathMessage(null);
      return;
    }

    Player player = event.getEntity();
    switch (getValue()) {
      case ENABLED -> {
        EntityDamageEvent cause = player.getLastDamageCause();
        if (cause != null && cause.getCause() != DamageCause.CUSTOM) {
          getChallengeMessageKey("message-cause").broadcast(Prefix.CHALLENGES, player, ArgumentFormat.DAMAGE_CAUSE.apply(cause));
        } else {
          getChallengeMessageKey("message").broadcast(Prefix.CHALLENGES, player);
        }
      }
      case VANILLA -> {
        Component original = event.deathMessage();
        if (original != null) {
          // the message is client side translated, so the same component goes out to everyone
          Component message = ComponentRewriter.rewrite(original, ComponentPlayerNames.REWRITE).colorIfAbsent(NamedTextColor.GRAY);
          broadcastAudience(target -> target.sendMessage(Prefix.CHALLENGES.asComponent(target).append(message)));
        }
      }
    }

    event.deathMessage(null);
  }

  public void setHideMessagesTemporarily(boolean hide) {
    this.hide = hide;
  }

}
