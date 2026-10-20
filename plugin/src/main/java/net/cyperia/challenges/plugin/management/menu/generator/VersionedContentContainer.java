package net.cyperia.challenges.plugin.management.menu.generator;

import net.cyperia.challenges.plugin.challenges.type.ReleaseInfo;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.ListIterator;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public interface VersionedContentContainer {

  /**
   * @see net.cyperia.challenges.plugin.challenges.type.ReleaseInfo#getSince(Object)
   */
  int getNewCount();

  /**
   * @see net.cyperia.challenges.plugin.challenges.type.ReleaseInfo#getUpdated(Object)
   */
  int getUpdatedCount();

  /**
   * @see net.cyperia.challenges.plugin.challenges.type.ReleaseInfo#isBeta(Object)
   */
  default int getBetaCount() {
    return 0;
  }

  interface FromList extends VersionedContentContainer {

    @NotNull
    List<?> getVersionedContent();

    @Override
    default int getNewCount() {
      return Helper.countNew(getVersionedContent());
    }

    @Override
    default int getUpdatedCount() {
      return Helper.countUpdated(getVersionedContent());
    }

    @Override
    default int getBetaCount() {
      return Helper.countBeta(getVersionedContent());
    }
  }

  // extracted so static methods don't leak into their children
  final class Helper {

    private Helper() {
    }

    public static int countNew(boolean allInFront, int startOffset, @NotNull List<?> items) {
      return count(allInFront, startOffset, ReleaseInfo::isNew, VersionedContentContainer::getNewCount, items);
    }

    public static int countNew(@NotNull List<?> items) {
      return countNew(false, 0, items);
    }

    public static int countUpdated(boolean allInFront, int startOffset, @NotNull List<?> items) {
      return count(allInFront, startOffset, ReleaseInfo::isUpdated, VersionedContentContainer::getUpdatedCount, items);
    }

    public static int countUpdated(@NotNull List<?> items) {
      return countUpdated(false, 0, items);
    }

    public static int countBeta(@NotNull List<?> items) {
      return count(false, 0, ReleaseInfo::isBeta, VersionedContentContainer::getBetaCount, items);
    }

    private static int count(boolean allInFront, int startOffset, @NotNull Predicate<? super Object> filter,
                             @NotNull ToIntFunction<VersionedContentContainer> extractor,
                             @NotNull List<?> items) {
      int count = 0;
      ListIterator<?> iterator = items.listIterator(startOffset);
      while (iterator.hasNext()) {
        Object item = iterator.next();

        if (item instanceof VersionedContentContainer container) {
          int containerCount = extractor.applyAsInt(container);
          if (containerCount > 0) {
            count += containerCount;
            continue;
          }
        }

        if (filter.test(item)) {
          count++;
        } else if (allInFront) {
          break;
        }
      }
      return count;
    }

  }

}
