package moe.caa.multilogin.flows.workflows;

import moe.caa.multilogin.flows.FlowContext;

/**
 * 表示一个工作流
 *
 * @param <C> 加工上下文
 */
public interface IFlows<C extends FlowContext> {
    /**
     * 开始加工
     */
    C run(C context);

    /**
     * 获得工序名称
     */
    String name();
}
