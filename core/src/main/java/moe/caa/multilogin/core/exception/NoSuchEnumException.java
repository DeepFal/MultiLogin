package moe.caa.multilogin.core.exception;

import lombok.NoArgsConstructor;

/**
 * 没有检索到 enum 类实例异常
 */
@NoArgsConstructor
public class NoSuchEnumException extends ReflectiveOperationException {

    /**
     * 构建这个异常
     *
     * @param s 异常信息
     */
    public NoSuchEnumException(String s) {
        super(s);
    }
}
