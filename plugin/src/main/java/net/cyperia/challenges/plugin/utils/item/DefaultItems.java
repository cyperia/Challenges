package net.cyperia.challenges.plugin.utils.item;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.cyperia.commons.bukkit.utils.item.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DefaultItems {

  public static final ItemStack FILL_ITEM = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE, Component.empty()).build();
  public static final ItemStack FILL_ITEM_CONTRAST = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE, Component.empty()).build();

  public static final ItemStack BLOCKED_ITEM = new ItemBuilder(Material.BARRIER, Component.empty()).build();

  @Contract("-> new")
  public static ItemStack createEnabledPreset() {
    return new ItemStack(Material.LIME_DYE);
  }

  @Contract("_ -> new")
  public static ItemStack createEnabledValuePreset(int amount) {
    return new ItemStack(Material.LIME_DYE, amount);
  }

  @Contract("-> new")
  public static ItemStack createDisabledPreset() {
    return new ItemStack(Material.RED_DYE);
  }

  @Contract("_ -> new")
  public static ItemStack createValuePreset(int value) {
    return new ItemStack(Material.STONE_BUTTON, Math.max(value, 1));
  }

  @Contract("-> new")
  public static ItemStack createCustomizePreset() {
    return new ItemStack(Material.OAK_SIGN);
  }

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static final class SkullTextures {
    public static final String
      ARROW_LEFT = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjI1YWQwOTgxNmY1Yjc1ZTcyMzUwNzFlODdiYmZmOGYxYmVlMjZmMDVkNDQwMWFjNGRmOTI1YTA3MDE5In19fQ==",
      ARROW_RIGHT = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWI2ZjFhMjViNmJjMTk5OTQ2NDcyYWVkYjM3MDUyMjU4NGZmNmY0ZTgzMjIxZTU5NDZiZDJlNDFiNWNhMTNiIn19fQ==",
      GREEN_ARROW_UP = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWRhMDI3NDc3MTk3YzZmZDdhZDMzMDE0NTQ2ZGUzOTJiNGE1MWM2MzRlYTY4YzhiN2JjYzAxMzFjODNlM2YifX19",
      RED_ARROW_DOWN = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTM4NTJiZjYxNmYzMWVkNjdjMzdkZTRiMGJhYTJjNWY4ZDhmY2E4MmU3MmRiY2FmY2JhNjY5NTZhODFjNCJ9fX0=";
  }

}
