package fun.ksnb.multilogin.velocity.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.AccessibleObject;

/**
 * 反射工具类库
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReflectUtil {

    /**
     * 操作 accessible 属性
     */
    public static AccessibleObject handleAccessible(AccessibleObject accessibleObject, boolean newAccessible) {
        accessibleObject.setAccessible(newAccessible);
        return accessibleObject;
    }
}
