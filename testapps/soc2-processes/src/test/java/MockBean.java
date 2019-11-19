import org.activiti.engine.delegate.DelegateExecution;


public class MockBean {
	
	public void generateProcessInstancePdf(DelegateExecution execution, String s, boolean b, String s2) {
		System.out.println("Generating pdf...");
	}

}
