package fun.ksnb.multilogin.velocity.main.v5t.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class IdentifiedKeyInvocationHandler implements InvocationHandler {
    private final Object obj;

    public IdentifiedKeyInvocationHandler(Object obj) {
        this.obj = obj;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("hasExpired")) {
            return false;
        }
        if (method.getName().equals("isSignatureValid")) {
            return true;
        }
        if (method.getName().equals("internalAddHolder")) {
            return true;
        }
        return method.invoke(obj, args);
    }
}
