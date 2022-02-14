package moe.caa.multilogin.flows;

import lombok.Getter;
import lombok.ToString;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 加工上下文
 */
@Getter
@ToString
public abstract class FlowContext {
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

    public FlowContext setSignal(Signal signal) {
        this.signal = signal;
        return this;
    }

    public FlowContext setThrowable(Throwable throwable) {
        this.throwable = throwable;
        return this;
    }

    public abstract FlowContext clone();

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
