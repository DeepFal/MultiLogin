package moe.caa.multilogin.core.config;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.BiFunction;

/**
 * UUID生成规则的枚举
 */
public enum UUIDTransform {

    /**
     * Yggdrasil验证服务器提供的UUID
     */
    DEFAULT((u, s) -> u),

    /**
     * 生成离线UUID（盗版UUID）
     */
    OFFLINE((u, s) -> UUID.nameUUIDFromBytes(("OfflinePlayer:" + s).getBytes(StandardCharsets.UTF_8))),

    /**
     * 随机UUID
     */
    RANDOM((u, s) -> UUID.randomUUID());

    private final BiFunction<UUID, String, UUID> func;

    UUIDTransform(BiFunction<UUID, String, UUID> func) {
        this.func = func;
    }

    /**
     * 生成 UUID
     *
     * @param onlineUuid 玩家在 Yggdrasil 的在线 UUID
     * @param name       玩家的 name
     * @return 生成结果
     */
    public UUID getResultUuid(UUID onlineUuid, String name) {
        return func.apply(onlineUuid, name);
    }
}
