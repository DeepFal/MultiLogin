package moe.caa.multilogin.flows.workflows;

import moe.caa.multilogin.flows.FlowContext;

import java.util.function.Consumer;

/**
 * 表示一个工作流
 *
 * @param <V> 加工零件
 */
public interface IFlows<V> {

    static <V> IFlows<V> of(String name, Consumer<FlowContext<V>> contextConsumer) {
        return new IFlows<>() {
            @Override
            public FlowContext<V> run(FlowContext<V> context) {
                contextConsumer.accept(context);
                return context;
            }

            @Override
            public String name() {
                return name;
            }
        };
    }

    /**
     * 开始加工
     */
    FlowContext<V> run(FlowContext<V> context);

    /**
     * 获得工序名称
     */
    String name();
}
