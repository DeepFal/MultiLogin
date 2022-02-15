package moe.caa.multilogin.flows.workflows;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * 代表顺序流
 */
public class SequenceFlows<C> extends IFlows<C> {
    @Getter
    private final List<IFlows<C>> steps;

    public SequenceFlows() {
        this(new ArrayList<>());
    }

    public SequenceFlows(List<IFlows<C>> steps) {
        this.steps = (steps);
    }

    @Override
    public Signal run(C context) {
        for (IFlows<C> step : steps) {
            Signal signal = step.run(context);
            // PASS， 继续执行
            if (signal == Signal.PASSED) continue;
            // 中断
            if (signal == Signal.TERMINATED) return Signal.TERMINATED;
        }
        return Signal.PASSED;
    }

    @Override
    public String name() {
        return "SequenceFlows";
    }
}
