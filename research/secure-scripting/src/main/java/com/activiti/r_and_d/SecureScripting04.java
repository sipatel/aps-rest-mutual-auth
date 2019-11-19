package com.activiti.r_and_d;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;

/**
 * @author Joram Barrez
 */
public class SecureScripting04 {

    public static void main(String[] args) {

        // Between this example and the previous example a lot of time was spent to make Nashorn
        // somehow intercept or cope with the infinite loop/memory usage. However, after extensive
        // searching and experimenting, it seems the features simple are not (yet?) in Nashorn.
        //
        // For example in JDK < 8, the Rhino javascript engine had the 'instructionCount' callback mechanism,
        // which is not present in Nashorn.
        //
        // We tried for example to mimic the instructionCount idea, by prettifying the script first
        // (cause people could write the whole script on one line) and then injecting a line of code that triggers
        // a callback. However, that was
        // 1) not quite straightforward to do
        // 2) one would still be able to write an instruction on one line that runs infinitely/uses a lot of memory
        //
        // Being stuck there, we looked at the Rhino engine from Mozilla. Since its inclusion in the JDK a long time
        // ago it actually evolved further, while the version in the JDK wasn't updated.
        //
        // After reading up the (sparse) Rhino docs, it became clear Rhino seemed to have a far richer
        // feature-set wrt our use case. So in this example we try the 'ClassShutter' of Rhino
        // which should be similar to the ClassFilter of Nashorn.
        //
        // It should fail with "org.mozilla.javascript.EcmaError: TypeError: Cannot call property println in object [JavaPackage java.lang.System.out]. It is not a function, it is "object"."

        ProcessEngine processEngine = createProcessEngine();
        processEngine.getRepositoryService().createDeployment().addClasspathResource("r_and_d/04.bpmn20.xml").deploy();
		processEngine.getRuntimeService().startProcessInstanceByKey("ScriptingTest04");
		System.out.println("SecureScripting04 DONE");

    }

    private static ProcessEngine createProcessEngine() {
        return ProcessEngineConfiguration.createStandaloneProcessEngineConfiguration()
                .setJdbcUrl("jdbc:h2:mem:activiti;DB_CLOSE_DELAY=1000")
                .setJdbcDriver("org.h2.Driver")
                .setJdbcUsername("sa")
                .setJdbcPassword("")
                .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE)
                .buildProcessEngine();
    }

}
