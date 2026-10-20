package net.cyperia.challenges.plugin.content.i18n.impl.format;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.DyedItemColor;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.object.ObjectContents;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Resolves a {@link Material} / {@link ItemStack} to an Adventure sprite {@link Component}
 * for boss bars, action bars, tab lists, etc. (MC 1.21.9+ object components).
 *
 * <p>Fully self-contained: no config files, no asset extraction. The override tables below
 * were machine-validated against the complete vanilla item registry and texture lists of
 * both 26.1.2 and the 26.3 snapshots (0 unresolved sprites on either). Materials that
 * don't exist on the running server version are skipped automatically, so this compiles
 * and runs across API versions.</p>
 *
 * <p>Items with no atlas sprite fall back to the closest faithful representation where
 * one exists: heads whose entity texture is a valid skin layout render as profile face
 * sprites (see {@link #HEAD_SKIN_TEXTURES}), colored beds borrow their wool, and shaped
 * blocks borrow the block they are cut from. Anything left stays
 * {@link Component#empty()} rather than rendering something misleading or broken.</p>
 *
 * <p>Returned components may already carry a color, because greyscale textures such as
 * foliage and leather only look right once vanilla's tint is replayed as a text color
 * (see {@link #SPRITE_TINTS}). Callers that want to style a sprite must therefore use
 * {@link Component#colorIfAbsent(net.kyori.adventure.text.format.TextColor)} rather than
 * {@link Component#color(net.kyori.adventure.text.format.TextColor)}.</p>
 */
final class ItemSprites {

  private ItemSprites() {
  }

  // ------------------------------------------------------------------ atlases

  private static final Key BLOCKS_ATLAS = Key.key("minecraft", "blocks");

  private static final Key ITEMS_ATLAS = Key.key("minecraft", "items");

  private static final Map<Material, Component> CACHE = new ConcurrentHashMap<>();

  // ------------------------------------------------------- irregular item sprites
  // Items whose sprite name differs from their registry key (animated / layered / absent).

  private static final Map<Material, String> ITEM_OVERRIDES = materialMap(Map.of(
    "clock", "clock_00",        // per-frame since the item-model split
    "compass", "compass_16",      // frame 16 = the classic "north" look
    "recovery_compass", "recovery_compass_16",
    "crossbow", "crossbow_standby",
    "debug_stick", "stick",           // has no texture of its own
    "tipped_arrow", "tipped_arrow_base", // tint layer can't render; base only
    "enchanted_golden_apple", "golden_apple",    // glint-only variant, no own texture
    "light", "light_15"         // plain "light" removed in 26.3+
  ));

  // ------------------------------------------------- blocks with flat ITEM sprites
  // Their block form has no matching texture, but item/<key> exists.

  private static final Set<Material> ITEM_SPRITE_BLOCKS = materialSet(
    "wheat", "nether_wart", "sugar_cane", "bamboo", "cake", "cauldron", "bell",
    "hopper", "repeater", "comparator", "brewing_stand", "flower_pot",
    "campfire", "soul_campfire", "lantern", "soul_lantern", "kelp", "seagrass",
    "sea_pickle", "pointed_dripstone", "pink_petals", "wildflowers", "leaf_litter",
    "firefly_bush", "mangrove_propagule", "pitcher_plant", "nether_sprouts",
    "barrier", "structure_void", "sniffer_egg", "sulfur_spike");

  /**
   * Tag/suffix driven so future colors, wood types & oxidation states just work.
   */
  private static boolean usesItemSprite(Material m, String key) {
    return ITEM_SPRITE_BLOCKS.contains(m)
      || Tag.DOORS.isTagged(m)
      || Tag.ALL_SIGNS.isTagged(m)       // signs + hanging signs
      || Tag.CANDLES.isTagged(m)
      || key.endsWith("_chain") || key.equals("iron_chain")
      || key.endsWith("copper_lantern"); // (waxed_ already stripped by caller)
  }

  // ------------------------------------------------------------ sprite-less items
  // Rendered as 3D block-entity models in inventory; their textures exist only as
  // unwrapped UV sheets in dedicated atlases (minecraft:chest, shulker_boxes, ...)
  // which look wrong drawn flat, so we substitute (beds, player heads) or stay empty.

  private static boolean hasNoSprite(Material m, String key) {
    return key.endsWith("chest")           // chest, trapped/ender, copper chests
      || key.endsWith("_banner")
      || key.contains("golem_statue")    // 26.x copper golem statues
      || m == Material.CONDUIT || m == Material.DECORATED_POT || m == Material.SHIELD;
  }

  // --------------------------------------------------------------------- mob heads
  // Heads have no atlas sprite at all: their item model is a `minecraft:special`
  // renderer (`minecraft:head` with a fixed `kind`) drawing the 3D block-entity model.
  // The only route to a flat sprite is the player-head object component, whose profile
  // may carry a bare `texture` (a resource-pack path below textures/, no .png) in place
  // of a name/uuid.
  //
  // That is a *player skin* field: the client blits the face with u=8, v=8, w=8, h=8
  // against a fixed 64x64 reference. An entity texture therefore only works if it is a
  // genuine skin layout, meaning BOTH of
  //   - a 64x64 canvas — against a legacy 64x32 one the blit scales down to
  //     (8,4)-(16,8) and samples the wrong, vertically squashed strip;
  //   - a head cube 8 wide at texOffs(0,0) — a wider head puts its face at
  //     (8,8)-(18,16), which an 8px crop slices off-centre.
  // Verify both against the real assets before ever adding an entry here.

  private static final Map<Material, String> HEAD_SKIN_TEXTURES = materialMap(Map.of(
    "zombie_head", "entity/zombie/zombie",
    // profile-less fallback matching the vanilla item icon; real profiles are
    // rendered by the ItemStack overload
    "player_head", "entity/player/wide/steve"
  ));

  // The remaining heads are absent because vanilla ships no texture that satisfies the
  // rules above — not an oversight, and not fixable by pointing at a neighbouring mob:
  //   creeper / skeleton / wither_skeleton  64x32, as are stray, bogged, creeper_armor
  //   piglin                               10-wide head, as are brute and zombified
  //   dragon                               bespoke 256x256 model, no face UV at all
  // They fall through to Component.empty(), like chests, banners and shulker boxes.

  private static boolean isSkull(String key) {
    return key.endsWith("_head") || key.endsWith("_skull");
  }

  // ------------------------------------------------ irregular block texture names
  // Multi-face / animated / renamed blocks. Every entry verified to exist in both
  // 26.1.2 and 26.3 assets (e.g. purpur_pillar_top, because the plain purpur_pillar
  // texture was renamed to purpur_pillar_side in 26.2+).

  private static final Map<Material, String> BLOCK_OVERRIDES = materialMap(Map.<String, String>ofEntries(
    Map.entry("grass_block", "grass_block_side"),
    Map.entry("mycelium", "mycelium_side"),
    Map.entry("podzol", "podzol_side"),
    Map.entry("dirt_path", "dirt_path_top"),
    Map.entry("crafting_table", "crafting_table_front"),
    Map.entry("furnace", "furnace_front"),
    Map.entry("blast_furnace", "blast_furnace_front"),
    Map.entry("smoker", "smoker_front"),
    Map.entry("loom", "loom_front"),
    Map.entry("smithing_table", "smithing_table_front"),
    Map.entry("fletching_table", "fletching_table_front"),
    Map.entry("cartography_table", "cartography_table_side3"),
    Map.entry("grindstone", "grindstone_side"),
    Map.entry("stonecutter", "stonecutter_side"),
    Map.entry("lectern", "lectern_front"),
    Map.entry("enchanting_table", "enchanting_table_side"),
    Map.entry("jukebox", "jukebox_side"),
    Map.entry("tnt", "tnt_side"),
    Map.entry("cactus", "cactus_side"),
    Map.entry("melon", "melon_side"),
    Map.entry("pumpkin", "pumpkin_side"),
    Map.entry("hay_block", "hay_block_side"),
    Map.entry("dried_kelp_block", "dried_kelp_side"),
    Map.entry("bone_block", "bone_block_side"),
    Map.entry("snow_block", "snow"),
    Map.entry("barrel", "barrel_side"),
    Map.entry("composter", "composter_side"),
    Map.entry("dispenser", "dispenser_front"),
    Map.entry("dropper", "dropper_front"),
    Map.entry("observer", "observer_front"),
    Map.entry("crafter", "crafter_north"),
    Map.entry("piston", "piston_side"),
    Map.entry("sticky_piston", "piston_side"),
    Map.entry("daylight_detector", "daylight_detector_top"),
    Map.entry("target", "target_side"),
    Map.entry("bee_nest", "bee_nest_front"),
    Map.entry("beehive", "beehive_front"),
    Map.entry("honey_block", "honey_block_side"),
    Map.entry("bookshelf", "bookshelf"),
    Map.entry("chiseled_bookshelf", "chiseled_bookshelf_occupied"),
    Map.entry("magma_block", "magma"),
    Map.entry("basalt", "basalt_side"),
    Map.entry("polished_basalt", "polished_basalt_side"),
    Map.entry("quartz_block", "quartz_block_side"),
    Map.entry("smooth_quartz", "quartz_block_bottom"),
    Map.entry("quartz_pillar", "quartz_pillar_top"),
    Map.entry("purpur_pillar", "purpur_pillar_top"),
    Map.entry("smooth_sandstone", "sandstone_top"),
    Map.entry("smooth_red_sandstone", "red_sandstone_top"),
    Map.entry("lodestone", "lodestone_side"),
    Map.entry("respawn_anchor", "respawn_anchor_side0"),
    Map.entry("ancient_debris", "ancient_debris_side"),
    Map.entry("reinforced_deepslate", "reinforced_deepslate_side"),
    Map.entry("sculk_sensor", "sculk_sensor_top"),
    Map.entry("calibrated_sculk_sensor", "calibrated_sculk_sensor_top"),
    Map.entry("sculk_catalyst", "sculk_catalyst_side"),
    Map.entry("sculk_shrieker", "sculk_shrieker_side"),
    Map.entry("suspicious_sand", "suspicious_sand_0"),
    Map.entry("suspicious_gravel", "suspicious_gravel_0"),
    Map.entry("vault", "vault_front_off"),
    Map.entry("trial_spawner", "trial_spawner_side_inactive"),
    Map.entry("command_block", "command_block_front"),
    Map.entry("chain_command_block", "chain_command_block_front"),
    Map.entry("repeating_command_block", "repeating_command_block_front"),
    Map.entry("jigsaw", "jigsaw_side"),
    Map.entry("test_block", "test_block_start"),
    Map.entry("end_portal_frame", "end_portal_frame_side"),
    Map.entry("scaffolding", "scaffolding_side"),
    Map.entry("heavy_weighted_pressure_plate", "iron_block"),
    Map.entry("light_weighted_pressure_plate", "gold_block"),
    Map.entry("petrified_oak_slab", "oak_planks"),
    Map.entry("bamboo_fence", "bamboo_fence"),
    Map.entry("bamboo_fence_gate", "bamboo_fence_gate"),
    Map.entry("bamboo_stairs", "bamboo_planks"),
    Map.entry("bamboo_slab", "bamboo_planks"),
    Map.entry("bamboo_button", "bamboo_planks"),
    Map.entry("bamboo_pressure_plate", "bamboo_planks"),
    Map.entry("anvil", "anvil"),
    Map.entry("chipped_anvil", "chipped_anvil_top"),
    Map.entry("damaged_anvil", "damaged_anvil_top"),
    Map.entry("azalea", "azalea_top"),
    Map.entry("flowering_azalea", "flowering_azalea_top"),
    Map.entry("big_dripleaf", "big_dripleaf_top"),
    Map.entry("small_dripleaf", "small_dripleaf_top"),
    Map.entry("mangrove_roots", "mangrove_roots_side"),
    Map.entry("muddy_mangrove_roots", "muddy_mangrove_roots_side"),
    Map.entry("sniffer_egg", "sniffer_egg_not_cracked_north"), // fallback; item sprite preferred
    Map.entry("dried_ghast", "dried_ghast_hydration_0_north"),
    Map.entry("shelf_mushroom", "shelf_mushroom_stage1"),
    Map.entry("sunflower", "sunflower_front"),
    Map.entry("lilac", "lilac_top"),
    Map.entry("rose_bush", "rose_bush_top"),
    Map.entry("peony", "peony_top"),
    Map.entry("tall_grass", "tall_grass_top"),
    Map.entry("large_fern", "large_fern_top"),
    Map.entry("straw_bed", "straw_bed") // unlike colored beds, has a flat texture
  ));

  // ------------------------------------------------------------------ sprite tints
  // Foliage, grass and leather ship as (near-)greyscale textures that vanilla colors at
  // render time, so drawn raw they come out a washed-out grey. A sprite object is styled
  // like any other glyph and the client multiplies it by the component's text color —
  // the same reason the callers force white to stop sprites picking up the surrounding
  // message color — so replaying vanilla's tint as a text color reproduces the icon.
  //
  // Values are vanilla's own item tints, lifted from the `tints` of each item definition
  // in the client assets (1.21.11) rather than eyeballed. Only entries whose tint lands
  // on the very texture we draw are listed; see below for the ones that look tinted but
  // must not be.

  private static final TextColor FOLIAGE = TextColor.color(0x48B518);
  private static final TextColor GRASS = TextColor.color(0x7CBD6B); // colormap/grass.png @ 0.5/1.0
  private static final TextColor UNDYED_LEATHER = TextColor.color(0xA06540);

  private static final Map<Material, TextColor> SPRITE_TINTS = materialMap(Map.ofEntries(
    Map.entry("oak_leaves", FOLIAGE),
    Map.entry("jungle_leaves", FOLIAGE),
    Map.entry("acacia_leaves", FOLIAGE),
    Map.entry("dark_oak_leaves", FOLIAGE),
    Map.entry("vine", FOLIAGE),
    Map.entry("spruce_leaves", TextColor.color(0x619961)),
    Map.entry("birch_leaves", TextColor.color(0x80A755)),
    Map.entry("mangrove_leaves", TextColor.color(0x92C648)),
    Map.entry("lily_pad", TextColor.color(0x71C35C)),
    // biome-dependent in world; the item definitions pin the icon to temperature 0.5,
    // downfall 1.0, which resolves through colormap/grass.png to this one color
    Map.entry("short_grass", GRASS),
    Map.entry("tall_grass", GRASS),
    Map.entry("fern", GRASS),
    Map.entry("large_fern", GRASS),
    Map.entry("bush", GRASS),
    Map.entry("leather_helmet", UNDYED_LEATHER),
    Map.entry("leather_chestplate", UNDYED_LEATHER),
    Map.entry("leather_leggings", UNDYED_LEATHER),
    Map.entry("leather_boots", UNDYED_LEATHER),
    Map.entry("leather_horse_armor", UNDYED_LEATHER)
  ));

  // Deliberately untinted, because vanilla's tint applies to a layer we never draw
  // (tints[i] maps to layer<i>, and we only ever draw one texture):
  //   grass_block    tints the top face and side_overlay, not the side we show
  //   potion, tipped_arrow  tint layer0 (potion_overlay / tipped_arrow_head); we draw layer1
  //   filled_map, firework_star, wolf_armor  tint layer0 pure white, i.e. a no-op
  // Cherry, azalea and pale oak leaves are authored in full color and are not tinted.

  // ==================================================================== public API

  /**
   * Sprite for an item stack. Prefer this over {@link #sprite(Material)}: it renders the
   * actual skin of a player head and skips stacks whose item model was customized by a
   * datapack/plugin (a vanilla-derived sprite would be misleading for those).
   */
  public static @NotNull Component sprite(@Nullable ItemStack stack) {
    if (stack == null || stack.isEmpty()) return Component.empty();

    Key model = stack.getData(DataComponentTypes.ITEM_MODEL);
    if (model != null && !model.equals(stack.getType().getKey())) return Component.empty();

    if (stack.getType() == Material.PLAYER_HEAD) {
      // ResolvableProfile is itself a SkinSource, so this covers every flavour of head
      // in one go: owner uuid/name, a raw base64 textures property, or a resource-pack
      // skin patch. Absent for a plain PLAYER_HEAD, which falls through to the sprite.
      ResolvableProfile profile = stack.getData(DataComponentTypes.PROFILE);
      if (profile != null) return Component.object(ObjectContents.playerHead(profile));
    }

    Component sprite = sprite(stack.getType());
    // the leather pieces draw the very layer vanilla dyes, so an actual dye replaces the
    // default brown. Other dyeable items tint an overlay layer we never draw, hence the
    // check that this material is one whose default tint is the leather one.
    if (SPRITE_TINTS.get(stack.getType()) == UNDYED_LEATHER) {
      DyedItemColor dyed = stack.getData(DataComponentTypes.DYED_COLOR);
      if (dyed != null) return sprite.color(TextColor.color(dyed.color().asRGB()));
    }
    return sprite;
  }

  /**
   * Sprite for a material, or {@link Component#empty()} if nothing usable exists.
   */
  public static @NotNull Component sprite(@Nullable Material material) {
    if (material == null || material.isAir() || !material.isItem()) return Component.empty();
    return CACHE.computeIfAbsent(material, ItemSprites::resolve);
  }

  // ==================================================================== resolution

  private static Component resolve(Material m) {
    Component sprite = resolveSprite(m);
    TextColor tint = SPRITE_TINTS.get(m);
    // never color Component.empty(): callers tell "no sprite" apart by equality with it
    if (tint == null || sprite.equals(Component.empty())) return sprite;
    return sprite.color(tint);
  }

  private static Component resolveSprite(Material m) {
    // waxed_/infested_ blocks are pure state variants sharing the base's textures
    // AND item sprites — strip before anything else (fixes waxed copper doors etc.)
    String key = stripAliasPrefix(m.getKey().getKey());

    String itemOverride = ITEM_OVERRIDES.get(m);
    if (itemOverride != null) return item(itemOverride);

    String headTexture = HEAD_SKIN_TEXTURES.get(m);
    if (headTexture != null) return head(headTexture);
    if (isSkull(key)) return Component.empty(); // the heads listed as unsupported above

    if (hasNoSprite(m, key)) return Component.empty();
    if (Tag.SHULKER_BOXES.isTagged(m)) return Component.empty();

    String blockOverride = BLOCK_OVERRIDES.get(m);
    if (blockOverride != null) return block(blockOverride);

    // colored beds have no flat sprite anywhere -> wool substitute
    if (Tag.BEDS.isTagged(m) && key.endsWith("_bed")) {
      return block(key.substring(0, key.length() - 4) + "_wool");
    }

    if (!m.isBlock() || usesItemSprite(m, key)) return item(key);
    return block(deriveBlockTexture(key));
  }

  /**
   * Shaped blocks (stairs, slabs, walls, fences, panes, buttons, plates, carpets),
   * wood/hyphae and froglights have no textures of their own — derive the block
   * they borrow from, then apply that base's override if it has one.
   */
  private static String deriveBlockTexture(String key) {
    if (key.endsWith("froglight")) return key + "_side";
    if (key.endsWith("_wood")) return key.substring(0, key.length() - 5) + "_log";
    if (key.endsWith("_hyphae")) return key.substring(0, key.length() - 7) + "_stem";

    String base = null;
    for (String suffix : new String[]{"_stairs", "_slab", "_wall", "_fence_gate",
      "_fence", "_button", "_pressure_plate", "_pane", "_carpet"}) {
      if (key.endsWith(suffix)) {
        base = key.substring(0, key.length() - suffix.length());
        break;
      }
    }
    if (base == null) return key; // full block with its own texture

    // resolve the borrowed base: oak_stairs -> oak_planks, nether_brick_fence ->
    // nether_bricks, purpur_stairs -> purpur_block, moss_carpet -> moss_block,
    // white_carpet -> white_wool, smooth_sandstone_slab -> (override) sandstone_top
    for (String candidate : List.of(base, base + "_planks", base + "s",
      base + "_block", base + "_wool")) {
      Material bm = Material.matchMaterial(candidate);
      if (bm != null && bm.isBlock()) {
        String o = BLOCK_OVERRIDES.get(bm);
        return o != null ? o : candidate;
      }
    }
    return key; // graceful worst case for future unknown blocks
  }

  private static String stripAliasPrefix(String key) {
    for (String prefix : new String[]{"waxed_", "infested_"}) {
      if (key.startsWith(prefix)) {
        String stripped = key.substring(prefix.length());
        if (Material.matchMaterial(stripped) != null) return stripped;
      }
    }
    return key;
  }

  // ==================================================================== components

  private static Component item(String name) {
    return Component.object(ObjectContents.sprite(ITEMS_ATLAS, Key.key("minecraft", "item/" + name)));
  }

  private static Component block(String name) {
    return Component.object(ObjectContents.sprite(BLOCKS_ATLAS, Key.key("minecraft", "block/" + name)));
  }

  /**
   * Face sprite cut from a skin-layout texture below {@code textures/}.
   */
  private static Component head(String texture) {
    return Component.object(ObjectContents.playerHead()
      .texture(Key.key("minecraft", texture))
      // entity textures aren't skins, so nothing guarantees their overlay slot holds a
      // hat layer rather than unrelated pixels; it is blank for both entries anyway
      .hat(false)
      .build());
  }

  // -------------------------------------------------- version-safe table builders
  // Built from key strings via matchMaterial so entries for materials that don't
  // exist on this server/API version are silently skipped (compiles everywhere,
  // and 26.2+/26.3 entries like sulfur_spike are inert on 26.1.x).

  private static Set<Material> materialSet(String... keys) {
    return Arrays.stream(keys)
      .map(Material::matchMaterial)
      .filter(Objects::nonNull)
      .collect(Collectors.toCollection(() -> EnumSet.noneOf(Material.class)));
  }

  private static <V> Map<Material, V> materialMap(Map<String, V> byKey) {
    Map<Material, V> out = new EnumMap<>(Material.class);
    byKey.forEach((k, v) -> {
      Material m = Material.matchMaterial(k);
      if (m != null) out.put(m, v);
    });
    return out;
  }
}
