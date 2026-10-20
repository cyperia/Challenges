package net.cyperia.challenges.plugin.management.challenges;

import net.cyperia.challenges.catalog.Icon;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.jetbrains.annotations.NotNull;

public final class CatalogIcons {

  private CatalogIcons() {
  }

  @NotNull
  public static ItemStack item(@NotNull Icon icon) {
    ItemStack item = new ItemStack(Material.valueOf(icon.material()));
    if (icon.rgb() != null) {
      Color color = Color.fromRGB(icon.rgb());
      item.editMeta(meta -> {
        if (meta instanceof PotionMeta potion) potion.setColor(color);
        if (meta instanceof LeatherArmorMeta leather) leather.setColor(color);
      });
    }
    return item;
  }

}
