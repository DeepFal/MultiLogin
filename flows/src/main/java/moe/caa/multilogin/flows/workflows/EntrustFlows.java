package moe.caa.multilogin.flows.workflows;

import lombok.Getter;
import moe.caa.multilogin.flows.ProcessingFailedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 代表一个并行的委托流
 * 所有工序并行尝试加工这个零件，直到有一条工序能顺利完成。
 */
public class EntrustFlows<C> extends IFlows<C> {
    @Getter
    private final List<IFlows<C>> steps;

    public EntrustFlows() {
        this(new ArrayList<>());
    }

    public EntrustFlows(List<IFlows<C>> steps) {
        this.steps = (steps);
    }

    @Override
    public Signal run(C context) {
        // 存放成功的标志信号
        AtomicBoolean passed = new AtomicBoolean(false);
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
                    // 这个工序能完成这项任务，释放信号
                    if (signal == Signal.PASSED) {
                        passed.set(true);
                        latch.countDown();
                    }
                } finally {
                    currentTasks.remove(step);
                    // 没人能完成这个工序，释放信号
                    if (currentTasks.isEmpty()) latch.countDown();
                }
            });
        }

        if (flag) try {
            latch.await();
        } catch (InterruptedException e) {
            throw new ProcessingFailedException(name(), e);
        }

        return passed.get() ? Signal.PASSED : Signal.TERMINATED;
    }

    @Override
    public String name() {
        return "EntrustFlows";
    }
}
