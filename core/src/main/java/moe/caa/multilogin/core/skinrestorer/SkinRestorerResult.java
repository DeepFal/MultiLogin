package moe.caa.multilogin.core.skinrestorer;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import moe.caa.multilogin.api.auth.yggdrasil.response.Property;

/**
 * 返回皮肤修复结果
 */
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SkinRestorerResult {
    private final Property result;

    protected static SkinRestorerResult ofSucceed(Property result) {
        return new SkinRestorerResult(result);
    }

    protected static SkinRestorerResult ofFailed() {
        return new SkinRestorerResult(null);
    }

    public Property getResult() {
        return result;
    }

    public boolean hasPassed() {
        return result != null;
    }
}
