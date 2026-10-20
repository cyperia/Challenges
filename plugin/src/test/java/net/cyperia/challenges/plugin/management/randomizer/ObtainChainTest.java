package net.cyperia.challenges.plugin.management.randomizer;

import net.cyperia.challenges.plugin.management.randomizer.ObtainChain.*;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

class ObtainChainTest {

  private static final Kill BLAZE = new Kill(EntityType.BLAZE, null, List.of(Material.BLAZE_ROD));
  private static final Craft POWDER = new Craft(Material.BLAZE_POWDER, null, List.of(new Ingredient(List.of(Material.BLAZE_ROD), 1)));
  private static final Kill ENDERMAN = new Kill(EntityType.ENDERMAN, null, List.of(Material.ENDER_PEARL));
  private static final Craft EYE = new Craft(Material.ENDER_EYE, null, List.of(
    new Ingredient(List.of(Material.BLAZE_POWDER), 1), new Ingredient(List.of(Material.ENDER_PEARL), 1)));

  @Test
  void resolvesEveryStepDownToAKillOrABrokenBlock() {
    // listed in the worst order, so the chain has to settle over several passes
    ObtainChain chain = new ObtainChain(List.of(EYE, POWDER, ENDERMAN, BLAZE));
    assertEquals(EYE, chain.getSource(Material.ENDER_EYE));
    assertEquals(POWDER, chain.getSource(Material.BLAZE_POWDER));
    assertEquals(BLAZE, chain.getSource(Material.BLAZE_ROD));
  }

  @Test
  void anItemWithAMissingIngredientIsUnreachable() {
    ObtainChain chain = new ObtainChain(List.of(EYE, POWDER, ENDERMAN));
    assertNull(chain.getSource(Material.ENDER_EYE));
  }

  @Test
  void prefersTheShorterWayAndTheCheaperOption() {
    Break gravel = new Break(Material.GRAVEL, List.of(Material.FLINT));
    Craft flintFromNothingReachable = new Craft(Material.FLINT, null, List.of(new Ingredient(List.of(Material.DIAMOND_BLOCK), 1)));
    Craft flintAndSteel = new Craft(Material.FLINT_AND_STEEL, null, List.of(new Ingredient(List.of(Material.DIAMOND, Material.FLINT), 1)));
    ObtainChain chain = new ObtainChain(List.of(flintFromNothingReachable, flintAndSteel, gravel));

    assertInstanceOf(Break.class, chain.getSource(Material.FLINT));
    assertEquals(Material.FLINT, chain.getCheapestOption(flintAndSteel.ingredients().get(0)));
  }

  @Test
  void aRecipeNeedingItsOwnResultNeverWins() {
    Craft loop = new Craft(Material.STICK, null, List.of(new Ingredient(List.of(Material.STICK), 1)));
    assertNull(new ObtainChain(List.of(loop)).getSource(Material.STICK));
  }

}
