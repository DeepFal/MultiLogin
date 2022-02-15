package moe.caa.multilogin.flows.workflows;

import lombok.Getter;
import moe.caa.multilogin.flows.ProcessingFailedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 代表一个并行的车间
 * 所有工序必须全部 PASS
 */
public class ParallelFlows<C> extends IFlows<C> {
    @Getter
    private final List<IFlows<C>> steps;

    public ParallelFlows() {
        this(new ArrayList<>());
    }

    public ParallelFlows(List<IFlows<C>> steps) {
        this.steps = (steps);
    }

    @Override
    public Signal run(C context) {
        // 存放终止信号
        AtomicBoolean terminate = new AtomicBoolean(false);
        // 信号
        CountDownLatch latch = new CountDownLatch(1);
        // 存放当前有多少工序加工
        List<IFlows<C>> currentTasks = Collections.synchronizedList(new ArrayList<>());
        // 避免阻死
        boolean flag = false;
        for (IFlows<C> step : steps) {
            flag = true;
            currentTasks.add(step);
            IFlows.getExecutorService().execute(() -> {
                try {
                    Signal signal = step.run(context);
                    if (signal != Signal.TERMINATED) return;
                    // 这个工序不能完成当前任务，释放信号
                    terminate.set(true);
                    latch.countDown();
                } finally {
                    currentTasks.remove(step);
                    // 全部完成这个工序，释放信号
                    if (currentTasks.isEmpty()) latch.countDown();
                }
            });
        }

        if (flag) try {
            latch.await();
        } catch (InterruptedException e) {
            throw new ProcessingFailedException(name(), e);
        }
        return terminate.get() ? Signal.TERMINATED : Signal.PASSED;
    }

    @Override
    public String name() {
        return "ParallelFlows";
    }
}
