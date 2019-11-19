package com.activiti.r_and_d.task;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.mozilla.javascript.*;


public class SafeScriptTask06 implements JavaDelegate {

    private static final MyFactory MY_FACTORY = new MyFactory();

    private Expression script;

    @Override
    public void execute(final DelegateExecution execution) throws Exception {

        Context context = Context.enter();
        try {

            // Whitelisting
//			context.setClassShutter(new MyClassShutter());

            context.setOptimizationLevel(-1);
            context.setMaximumInterpreterStackDepth(10);

            Scriptable scope = context.initStandardObjects();
            Object result = context.evaluateString(scope,
                    (String) script.getValue(execution), "<cmd>", 0, null);
            System.out.println(Context.toString(result));

        } finally {
            Context.exit();
        }

        System.out.println("Java Delegate done");
    }

    public static class MyClassShutter implements ClassShutter {

        @Override
        public boolean visibleToScripts(String fullClassName) {
            System.out.println("Can i use " + fullClassName + " ?");
            return false;
        }

    }

    public static class MyFactory extends ContextFactory {

        // Custom {@link Context} to store execution time.
        private static class MyContext extends Context {
            long startTime;
        }

        static {
            // Initialize GlobalFactory with custom factory
            ContextFactory.initGlobal(new MyFactory());
        }

        // Override {@link #makeContext()}
        protected Context makeContext() {
            MyContext cx = new MyContext();
            // Make Rhino runtime to call observeInstructionCount
            // each 10000 bytecode instructions
            cx.setInstructionObserverThreshold(10000);
            return cx;
        }

        // Override {@link #observeInstructionCount(Context, int)}
        protected void observeInstructionCount(Context cx, int instructionCount) {
            System.out.println("observeInstructionCount");
            MyContext mcx = (MyContext) cx;
            long currentTime = System.currentTimeMillis();
            if (currentTime - mcx.startTime > 3 * 1000) {
                // More then 10 seconds from Context creation time:
                // it is time to stop the script.
                // Throw Error instance to ensure that script will never
                // get control back through catch or finally.
                throw new Error();
            }
        }

        // Override {@link #doTopCall(Callable, Context, Scriptable, Scriptable, Object[])}
        protected Object doTopCall(Callable callable, Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
            MyContext mcx = (MyContext) cx;
            mcx.startTime = System.currentTimeMillis();
            return super.doTopCall(callable, cx, scope, thisObj, args);
        }
    }

}
