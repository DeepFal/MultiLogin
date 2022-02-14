package moe.caa.multilogin.flows.workflows;

import lombok.Getter;
import moe.caa.multilogin.flows.FlowContext;
import moe.caa.multilogin.flows.ProcessingFailedException;

import java.util.ArrayList;
import java.util.List;

/**
 * 代表顺序流
 */
public class SequenceFlows<T> implements IFlows<T> {
    @Getter
    private final List<IFlows<T>> steps;

    public SequenceFlows() {
        this(new ArrayList<>());
    }

    public SequenceFlows(List<IFlows<T>> steps) {
        this.steps = (steps);
    }

    @Override
    public FlowContext<T> run(FlowContext<T> context) {
        for (IFlows<T> step : steps) {
            context = step.run(context);
            // PASS， 继续执行
            if (context.getSignal() == FlowContext.Signal.PASS) continue;
            // 中断
            if (context.getSignal() == FlowContext.Signal.TERMINATE) break;
            // 工序异常，直接报错
            throw new ProcessingFailedException(step.name(), context.getThrowable());
        }
        return context;
    }

    @Override
    public String name() {
        return "SequenceFlows";
    }
}
