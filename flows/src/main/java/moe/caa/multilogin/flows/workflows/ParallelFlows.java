package moe.caa.multilogin.flows.workflows;

import lombok.Getter;
import moe.caa.multilogin.flows.FlowContext;
import moe.caa.multilogin.flows.ProcessingFailedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 代表一个并行的车间
 * 所有工序必须全部 PASS
 */
public class ParallelFlows<T> implements IFlows<T> {
    @Getter
    private final List<IFlows<T>> steps;

    public ParallelFlows() {
        this(new ArrayList<>());
    }

    public ParallelFlows(List<IFlows<T>> steps) {
        this.steps = (steps);
    }

    @Override
    public FlowContext<T> run(FlowContext<T> context) {
        // 信号
        CountDownLatch latch = new CountDownLatch(1);
        // 异常信号
        AtomicReference<IFlows<T>> error = new AtomicReference<>();
        // 存放当前有多少工序加工
        List<IFlows<T>> currentTasks = Collections.synchronizedList(new ArrayList<>());
        // 避免阻死
        boolean flag = false;
        for (IFlows<T> step : steps) {
            flag = true;
            currentTasks.add(step);
            FlowContext.getExecutorService().execute(() -> {
                try {
                    final FlowContext<T> run = step.run(context);
                    // 这个工序异常
                    if (run.getSignal() == FlowContext.Signal.ERROR) error.set(step);
                    // 这个工序不能完成当前任务，释放信号
                    if (run.getSignal() != FlowContext.Signal.PASS) latch.countDown();
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
        if (context.getSignal() == FlowContext.Signal.ERROR)
            throw new ProcessingFailedException(error.get().name(), context.getThrowable());

        return context;
    }

    @Override
    public String name() {
        return "ParallelFlows";
    }
}
