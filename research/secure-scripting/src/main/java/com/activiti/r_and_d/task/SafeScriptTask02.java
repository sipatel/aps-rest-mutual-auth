package com.activiti.r_and_d.task;
import javax.script.Bindings;
import javax.script.ScriptEngine;

import jdk.nashorn.api.scripting.ClassFilter;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.scripting.ScriptingEngines;


public class SafeScriptTask02 implements JavaDelegate {
	
	private Expression script;
	
	@Override
	public void execute(DelegateExecution execution) throws Exception {
		NashornScriptEngineFactory factory = new NashornScriptEngineFactory();
		ScriptEngine scriptEngine = factory.getScriptEngine(new SafeClassFilter());
		
		 ScriptingEngines scriptingEngines = Context
		      .getProcessEngineConfiguration()
		      .getScriptingEngines();

		Bindings bindings = scriptingEngines.getScriptBindingsFactory().createBindings(execution, false); // false -> we don't want to store any variable automatically
		scriptEngine.eval((String) script.getValue(execution), bindings);
		
		 System.out.println("Java delegate done");
	}
	
	public static class SafeClassFilter implements ClassFilter {
		
		@Override
		public boolean exposeToScripts(String s) {
      return false;
		}
		
	}
	
}
