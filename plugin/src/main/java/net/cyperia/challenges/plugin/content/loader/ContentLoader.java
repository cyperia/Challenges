package net.cyperia.challenges.plugin.content.loader;

import net.cyperia.challenges.plugin.Challenges;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public abstract class ContentLoader {

  @NotNull
  protected File getMessagesFolder() {
    return Challenges.getInstance().getDataFile("messages");
  }

  @NotNull
  protected final File getMessageFile(@NotNull String name, @NotNull String extension) {
    return new File(getMessagesFolder(), name + "." + extension);
  }

  @NotNull
  protected final String getGitHubUrl(@NotNull String path) {
    return "https://raw.githubusercontent.com/cyperia/Challenges/main/" + path;
  }

  protected final void callSubscribers() {
    Challenges.getInstance().getLoaderRegistry().callSubscribers(this.getClass());
  }

  protected abstract void load();

}
