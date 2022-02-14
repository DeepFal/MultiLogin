package moe.caa.multilogin.flows;

import lombok.Getter;
import lombok.ToString;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 流上下文
 *
 * @param <V> 零件
 */
@Getter
@ToString
public abstract class FlowContext<V> {
    private static final AtomicInteger asyncThreadId = new AtomicInteger(0);

    @Getter
    private static final ExecutorService executorService = Executors.newCachedThreadPool(r ->
            new Thread(r, "MultiLogin Flows #" + asyncThreadId.incrementAndGet())
    );
    private Throwable throwable;
    private Signal signal = Signal.PASS;

    public static void close() {
        executorService.shutdown();
    }

    public FlowContext<V> setSignal(Signal signal) {
        this.signal = signal;
        return this;
    }

    public FlowContext<V> setThrowable(Throwable throwable) {
        this.throwable = throwable;
        return this;
    }

    public abstract FlowContext<V> clone();

    /**
     * 代表加工信号
     */
    public enum Signal {

        /**
         * 通过
         */
        PASS,

        /**
         * 提前终止
         */
        TERMINATE,

        /**
         * 异常
         */
        ERROR;
    }
}
