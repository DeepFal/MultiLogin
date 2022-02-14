package moe.caa.multilogin.core.util;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 占位详情
 */
@Getter
public class FormatContent {
    private final List<FormatEntry> formatEntries;

    private FormatContent(FormatEntry... entries) {
        formatEntries = Arrays.asList(entries);
    }

    /**
     * 构建空占位内容
     *
     * @return 空占位内容
     */
    public static FormatContent empty() {
        return new FormatContent();
    }

    /**
     * 创建一个占位内容
     *
     * @param entries 占位聚合
     * @return 占位内容
     */
    public static FormatContent createContent(FormatEntry... entries) {
        return new FormatContent(entries);
    }

    /**
     * 创建一个占位内容
     *
     * @param entries 占位聚合
     * @return 占位内容
     */
    public static FormatContent createContent(Map<String, Object> entries) {
        return createContent(
                entries.entrySet().stream()
                        .map(e -> FormatEntry.builder().name(e.getKey()).content(e.getValue()).build())
                        .toArray(FormatEntry[]::new)
        );
    }

    /**
     * 占位填充数据
     *
     * @param source  源字符串
     * @param content 填充内容
     * @return 完善后的字符串
     */
    public static String format(String source, FormatContent content) {
        List<FormatContent.FormatEntry> entries = content.getFormatEntries();
        for (int i = 0; i < entries.size(); i++) {
            source = source.replace("{" + i + "}", entries.get(i).getContent().toString());
            source = source.replace("{" + entries.get(i).getName() + "}", entries.get(i).getContent().toString());
        }
        return source;
    }

    public String format(String source) {
        return format(source, this);
    }

    /**
     * 代表占位节
     */
    @Data
    @Builder()
    public static class FormatEntry {
        private final String name;
        private final Object content;
    }
}