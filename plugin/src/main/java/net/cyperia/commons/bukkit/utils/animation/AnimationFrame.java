package net.cyperia.commons.bukkit.utils.animation;

import net.cyperia.challenges.plugin.utils.item.DefaultItems;
import net.cyperia.commons.bukkit.utils.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class AnimationFrame implements Cloneable {

  private final ItemStack[] content;
  private boolean sound = true;

  public AnimationFrame(@NotNull ItemStack[] content) {
    this.content = Arrays.copyOf(content, content.length);
  }

  public AnimationFrame(int size) {
    this.content = new ItemStack[size];
  }

  @NotNull
  public AnimationFrame fill(@NotNull ItemStack item) {
    Arrays.fill(content, item);
    return this;
  }

  @NotNull
  public AnimationFrame setContrast(int... slots) {
    for (int slot : slots) {
      content[slot] = DefaultItems.FILL_ITEM_CONTRAST;
    }
    return this;
  }

  @NotNull
  public AnimationFrame setItem(int slot, @NotNull ItemBuilder item) {
    return setItem(slot, item.build());
  }

  @NotNull
  public AnimationFrame setItem(int slot, @NotNull ItemStack item) {
    content[slot] = item;
    return this;
  }

  @NotNull
  public AnimationFrame setSound(boolean play) {
    this.sound = play;
    return this;
  }

  @Nullable
  public ItemStack getItem(int slot) {
    return content[slot];
  }

  @Nullable
  public Material getItemType(int slot) {
    return getItem(slot) == null ? Material.AIR : getItem(slot).getType();
  }

  @NotNull
  public ItemStack[] getContent() {
    return content;
  }

  public boolean shouldPlaySound() {
    return sound;
  }

  public int getSize() {
    return content.length;
  }

  @NotNull
  @Override
  public AnimationFrame clone() {
    return new AnimationFrame(content);
  }

}
