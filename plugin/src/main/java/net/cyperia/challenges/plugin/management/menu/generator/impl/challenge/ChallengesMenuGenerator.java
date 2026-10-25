package net.cyperia.challenges.plugin.management.menu.generator.impl.challenge;

import net.cyperia.challenges.plugin.challenges.type.IChallenge;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.management.menu.generator.types.IChallengesMenuGenerator;
import net.cyperia.challenges.plugin.management.menu.generator.MultiPageMenuGenerator;
import net.cyperia.challenges.plugin.management.menu.generator.VersionedContentContainer;
import net.cyperia.challenges.plugin.management.menu.generator.impl.challenge.categorised.CategorisedMenuGenerator;
import net.cyperia.challenges.plugin.management.menu.generator.impl.custom.CustomListMenuGenerator;
import net.cyperia.challenges.plugin.management.menu.generator.impl.search.SearchResultHighlightMenuGenerator;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * @see ChallengeListMenuGenerator
 * @see CategorisedMenuGenerator
 * @see CustomListMenuGenerator
 */
public abstract class ChallengesMenuGenerator extends MultiPageMenuGenerator implements IChallengesMenuGenerator, VersionedContentContainer {

  public abstract int getEnabledCount();

  public abstract int getModifiedCount();

  @NotNull
  public abstract Optional<IChallenge> getFirstEnabledChallenge();

  // args: {0} enabled, {1} total, {2} first enabled name, {3} changed from default
  @NotNull
  public LocalizableMessage formatInfo(@NotNull MessageKey info) {
    return info.withArgs(
      getEnabledCount(), getCachedCount(),
      getFirstEnabledChallenge().map(IChallenge::getName).orElse(MessageKey.of("generic.disabled")),
      getModifiedCount()
    );
  }

  @Nullable
  public abstract ChallengeListMenuGenerator getChallengeListMenuGenerator(@NotNull IChallenge challenge);

  @Override
  public void showFinable(@NotNull Player player, @NotNull IChallenge challenge) {
    ChallengeListMenuGenerator listGenerator = getChallengeListMenuGenerator(challenge);
    if (listGenerator != null) {
      int page = listGenerator.getPageOfChallenge(player, challenge);
      int slot = listGenerator.getDisplaySlotOfChallenge(player, challenge);
      SearchResultHighlightMenuGenerator viewGenerator = new SearchResultHighlightMenuGenerator(listGenerator, page, slot);
      viewGenerator.openMenu(player);
    } else {
      openPageOf(player, challenge);
    }
  }
}
