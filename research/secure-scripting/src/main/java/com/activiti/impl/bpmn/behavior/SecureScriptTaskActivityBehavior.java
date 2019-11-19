package com.activiti.impl.bpmn.behavior;

import org.activiti.engine.impl.bpmn.behavior.ScriptTaskActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.mozilla.javascript.*;

/**
 * @author Joram Barrez
 */
public class SecureScriptTaskActivityBehavior extends ScriptTaskActivityBehavior {

    public SecureScriptTaskActivityBehavior(String scriptTaskId, String script,
                                            String language, String resultVariable, boolean storeScriptVariables) {
        super(scriptTaskId, script, language, resultVariable, storeScriptVariables);
    }

    @Override
    public void execute(ActivityExecution execution) throws Exception {
        Context context = Context.enter();
        try {
            Scriptable scope = context.initStandardObjects();

            SecureScriptScope secureScriptScope = new SecureScriptScope(execution);
            scope.setPrototype(secureScriptScope);

            context.evaluateString(scope, script, "<script>", 0, null);
        } finally {
            Context.exit();
        }
    }

}
