package net.cyperia.challenges.plugin.challenges.implementation.goal;

import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.SettingModifierCollectionGoal;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.spigot.events.PlayerInventoryClickEvent;
import net.cyperia.challenges.plugin.spigot.events.PlayerPickupItemEvent;
import net.cyperia.challenges.plugin.utils.misc.ExperimentalUtils;
import net.cyperia.challenges.plugin.utils.misc.ListBuilder;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CollectWoodGoal extends SettingModifierCollectionGoal {

  private static final int
    OVERWORLD = 1,
    NETHER = 2,
    BOTH = 3;

  public CollectWoodGoal() {
    super(1, 3);
    setValueUnit(ValueUnit.named(this::getSearchedDimension));
  }

  @NotNull
  @Override
  public ItemStack getSettingsItemPreset() {
    if (getValue() == OVERWORLD) return new ItemStack(Material.OAK_LOG);
    if (getValue() == NETHER) return new ItemStack(Material.WARPED_STEM);
    return new ItemStack(Material.CRYING_OBSIDIAN); // both
  }

  @NotNull
  private LocalizableMessage getSearchedDimension(int value) {
    if (value == OVERWORLD) return getChallengeMessageKey("settings.overworld");
    if (value == NETHER) return getChallengeMessageKey("settings.nether");
    return getChallengeMessageKey("settings.both");
  }

  @Override
  protected void onEnable() {
    super.onEnable();
    setTarget(getWoodMaterials());
    checkCollects();
  }

  @Override
  protected void onValueChange() {
    setTarget(getWoodMaterials());
    checkCollects();
  }

  @NotNull
  private Object[] getWoodMaterials() {
    return new ListBuilder<Material>().fill(builder -> {
      for (Material material : ExperimentalUtils.getMaterials()) {
        if (isSearched(material))
          builder.add(material);
      }
    }).build().toArray();
  }

  private boolean isLog(@NotNull Material material) {
    return material.name().contains("LOG") && !material.name().contains("STRIPPED");
  }

  private boolean isNetherLog(@NotNull Material material) {
    return material == Material.WARPED_STEM || material == Material.CRIMSON_STEM;
  }

  private boolean isSearched(@NotNull Material material) {
    return getValue() == OVERWORLD && isLog(material) ||
      getValue() == NETHER && isNetherLog(material) ||
      getValue() == BOTH && (isLog(material) || isNetherLog(material));
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onPickupItem(@NotNull PlayerPickupItemEvent event) {
    if (!shouldExecuteEffect()) return;
    Material material = event.getItem().getItemStack().getType();
    Player player = event.getPlayer();
    handleCollect(player, material);
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onPlayerInventoryClick(@NotNull PlayerInventoryClickEvent event) {
    if (!shouldExecuteEffect()) return;
    if (event.isCancelled()) return;
    if (event.getClickedInventory() == null) return;
    if (event.getClickedInventory().getHolder() != event.getPlayer()) return;
    if (event.getCurrentItem() == null) return;
    Player player = event.getPlayer();
    Material material = event.getCurrentItem().getType();
    handleCollect(player, material);
  }

  private void handleCollect(@NotNull Player player, @NotNull Material material) {
    collect(player, material, () -> {
      getChallengeMessageKey("item-collected").send(player, Prefix.CHALLENGES, material);
      SoundSample.PLING.play(player);
    });
  }

}
