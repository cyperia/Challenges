package net.cyperia.challenges.plugin.challenges.type.annotation;

import java.lang.annotation.*;

// Left out of the public release, for support code without a catalog entry; a catalog entry is marked "beta" there
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface Beta {
}
