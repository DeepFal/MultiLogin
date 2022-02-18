package moe.caa.multilogin.core.config;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import moe.caa.multilogin.core.auth.yggdrasil.HasJoinedContext;
import moe.caa.multilogin.core.util.FormatContent;
import moe.caa.multilogin.core.util.ValueUtil;
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
    // 毫不相干的内容，仅在命令中使用
    private final String path;

    private final int id;
    private final boolean enable;

    // body section start.
    private final String url;
    private final boolean postMode;
    private final boolean passIp;
    private final String ipContent;
    private final String postContent;
    // body section end.

    private final UUIDTransform transformUuid;
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
        int id = section.node("id").getInt(Integer.MIN_VALUE);
        if (id < 0) {
            if (id == Integer.MIN_VALUE) {
                throw new NullPointerException("id is null at " + Arrays.stream(section.node("id").path().array()).map(Object::toString).collect(Collectors.joining(".")));
            }
            throw new IllegalArgumentException("id value is negative at " + Arrays.stream(section.node("id").path().array()).map(Object::toString).collect(Collectors.joining(".")));
        }
        if (id > 255) {
            throw new IllegalArgumentException("id value is greater than 255 at " + Arrays.stream(section.node("id").path().array()).map(Object::toString).collect(Collectors.joining(".")));
        }

        boolean enable = section.node("enable").getBoolean(true);

        CommentedConfigurationNode body = section.node("body");
        String url = Objects.requireNonNull(body.node("url").getString(), "url is null at " + Arrays.stream(body.node("url").path().array()).map(Object::toString).collect(Collectors.joining(".")));
        boolean postMode = body.node("postMode").getBoolean(false);
        boolean passIp = body.node("passIp").getBoolean(false);
        String ipContent = body.node("ipContent").getString("&ip={0}");
        String postContent = body.node("postContent").getString("{\"username\":\"{username}\", \"serverId\":\"{serverId}\"}");

        UUIDTransform transformUuid = section.node("transformUuid").get(UUIDTransform.class, UUIDTransform.DEFAULT);
        String nameAllowedRegular = section.node("nameAllowedRegular").getString("");
        boolean whitelist = section.node("whitelist").getBoolean(false);
        boolean refuseRepeatedLogin = section.node("refuseRepeatedLogin").getBoolean(false);
        int authRetry = section.node("authRetry").getInt(0);

        CommentedConfigurationNode skinRestorer = section.node("skinRestorer");
        SkinRestorerType restorer = skinRestorer.node("restorer").get(SkinRestorerType.class, SkinRestorerType.OFF);
        SkinRestorerMethodType method = skinRestorer.node("method").get(SkinRestorerMethodType.class, SkinRestorerMethodType.URL);
        int retry = skinRestorer.node("retry").getInt(2);

        return new YggdrasilService(path, id, enable,
                url, postMode, passIp, ipContent, postContent,
                transformUuid, nameAllowedRegular, whitelist, refuseRepeatedLogin, authRetry,
                restorer, method, retry);
    }

    /**
     * 构建请求 URL
     */
    public String buildUrl(HasJoinedContext context) {
        return FormatContent.createContent(
                FormatContent.FormatEntry.builder().name("username").content(context.getUsername()).build(),
                FormatContent.FormatEntry.builder().name("serverId").content(context.getServerId()).build(),
                FormatContent.FormatEntry.builder().name("ip").content(passIp ? buildIpContent(context.getIp()) : "").build()
        ).format(url);
    }

    /**
     * 构建 ip 信息内容
     */
    private String buildIpContent(String ip) {
        if (ValueUtil.isEmpty(ipContent)) return "";
        return FormatContent.createContent(
                FormatContent.FormatEntry.builder().name("ip").content(ip).build()
        ).format(ipContent);
    }

    /**
     * 构建 POST 请求数据
     */
    public String buildPostContent(HasJoinedContext context) {
        if (!postMode) return "";
        return FormatContent.createContent(
                FormatContent.FormatEntry.builder().name("username").content(context.getUsername()).build(),
                FormatContent.FormatEntry.builder().name("serverId").content(context.getServerId()).build(),
                FormatContent.FormatEntry.builder().name("ip").content(passIp ? buildIpContent(context.getIp()) : "").build()
        ).format(postContent);
    }
}
