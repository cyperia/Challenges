package net.cyperia.commons.bukkit.utils.animation;

import net.cyperia.commons.bukkit.core.BukkitModule;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class AnimatedInventory {

  private final List<AnimationFrame> frames = new ArrayList<>();
  private final Function<Integer, Inventory> sizeToNewInventory;
  private final int size;
  private SoundSample frameSound = SoundSample.CLICK, endSound = SoundSample.OPEN;
  private int frameDelay = 1;

  @Deprecated
  public AnimatedInventory(@NotNull String title, int size, @Nullable InventoryHolder holder) {
    this.size = size;
    this.sizeToNewInventory = forSize -> Bukkit.createInventory(holder, forSize, title);
  }

  public AnimatedInventory(@NotNull Component title, int size) {
    this(title, size, null);
  }

  public AnimatedInventory(@NotNull Component title, int size, @Nullable InventoryHolder holder) {
    this.size = size;
    this.sizeToNewInventory = forSize -> Bukkit.createInventory(holder, forSize, title);
  }

  public AnimatedInventory(int size, @NotNull Function<Integer, Inventory> sizeToNewInventory) {
    this.sizeToNewInventory = sizeToNewInventory;
    this.size = size;
  }

  public void open(@NotNull Player player) {
    open(player, BukkitModule.getFirstInstance());
  }

  public void open(@NotNull Player player, @NotNull JavaPlugin plugin) {
    Regions.entity(plugin, player, () -> openOnOwnThread(player, plugin));
  }

  private void openOnOwnThread(@NotNull Player player, @NotNull JavaPlugin plugin) {
    Inventory inventory = createInventory();
    applyFrame(inventory, 0, player);
    player.openInventory(inventory);

    AtomicInteger index = new AtomicInteger(1);
    Regions.timer(plugin, player, task -> {

      boolean opened = inventory.getViewers().contains(player);
      if (index.get() >= frames.size() || !opened) {
        task.cancel();
        return;
      }

      applyFrame(inventory, index.get(), player);

      index.incrementAndGet();

    }, frameDelay, frameDelay);

  }

  public void openNotAnimated(@NotNull Player player, boolean playSound) {
    openNotAnimated(player, playSound, BukkitModule.getFirstInstance());
  }

  public void openNotAnimated(@NotNull Player player, boolean playSound, @NotNull JavaPlugin plugin) {
    Regions.entity(plugin, player, () -> {
      Inventory inventory = createInventory();

      if (!frames.isEmpty()) {
        AnimationFrame frame = getLastFrame();
        inventory.setContents(frame.getContent());
      }

      player.openInventory(inventory);

      if (playSound && endSound != null) endSound.play(player);
    });
  }

  private void applyFrame(@NotNull Inventory inventory, int index, @NotNull Player viewer) {
    AnimationFrame frame = frames.get(index);
    inventory.setContents(frame.getContent());

    if (index == frames.size() - 1 && endSound != null) endSound.play(viewer);
    else if (frameSound != null && frame.shouldPlaySound()) frameSound.play(viewer);
  }

  @NotNull
  public AnimatedInventory addFrame(@NotNull AnimationFrame frame) {
    if (size != frame.getSize())
      throw new IllegalArgumentException("AnimationFrame must have the same size (Expected " + size + "; Got " + frame.getSize() + ")");
    frames.add(frame);
    return this;
  }

  @NotNull
  public AnimationFrame createAndAdd() {
    AnimationFrame frame = new AnimationFrame(size);
    addFrame(frame);
    return frame;
  }

  @NotNull
  public AnimationFrame getFrame(int index) {
    return frames.get(index);
  }

  @NotNull
  public AnimationFrame getOrCreateFrame(int index) {
    while (frames.size() <= index) {
      cloneLastAndAdd();
    }
    return getFrame(index);
  }

  @NotNull
  public AnimationFrame cloneAndAdd(int index) {
    AnimationFrame frame = getFrame(index).clone();
    addFrame(frame);
    return frame;
  }

  @NotNull
  public AnimationFrame getLastFrame() {
    if (frames.isEmpty()) throw new IllegalStateException("Frames are empty");
    return getFrame(frames.size() - 1);
  }

  @NotNull
  public AnimationFrame cloneLastAndAdd() {
    AnimationFrame frame = getLastFrame().clone();
    addFrame(frame);
    return frame;
  }

  @NotNull
  public AnimatedInventory setEndSound(@Nullable SoundSample endSound) {
    this.endSound = endSound;
    return this;
  }

  @NotNull
  public AnimatedInventory setFrameSound(@Nullable SoundSample frameSound) {
    this.frameSound = frameSound;
    return this;
  }

  @NotNull
  public AnimatedInventory setFrameDelay(int delay) {
    if (delay < 1) throw new IllegalArgumentException("Delay cannot be smaller than 1");
    this.frameDelay = delay;
    return this;
  }

  @NotNull
  private Inventory createInventory() {
    return sizeToNewInventory.apply(size);
  }

}
