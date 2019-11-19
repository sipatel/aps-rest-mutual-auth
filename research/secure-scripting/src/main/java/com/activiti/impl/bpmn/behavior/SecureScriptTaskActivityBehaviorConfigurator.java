package com.activiti.impl.bpmn.behavior;

import org.activiti.engine.cfg.AbstractProcessEngineConfigurator;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Joram Barrez
 */
public class SecureScriptTaskActivityBehaviorConfigurator extends AbstractProcessEngineConfigurator {

    /**
     * When true, by default all classes will be blacklisted and all classes that want to be used
     * will need to the whitelisted individually.
     */
    protected boolean enableClassWhiteListing;

    /**
     * Whitelisted classes for script execution.
     *
     * By default empty (i.e. everything is blacklisted)
     *
     * From the Rhino ClassShutter javadoc:
     *
     * Due to the fact that there is no package reflection in Java, this method will also be called with package names.
     * There is no way for Rhino to tell if "Packages.a.b" is a package name or a class that doesn't exist.
     * What Rhino does is attempt to load each segment of "Packages.a.b.c": It first attempts to load class "a",
     * then attempts to load class "a.b", then finally attempts to load class "a.b.c".
     * On a Rhino installation without any ClassShutter set, and without any of the above classes,
     * the expression "Packages.a.b.c" will result in a [JavaPackage a.b.c] and not an error.
     *
     * With ClassShutter supplied, Rhino will first call visibleToScripts before attempting to look up the class name.
     * If visibleToScripts returns false, the class name lookup is not performed and subsequent Rhino execution assumes
     * the class is not present. So for "java.lang.System.out.println" the lookup of "java.lang.System" is skipped
     * and thus Rhino assumes that "java.lang.System" doesn't exist. So then for "java.lang.System.out",
     * Rhino attempts to load the class "java.lang.System.out" because it assumes that "java.lang.System" is a package name.
     */
    protected Set<String> whiteListedClasses;

    /**
     * The maximum time (in ms) that a script is allowed to execute before stopping it.
     *
     * By default disabled.
     */
    protected long maxScriptExecutionTime = -1L;

    /**
     * Limits the stack depth while calling functions within the script.
     *
     * By default disabled.
     */
    protected int maxStackDepth = -1;

    /**
     * Limits the memory used by the script. If the memory limit is reached, an exception will be thrown
     * and the script will be stopped.
     */
    protected long maxMemoryUsed = -1L;

    /**
     * The maximum script execution time and memory usage is implemented using a callback
     * that is called every x instructions of the script. Note that these are not script
     * instructions, but java byte code instructions (which means one script line can
     * be thousands of byte code instructions!).
     */
    protected int nrOfInstructionsBeforeStateCheckCallback = 100;

    /**
     * By default, no script optimization is applied.
     * Change this setting to change the Rhino script optimization level.
     * Note: some simple performance tests seem to indicate that for basic scripts
     * upping this value actually has worse results ...
     *
     * Note: if using a maxStackDepth setting, the script optimization level will always be -1.
     */
    protected int scriptOptimizationLevel = -1;

    @Override
    public void beforeInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
        SecureScriptCapableActivityBehaviorFactory activityBehaviorFactory = new SecureScriptCapableActivityBehaviorFactory(this);
        processEngineConfiguration.setActivityBehaviorFactory(activityBehaviorFactory);
    }

    @Override
    public void configure(ProcessEngineConfigurationImpl processEngineConfiguration) {
        // Nothing to do here, the logic is in the beforeInit method
    }

    public boolean isEnableClassWhiteListing() {
        return enableClassWhiteListing;
    }

    public SecureScriptTaskActivityBehaviorConfigurator setEnableClassWhiteListing(boolean enableClassWhiteListing) {
        this.enableClassWhiteListing = enableClassWhiteListing;
        return this;
    }

    public Set<String> getWhiteListedClasses() {
        return whiteListedClasses;
    }

    public SecureScriptTaskActivityBehaviorConfigurator setWhiteListedClasses(Set<String> whiteListedClasses) {
        this.whiteListedClasses = whiteListedClasses;
        return this;
    }

    public SecureScriptTaskActivityBehaviorConfigurator addWhiteListedClass(String whiteListedClass) {
        if (this.whiteListedClasses == null) {
            this.whiteListedClasses = new HashSet<String>();
        }
        this.whiteListedClasses.add(whiteListedClass);
        return this;
    }

    public long getMaxScriptExecutionTime() {
        return maxScriptExecutionTime;
    }

    public SecureScriptTaskActivityBehaviorConfigurator setMaxScriptExecutionTime(long maxScriptExecutionTime) {
        this.maxScriptExecutionTime = maxScriptExecutionTime;
        return this;
    }

    public int getNrOfInstructionsBeforeStateCheckCallback() {
        return nrOfInstructionsBeforeStateCheckCallback;
    }

    public SecureScriptTaskActivityBehaviorConfigurator setNrOfInstructionsBeforeStateCheckCallback(int nrOfInstructionsBeforeStateCheckCallback) {
        this.nrOfInstructionsBeforeStateCheckCallback = nrOfInstructionsBeforeStateCheckCallback;
        return this;
    }

    public int getMaxStackDepth() {
        return maxStackDepth;
    }

    public SecureScriptTaskActivityBehaviorConfigurator setMaxStackDepth(int maxStackDepth) {
        this.maxStackDepth = maxStackDepth;
        return this;
    }

    public long getMaxMemoryUsed() {
        return maxMemoryUsed;
    }

    public SecureScriptTaskActivityBehaviorConfigurator setMaxMemoryUsed(long maxMemoryUsed) {
        this.maxMemoryUsed = maxMemoryUsed;
        return this;
    }

    public int getScriptOptimizationLevel() {
        return scriptOptimizationLevel;
    }

    public SecureScriptTaskActivityBehaviorConfigurator setScriptOptimizationLevel(int scriptOptimizationLevel) {
        this.scriptOptimizationLevel = scriptOptimizationLevel;
        return this;
    }
}
