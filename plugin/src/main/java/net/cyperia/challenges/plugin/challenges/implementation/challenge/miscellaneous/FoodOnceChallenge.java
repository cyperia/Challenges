package net.cyperia.challenges.plugin.challenges.implementation.challenge.miscellaneous;

import net.cyperia.challenges.plugin.challenges.type.abstraction.SettingModifier;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FoodOnceChallenge extends SettingModifier {

  public FoodOnceChallenge() {
    super(2);
    setValueUnit(ChallengeHelper.createAudienceUnit(2));
    setUsesGlobalPunish(true);
  }

  @NotNull
  @Override
  public ItemStack getSettingsItemPreset() {
    if (teamFoodsActivated()) return new ItemStack(Material.ENDER_CHEST);
    return new ItemStack(Material.PLAYER_HEAD);
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onPlayerItemConsume(@NotNull PlayerItemConsumeEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;

    Material type = event.getItem().getType();
    if (hasEaten(event.getPlayer(), type)) {
      getChallengeMessageKey("failed").broadcast(Prefix.CHALLENGES, event.getPlayer(), type);
      ChallengeHelper.punish(event.getPlayer(), 1);
    } else {
      addFood(event.getPlayer(), type);
      if (teamFoodsActivated()) {
        getChallengeMessageKey("new-food-team").broadcast(Prefix.CHALLENGES, event.getPlayer(), type);
      } else {
        getChallengeMessageKey("new-food").send(event.getPlayer(), Prefix.CHALLENGES, event.getPlayer(), type);

      }
    }

  }

  private void addFood(Player player, Material type) {
    if (teamFoodsActivated()) {
      addTeamFood(type);
    } else {
      addPlayerFood(player, type);
    }
  }

  private void addPlayerFood(@NotNull Player player, @NotNull Material material) {
    List<Material> foods = getPlayerData(player).getEnumList("foods", Material.class);
    foods.add(material);
    getPlayerData(player).set("foods", foods);
  }

  private boolean hasEaten(@NotNull Player player, @NotNull Material material) {
    if (teamFoodsActivated()) {
      return hasBeenEatenByTeam(material);
    }

    return getPlayerData(player).getEnumList("foods", Material.class).contains(material);
  }

  private void addTeamFood(@NotNull Material material) {
    List<Material> foods = getGameStateData().getEnumList("foods", Material.class);
    foods.add(material);
    getGameStateData().set("foods", foods);
  }

  private boolean hasBeenEatenByTeam(@NotNull Material material) {
    return getGameStateData().getEnumList("foods", Material.class).contains(material);
  }

  private boolean teamFoodsActivated() {
    return getValue() == 2;
  }

}
