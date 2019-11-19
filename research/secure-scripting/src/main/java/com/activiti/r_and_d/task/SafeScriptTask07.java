package com.activiti.r_and_d.task;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.mozilla.javascript.*;

import java.lang.management.ManagementFactory;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class SafeScriptTask07 implements JavaDelegate {

    private static final MyFactory MY_FACTORY = new MyFactory();

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(10);

    private Expression script;

    @Override
    public void execute(final DelegateExecution execution) throws Exception {

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {

                Context context = Context.enter();
                MyFactory.MyContext myContext = (MyFactory.MyContext) context;
                myContext.setThreadId(Thread.currentThread().getId());

                try {

                    context.setOptimizationLevel(-1);
                    context.setMaximumInterpreterStackDepth(10);

//                    Scriptable scope = context.initStandardObjects();

                    ScriptableObject scope = new ImporterTopLevel(context);

                    Object result = context.evaluateString(scope,
                            (String) script.getValue(execution), "<cmd>", 0, null);
                    System.out.println(Context.toString(result));

                } finally {
                    Context.exit();
                }

            }

        });

        thread.start();
        thread.join();

        System.out.println("Java Delegate done");
    }


    public static class MyFactory extends ContextFactory {

        private com.sun.management.ThreadMXBean threadMXBean; // Only Oracle / OpenJDK (only checked those)

        public MyFactory() {
            this.threadMXBean = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
        }

        // Custom {@link Context} to store execution time.
        public static class MyContext extends Context {

            private long startTime;
            private long threadId;
            private long startMemory;

            public long getStartTime() {
                return startTime;
            }

            public void setStartTime(long startTime) {
                this.startTime = startTime;
            }

            public long getThreadId() {
                return threadId;
            }

            public void setThreadId(long threadId) {
                this.threadId = threadId;
            }

        }

        static {
            // Initialize GlobalFactory with this custom factory
            ContextFactory.initGlobal(new MyFactory());
        }

        // Override {@link #makeContext()}
        protected Context makeContext() {
            MyContext cx = new MyContext();
            // Make Rhino runtime to call observeInstructionCount
            // each 10000 bytecode instructions
            cx.setInstructionObserverThreshold(10);
            return cx;
        }

        // Override {@link #observeInstructionCount(Context, int)}
        protected void observeInstructionCount(Context cx, int instructionCount) {
            System.out.println("observeInstructionCount");
            MyContext myContext = (MyContext) cx;
            long currentTime = System.currentTimeMillis();

            // Time
            if (currentTime - myContext.startTime > 3 * 1000) {
                // More then 10 seconds from Context creation time:
                // it is time to stop the script.
                // Throw Error instance to ensure that script will never
                // get control back through catch or finally.
                throw new Error();
            }

            // Memory
            if (myContext.startMemory <= 0) {
                myContext.startMemory = threadMXBean.getThreadAllocatedBytes(myContext.getThreadId());
                System.out.println("Start = " + myContext.startMemory);
            } else {
                long currentAllocatedBytes = threadMXBean.getThreadAllocatedBytes(myContext.getThreadId());
                System.out.println("Current allocated bytes = " + currentAllocatedBytes);
                System.out.println("Diff = " + ((currentAllocatedBytes - myContext.startMemory)/1024));
                if (currentAllocatedBytes - myContext.startMemory >= 3145728) { // 3MB
                    throw new Error("3 MB memory limit reached");
                }
            }

        }

        // Override {@link #doTopCall(Callable, Context, Scriptable, Scriptable, Object[])}
        protected Object doTopCall(Callable callable, Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
            MyContext mcx = (MyContext) cx;
            mcx.setStartTime(System.currentTimeMillis());
            return super.doTopCall(callable, cx, scope, thisObj, args);
        }
    }

}
