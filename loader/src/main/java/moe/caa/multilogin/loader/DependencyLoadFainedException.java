package moe.caa.multilogin.loader;

/**
 * 依赖加载异常
 */
public class DependencyLoadFainedException extends Exception {
    public DependencyLoadFainedException(String message) {
        super(message);
    }

    public DependencyLoadFainedException(String message, Throwable cause) {
        super(message, cause);
    }
}
