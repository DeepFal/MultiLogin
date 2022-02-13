package moe.caa.multilogin.core.config;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 存储 Yggdrasil 实例的对象
 */
@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class YggdrasilService {
    private final String path;
    private final boolean enable;
    private final String name;

    // body section start.
    private final String url;
    private final boolean postMode;
    private final boolean passIp;
    private final String ipContent;
    private final String postContent;
    // body section end.

    private final UUIDTransform transformUuid;
    private final boolean transformRepeatAdjust;
    private final String nameAllowedRegular;
    private final boolean whitelist;
    private final boolean refuseRepeatedLogin;
    private final int authRetry;
    private final SkinRestorerType skinRestorer;
    private final SkinRestorerMethodType skinRestorerMethod;
    private final int skinRestorerRetry;

    /**
     * 通过配置文件对象解析
     */
    protected static YggdrasilService parseConfig(String path, CommentedConfigurationNode section) throws SerializationException {
        boolean enable = section.node("enable").getBoolean(true);
        String name = section.node("name").getString("");

        CommentedConfigurationNode body = section.node("body");
        String url = Objects.requireNonNull(body.node("url").getString(), "Url is null at " + Arrays.stream(body.node("url").path().array()).map(Object::toString).collect(Collectors.joining(".")));
        boolean postMode = body.node("postMode").getBoolean(false);
        boolean passIp = body.node("passIp").getBoolean(false);
        String ipContent = body.node("ipContent").getString("&ip={0}");
        String postContent = body.node("postContent").getString("{\"username\":\"{username}\", \"serverId\":\"{serverId}\"}");

        UUIDTransform transformUuid = section.node("transformUuid").get(UUIDTransform.class, UUIDTransform.DEFAULT);
        boolean transformRepeatAdjust = section.node("transformRepeatAdjust").getBoolean(false);
        String nameAllowedRegular = section.node("nameAllowedRegular").getString("");
        boolean whitelist = section.node("whitelist").getBoolean(false);
        boolean refuseRepeatedLogin = section.node("refuseRepeatedLogin").getBoolean(false);
        int authRetry = section.node("authRetry").getInt(0);
        SkinRestorerType skinRestorer = section.node("skinRestorer").get(SkinRestorerType.class, SkinRestorerType.OFF);
        SkinRestorerMethodType skinRestorerMethod = section.node("skinRestorerMethod").get(SkinRestorerMethodType.class, SkinRestorerMethodType.URL);
        int skinRestorerRetry = section.node("skinRestorerRetry").getInt(2);

        return new YggdrasilService(path, enable, name,
                url, postMode, passIp, ipContent, postContent,
                transformUuid, transformRepeatAdjust, nameAllowedRegular, whitelist, refuseRepeatedLogin, authRetry,
                skinRestorer, skinRestorerMethod, skinRestorerRetry);
    }
}
