package com.activiti.impl.bpmn.behavior;

import org.activiti.bpmn.model.ScriptTask;
import org.activiti.engine.impl.bpmn.behavior.ScriptTaskActivityBehavior;
import org.activiti.engine.impl.bpmn.parser.factory.DefaultActivityBehaviorFactory;
import org.mozilla.javascript.ContextFactory;

/**
 * @author Joram Barrez
 */
public class SecureScriptCapableActivityBehaviorFactory extends DefaultActivityBehaviorFactory {

    private static final String LANGUAGE_JAVASCRIPT = "javascript";

    private static SecureScriptContextFactory SECURE_SCRIPT_CONTEXT_FACTORY;

    protected SecureScriptTaskActivityBehaviorConfigurator configurator;
    protected SecureScriptClassShutter secureScriptClassShutter;

    public SecureScriptCapableActivityBehaviorFactory(SecureScriptTaskActivityBehaviorConfigurator configurator) {
        this.configurator = configurator;
    }

    public ScriptTaskActivityBehavior createScriptTaskActivityBehavior(ScriptTask scriptTask) {
        String language = scriptTask.getScriptFormat();
        if (LANGUAGE_JAVASCRIPT.equalsIgnoreCase(language)) {
            return createSecureScriptTaskActivityBehavior(scriptTask, language);
        } else {
            return super.createScriptTaskActivityBehavior(scriptTask);
        }
    }

    protected ScriptTaskActivityBehavior createSecureScriptTaskActivityBehavior(ScriptTask scriptTask, String language) {

        SecureScriptTaskActivityBehavior secureScriptTaskActivityBehavior = new SecureScriptTaskActivityBehavior(
                scriptTask.getId(), scriptTask.getScript(), language, scriptTask.getResultVariable(), scriptTask.isAutoStoreVariables());

        // Maximum execution time/memory settings
        if (SECURE_SCRIPT_CONTEXT_FACTORY == null) {
            initSecureScriptContextFactory();
        }

        return secureScriptTaskActivityBehavior;
    }

    /**
     * Needs to be synchronized: multiple threads can parse script task, but only
     * one should initialise the SecureScriptContextFactory.
     */
    protected synchronized void initSecureScriptContextFactory() {
        if (SECURE_SCRIPT_CONTEXT_FACTORY == null) {
            SECURE_SCRIPT_CONTEXT_FACTORY = new SecureScriptContextFactory();

            SECURE_SCRIPT_CONTEXT_FACTORY.setOptimizationLevel(configurator.getScriptOptimizationLevel());

            if (configurator.isEnableClassWhiteListing() || configurator.getWhiteListedClasses() != null) {
                secureScriptClassShutter = new SecureScriptClassShutter();
                if (configurator.getWhiteListedClasses() != null && configurator.getWhiteListedClasses().size() > 0) {
                    secureScriptClassShutter.setWhiteListedClasses(configurator.getWhiteListedClasses());
                }
                SECURE_SCRIPT_CONTEXT_FACTORY.setClassShutter(secureScriptClassShutter);
            }

            if (configurator.getMaxScriptExecutionTime() > 0L) {
                SECURE_SCRIPT_CONTEXT_FACTORY.setMaxScriptExecutionTime(configurator.getMaxScriptExecutionTime());
            }

            if (configurator.getMaxMemoryUsed() > 0L) {
                SECURE_SCRIPT_CONTEXT_FACTORY.setMaxMemoryUsed(configurator.getMaxMemoryUsed());
            }

            if (configurator.getMaxStackDepth() > 0) {
                SECURE_SCRIPT_CONTEXT_FACTORY.setMaxStackDepth(configurator.getMaxStackDepth());
            }

            if (configurator.getMaxScriptExecutionTime() > 0L || configurator.getMaxMemoryUsed() > 0L) {
                SECURE_SCRIPT_CONTEXT_FACTORY.setObserveInstructionCount(configurator.getNrOfInstructionsBeforeStateCheckCallback());
            }

            ContextFactory.initGlobal(SECURE_SCRIPT_CONTEXT_FACTORY);
        }
    }

    public SecureScriptTaskActivityBehaviorConfigurator getConfigurator() {
        return configurator;
    }

    public void setConfigurator(SecureScriptTaskActivityBehaviorConfigurator configurator) {
        this.configurator = configurator;
    }

    public SecureScriptClassShutter getSecureScriptClassShutter() {
        return secureScriptClassShutter;
    }

    public void setSecureScriptClassShutter(SecureScriptClassShutter secureScriptClassShutter) {
        this.secureScriptClassShutter = secureScriptClassShutter;
    }

    public static SecureScriptContextFactory getSecureScriptContextFactory() {
        return SECURE_SCRIPT_CONTEXT_FACTORY;
    }

    public static void setSecureScriptContextFactory(SecureScriptContextFactory secureScriptContextFactory) {
        SECURE_SCRIPT_CONTEXT_FACTORY = secureScriptContextFactory;
    }
}
