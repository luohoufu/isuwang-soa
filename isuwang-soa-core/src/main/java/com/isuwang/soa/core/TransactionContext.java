package com.isuwang.soa.core;

/**
 * 服务端上下文
 *
 * @author craneding
 * @date 15/9/24
 */
public class TransactionContext extends Context {

    public static class Factory {
        private static ThreadLocal<TransactionContext> threadLocal = new ThreadLocal<>();

        public static TransactionContext getNewInstance() {
            return new TransactionContext();
        }

        public static TransactionContext setCurrentInstance(TransactionContext context) {
            threadLocal.set(context);

            return context;
        }

        public static TransactionContext getCurrentInstance() {
            TransactionContext context = threadLocal.get();

            if (context == null) {
                context = getNewInstance();

                threadLocal.set(context);
            }

            return context;
        }

        public static void removeCurrentInstance() {
            threadLocal.remove();
        }
    }


}
