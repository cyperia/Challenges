package net.cyperia.challenges.plugin.management.menu.generator.impl.custom.choose;

import net.cyperia.challenges.plugin.challenges.custom.settings.SettingType;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.management.menu.MenuType;
import net.cyperia.challenges.plugin.management.menu.format.ItemFormatter;
import net.cyperia.challenges.plugin.management.menu.generator.MultiPageSelectMenuGenerator;
import net.cyperia.challenges.plugin.management.menu.generator.impl.custom.IParentCustomGenerator;
import net.cyperia.challenges.plugin.utils.misc.ExperimentalUtils;
import net.cyperia.challenges.plugin.utils.misc.MapUtils;
import net.cyperia.commons.bukkit.utils.menu.MenuClickInfo;
import net.cyperia.commons.bukkit.utils.menu.MenuClickType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Locale;

public class CustomChooseMaterialMenuGenerator extends MultiPageSelectMenuGenerator<Material> {

  public static final int SIZE = CustomChooseOptionMenuGenerator.SIZE;
  public static final int[] SLOTS = CustomChooseOptionMenuGenerator.SLOTS;
  public static final Material[] MATERIALS;

  static {
    ArrayList<Material> materials = new ArrayList<>();
    for (Material material : ExperimentalUtils.getMaterials()) {
      if (material.isAir()) continue;
      if (!material.isItem()) continue;
      materials.add(material);
    }
    MATERIALS = materials.toArray(new Material[0]);
  }

  private final IParentCustomGenerator parent;

  public CustomChooseMaterialMenuGenerator(@NotNull IParentCustomGenerator parent) {
    this(parent, MATERIALS);
  }

  protected CustomChooseMaterialMenuGenerator(@NotNull IParentCustomGenerator parent, @NotNull Material[] elements) {
    super(elements);
    this.menuType = MenuType.CUSTOM;
    this.parent = parent;
  }

  @NotNull
  @Override
  public LocalizableMessage getMenuName() {
    return createSubMenuTitle(MessageKey.of("menu.custom.menu-short"), MessageKey.of("menu.custom.material.menu"));
  }

  @Override
  public void handleElementClick(@NotNull Material element, @NotNull MenuClickInfo info) {
    parent.accept(info.getPlayer(), SettingType.MATERIAL, MapUtils.createStringArrayMap("material", element.name()));
  }

  @NotNull
  @Override
  public ItemStack createDisplayItem(@NotNull Material element, @NotNull Locale locale) {
    // shares the name format with the material sub settings, an option list of the very same items
    return new ItemFormatter(element)
      .name(MessageKey.of("custom.setting.material.format.name").withArgs(element))
      .tooltip(MenuClickType.LEFT, MessageKey.of("menu.custom.interact.select"))
      .build(locale);
  }

  @Override
  protected void handleNavigateOutOfMenu(@NotNull Player player) {
    parent.decline(player);
  }

  @Override
  public int[] getSlots() {
    return SLOTS;
  }


  @Override
  public int getInventorySize() {
    return SIZE;
  }
}
