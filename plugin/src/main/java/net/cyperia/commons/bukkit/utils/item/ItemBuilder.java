package net.cyperia.commons.bukkit.utils.item;

import com.destroystokyo.paper.profile.PlayerProfile;
import net.cyperia.commons.bukkit.core.BukkitModule;
import net.cyperia.commons.common.config.Document;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.profile.PlayerTextures;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ItemBuilder {

  private static final ItemFlag[] ALL_FLAGS = ItemFlag.values();

  protected ItemStack item;
  protected ItemMeta meta;

  public ItemBuilder(@NotNull ItemStack item) {
    ItemStack cloned = item.clone();
    this(cloned, cloned.getItemMeta());
  }

  public ItemBuilder(@NotNull ItemStack item, @Nullable ItemMeta meta) {
    this.item = item;
    this.meta = meta;
  }

  public ItemBuilder(@NotNull Material material) {
    this(new ItemStack(material));
  }

  public ItemBuilder(@NotNull Material material, @NotNull Component name) {
    this(material);
    setName(name);
  }

  @NotNull
  public ItemMeta getItemMeta() {
    return getItemMetaAs();
  }

  @NotNull
  @SuppressWarnings("unchecked")
  public final <M> M getItemMetaAs() {
    return (M) (meta == null ? meta = item.getItemMeta() : meta);
  }

  @NotNull
  public ItemBuilder setName(@Nullable Component name) {
    getItemMeta().displayName(name);
    return this;
  }

  @NotNull
  public ItemBuilder appendName(@NotNull Component sequence) {
    Component name = getName();
    return setName(name == null ? sequence : name.append(sequence));
  }

  @NotNull
  public ItemBuilder setLore(@Nullable List<Component> lore) {
    getItemMeta().lore(lore);
    return this;
  }

  @NotNull
  public ItemBuilder setLore(@NotNull Component... lore) {
    return setLore(Arrays.asList(lore));
  }

  @NotNull
  public ItemBuilder appendLore(@NotNull Component... lore) {
    return appendLore(Arrays.asList(lore));
  }

  @NotNull
  public ItemBuilder appendLore(@NotNull Collection<? extends Component> lore) {
    if (lore.isEmpty()) return this;
    List<Component> existing = getItemMeta().lore();
    if (existing == null) return setLore(List.copyOf(lore));

    List<Component> combined = new ArrayList<>(existing.size() + lore.size());
    combined.addAll(existing);
    combined.addAll(lore);
    return setLore(combined);
  }

  /**
   * Applies the first line as the display name and every following one as the lore.
   */
  @NotNull
  public ItemBuilder setLines(@NotNull List<Component> lines) {
    if (lines.isEmpty()) return this;
    setName(lines.getFirst());
    return setLore(lines.size() == 1 ? null : List.copyOf(lines.subList(1, lines.size())));
  }

  @Nullable
  public Component getName() {
    return getItemMeta().displayName();
  }

  @NotNull
  public List<Component> getLore() {
    List<Component> lore = getItemMeta().lore();
    return lore == null ? new ArrayList<>() : lore;
  }

  @NotNull
  public ItemBuilder addEnchantment(@NotNull Enchantment enchantment, int level) {
    getItemMeta().addEnchant(enchantment, level, true);
    return this;
  }

  @NotNull
  public ItemBuilder addFlag(@NotNull ItemFlag... flags) {
    getItemMeta().addItemFlags(flags);
    return this;
  }

  @NotNull
  public ItemBuilder removeFlag(@NotNull ItemFlag... flags) {
    getItemMeta().removeItemFlags(flags);
    return this;
  }

  @NotNull
  public ItemBuilder hideAttributes() {
    applyDummyAttributeModifier();
    return addFlag(ALL_FLAGS);
  }

  @SuppressWarnings({"UnstableApiUsage"})
  protected void applyDummyAttributeModifier() {
    try {
      // hacky fix to make paper hide damage attributes (only works with custom modifiers), "Vanilla behavior since 1.20.5"
      // see https://github.com/PaperMC/Paper/issues/11224
      Attribute dummyAttribute = Attribute.LUCK;
      Collection<AttributeModifier> modifiers = getItemMeta().getAttributeModifiers(dummyAttribute);

      if (modifiers == null || modifiers.isEmpty()) {
        meta.addAttributeModifier(dummyAttribute, new AttributeModifier(
          new NamespacedKey(BukkitModule.getFirstInstance(), "dummy"),
          0,
          AttributeModifier.Operation.ADD_NUMBER,
          org.bukkit.inventory.EquipmentSlotGroup.ANY
        ));
      }
    } catch (Throwable ignored) { // defend against experimental api changes
    }
  }

  @NotNull
  public ItemBuilder showAttributes() {
    return removeFlag(ALL_FLAGS);
  }

  @NotNull
  public ItemBuilder setUnbreakable(boolean unbreakable) {
    getItemMeta().setUnbreakable(unbreakable);
    return this;
  }

  @NotNull
  public ItemBuilder setAmount(int amount) {
    item.setAmount(Math.clamp(amount, 0, 64));
    return this;
  }

  @NotNull
  public ItemBuilder setDamage(int damage) {
    this.<Damageable>getItemMetaAs().setDamage(damage);
    return this;
  }

  @NotNull
  public ItemBuilder setMaterial(@NotNull Material material) {
    item.setType(material);
    meta = item.getItemMeta();
    return this;
  }

  @NotNull
  public Material getType() {
    return item.getType();
  }

  public int getAmount() {
    return item.getAmount();
  }

  public int getDamage() {
    return this.<Damageable>getItemMetaAs().getDamage();
  }

  @NotNull
  public ItemStack build() {
    item.setItemMeta(getItemMeta()); // Call to getter to prevent null value
    return item;
  }

  @NotNull
  public ItemStack toItem() {
    return build();
  }

  @Override
  public ItemBuilder clone() {
    return new ItemBuilder(item.clone(), getItemMeta().clone());
  }


  // Legacy string API, kept for the not yet migrated LegacyItemBuilder

  @Deprecated
  public ItemBuilder(@NotNull Material material, @NotNull String name) {
    this(material);
    setName(name);
  }

  @Deprecated
  public ItemBuilder(@NotNull Material material, @NotNull String name, @NotNull String... lore) {
    this(material, name);
    setLore(lore);
  }

  @Deprecated
  public ItemBuilder(@NotNull Material material, @NotNull String name, int amount) {
    this(material, name);
    setAmount(amount);
  }

  @NotNull
  @Deprecated
  public ItemBuilder setName(@Nullable String name) {
    getItemMeta().setDisplayName(name);
    return this;
  }

  @NotNull
  @Deprecated
  public ItemBuilder setName(@Nullable Object name) {
    return setName(name == null ? null : name.toString());
  }

  /**
   * Applies the first line as the display name and every following one as the lore.
   */
  @NotNull
  @Deprecated
  public ItemBuilder setName(@NotNull String... content) {
    if (content.length > 0) setName(content[0]);
    if (content.length > 1) setLore(Arrays.copyOfRange(content, 1, content.length));
    return this;
  }

  @NotNull
  @Deprecated
  public ItemBuilder appendName(@Nullable String sequence) {
    return setName(getItemMeta().getDisplayName() + sequence);
  }

  @NotNull
  @Deprecated
  public ItemBuilder setLore(@NotNull String... lore) {
    getItemMeta().setLore(Arrays.asList(lore));
    return this;
  }

  @NotNull
  @Deprecated
  public ItemBuilder appendLore(@NotNull String... lore) {
    List<String> existing = getItemMeta().getLore();
    if (existing == null) existing = new ArrayList<>(lore.length);
    Collections.addAll(existing, lore);
    getItemMeta().setLore(existing);
    return this;
  }

  public static class BannerBuilder extends ItemBuilder {

    public BannerBuilder(@NotNull Material material) {
      super(material);
    }

    public BannerBuilder(@NotNull Material material, @NotNull Component name) {
      super(material, name);
    }

    public BannerBuilder(@NotNull ItemStack item) {
      super(item);
    }

    @NotNull
    public BannerBuilder addPattern(@NotNull BannerPattern pattern, @NotNull DyeColor color) {
      return addPattern(pattern.getPatternType(), color);
    }

    @NotNull
    public BannerBuilder addPattern(@NotNull PatternType pattern, @NotNull DyeColor color) {
      getItemMeta().addPattern(new Pattern(color, pattern));
      return this;
    }

    @NotNull
    @Override
    public BannerMeta getItemMeta() {
      return getItemMetaAs();
    }

  }

  public static class SkullBuilder extends ItemBuilder {

    public static void setBase64Texture(@NotNull SkullMeta item, @NotNull String base64Texture) {
      String textureUrlJson = new String(Base64.getDecoder().decode(base64Texture), StandardCharsets.UTF_8);
      String textureUrl = Document.parseJson(textureUrlJson)
        .getString("textures.SKIN.url");
      if (textureUrl == null) return; // TODO
      setTexture(item, textureUrl);
    }

    public static void setTexture(@NotNull SkullMeta meta, @NotNull String textureUrl) {
      UUID uuid = UUID.nameUUIDFromBytes(textureUrl.getBytes());

      PlayerProfile profile = Bukkit.createProfile(uuid);
      PlayerTextures texture = profile.getTextures();

      try {
        texture.setSkin(URI.create(textureUrl).toURL());
      } catch (MalformedURLException e) {
        throw new IllegalArgumentException("Invalid texture url", e);
      }

      profile.setTextures(texture);
      meta.setPlayerProfile(profile);
    }

    public SkullBuilder() {
      super(Material.PLAYER_HEAD);
    }

    @NotNull
    public SkullBuilder setOwner(@NotNull OfflinePlayer owner) {
      getItemMeta().setOwningPlayer(owner);
      return this;
    }

    @NotNull
    public SkullBuilder setOwner(@NotNull UUID uuid, @NotNull String name) {
      PlayerProfile profile = Bukkit.createProfile(uuid, name);
      getItemMeta().setPlayerProfile(profile);
      return this;
    }

    @NotNull
    public SkullBuilder setTexture(@NotNull String textureUrl) {
      setTexture(getItemMeta(), textureUrl);
      return this;
    }

    public SkullBuilder setBase64Texture(@NotNull String base64Texture) {
      setBase64Texture(getItemMeta(), base64Texture);
      return this;
    }

    @NotNull
    @Override
    public SkullMeta getItemMeta() {
      return getItemMetaAs();
    }

  }

  public static class PotionBuilder extends ItemBuilder {

    @NotNull
    public static ItemBuilder createWaterBottle() {
      return new PotionBuilder(Material.POTION).setColor(Color.BLUE).hideAttributes();
    }

    public PotionBuilder(@NotNull Material material) {
      super(material);
    }

    public PotionBuilder(@NotNull Material material, @NotNull Component name) {
      super(material, name);
    }

    public PotionBuilder(@NotNull ItemStack item) {
      super(item);
    }

    @NotNull
    public PotionBuilder addEffect(@NotNull PotionEffect effect) {
      getItemMeta().addCustomEffect(effect, true);
      return this;
    }

    @NotNull
    public PotionBuilder setColor(@NotNull Color color) {
      getItemMeta().setColor(color);
      return this;
    }

    @NotNull
    public PotionBuilder color(@NotNull Color color) {
      return setColor(color);
    }

    @NotNull
    @Override
    public PotionMeta getItemMeta() {
      return getItemMetaAs();
    }

  }

  public static class LeatherArmorBuilder extends ItemBuilder {

    public LeatherArmorBuilder(@NotNull Material material) {
      super(material);
    }

    public LeatherArmorBuilder(@NotNull Material material, @NotNull Component name) {
      super(material, name);
    }

    public LeatherArmorBuilder(@NotNull ItemStack item) {
      super(item);
    }

    @NotNull
    public LeatherArmorBuilder setColor(@NotNull Color color) {
      getItemMeta().setColor(color);
      return this;
    }

    @NotNull
    public LeatherArmorBuilder color(@NotNull Color color) {
      return setColor(color);
    }

    @NotNull
    @Override
    public LeatherArmorMeta getItemMeta() {
      return getItemMetaAs();
    }

  }

}
