package com.activiti.r_and_d.task;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.mozilla.javascript.ClassShutter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;


public class SafeScriptTask04 implements JavaDelegate {

    private Expression script;

    @Override
    public void execute(final DelegateExecution execution) throws Exception {

        Context context = Context.enter();
        try {

            // Whitelisting
            context.setClassShutter(new MyClassShutter());

            context.setGenerateObserverCount(true);
            context.setInstructionObserverThreshold(1);

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

}
