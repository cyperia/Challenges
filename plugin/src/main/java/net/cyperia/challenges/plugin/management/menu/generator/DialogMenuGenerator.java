package net.cyperia.challenges.plugin.management.menu.generator;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.RegistryBuilderFactory;
import io.papermc.paper.registry.data.dialog.DialogRegistryEntry;
import net.cyperia.challenges.plugin.content.i18n.TranslationManager;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public abstract class DialogMenuGenerator<S> implements IMenuGenerator {

  @Override
  public final void openMenu(@NotNull Player player, int page) {
    openMenu(player);
  }

  @Override
  public void openMenu(@NotNull Player player) {
    SoundSample.PLOP.play(player);
    openMenu(player, createDefaultState(player));
  }

  public void openMenu(@NotNull Player player, @NotNull S state) {
    player.showDialog(createNewDialog(player, state));
  }

  @NotNull
  protected Dialog createNewDialog(@NotNull Player player, @NotNull S state) {
    Locale locale = TranslationManager.getLocaleOf(player);
    return Dialog.create(factory -> this.setupDialog(factory, state, player, locale));
  }

  protected void setupDialog(@NotNull RegistryBuilderFactory<Dialog, ? extends DialogRegistryEntry.Builder> factory, @NotNull S state,
                             @NotNull Player viewer, @NotNull Locale locale) {
    setupDialog(factory.empty(), state, viewer, locale);
  }

  @NotNull
  protected abstract S createDefaultState(@NotNull Player player);

  protected abstract void setupDialog(@NotNull DialogRegistryEntry.Builder dialog, @NotNull S state, @NotNull Player viewer, @NotNull Locale locale);

  @Override
  public void updatePages() {
    // noop; nothing to cache; generated dynamically
  }

  @Override
  public void updatePage(int page) {
    // noop; nothing to cache; generated dynamically
  }

  @Override
  public final int getPageCount(@NotNull Player viewer) {
    return 1;
  }
}
