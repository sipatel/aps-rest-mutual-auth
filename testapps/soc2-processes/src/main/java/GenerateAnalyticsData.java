/**
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;


/**
 * Code snippets from http://www.codejava.net/java-se/networking/upload-files-by-sending-multipart-request-programmatically
 *  
 * @author jbarrez
 */
public class GenerateAnalyticsData {
	
	public static void main(String[] args) throws Exception {
	    
		System.out.println();
		System.out.println("This script will import the 'account opening' app, and generate demo analytics data for it");
		System.out.println("Optional argumenst : <nrOfEvents> <tenantId>");
		System.out.println();
		
		int nrOfEvents = 100;
		if (args.length >= 1) {
			nrOfEvents = Integer.valueOf(args[0]);
		}
		
		long tenantId = 1;
		if (args.length == 2) {
			tenantId = Long.valueOf(args[1]);
		}
		
		// Import the app definition model
		
		String modelJson = importApp();
		
		ScriptEngineManager factory = new ScriptEngineManager();
		ScriptEngine engine = factory.getEngineByName("JavaScript");
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("json", modelJson);
		engine.put("map", map);
		
		String script = "var model = JSON.parse(map.get('json')); map.put('id', model.id);";
		engine.eval(script);
		
		long modelId = ((Number) map.get("id")).longValue(); 
	    System.out.println("Model id = " + modelId);
		
	    
		// Publish the app
	    System.out.println("Publishing app");
		publishApp(modelId);
		
		
		// Deploy the app
		System.out.println("Deploying app");
		deployApp(modelId);
		
		// Generate some users
		System.out.println("Generating users");
		List<Long> userIds = new ArrayList<Long>();
		userIds.add(generateUser("Joram", "Barrez", tenantId));
		userIds.add(generateUser("Tijs", "Rademakers", tenantId));
		userIds.add(generateUser("Yvo", "Swillens", tenantId));
		userIds.add(generateUser("Erik", "Witloof", tenantId));
		userIds.add(generateUser("Lucian", "Tuca", tenantId));
		userIds.add(generateUser("Paul", "Holmens-Higgin", tenantId));
		
		// Generate data
		generateData(nrOfEvents, userIds);
		
		System.out.println("All done");
    }


	private static String importApp() throws Exception {
	    String modelJson = "";
		try {
			MultipartUtility multipart = new MultipartUtility("http://localhost:9999/activiti-app/api/enterprise/app-definitions/import", "UTF-8");
			multipart.addFilePart("file", new File("data/account_opening.zip"));

			List<String> response = multipart.finish();
			for (String line : response) {
				modelJson += line;
			}
		} catch (IOException ex) {
			System.err.println(ex);
		}
	    return modelJson;
    }
	
	private static void publishApp(long modelId) throws Exception  {
		URL url = new URL(String.format("http://localhost:9999/activiti-app/api/enterprise/app-definitions/" + modelId + "/publish"));
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		connection.setRequestProperty("Authorization", "Basic YWRtaW5AYXBwLmFjdGl2aXRpLmNvbTprMW5nazBuZw==");
		connection.setDoOutput(true);
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setRequestProperty("Accept", "application/json");
		connection.setRequestMethod("POST");
		
		String json = "{\"comment\" : \"Demo data\"}";
		byte[] outputBytes = json.getBytes("UTF-8");
		OutputStream os = connection.getOutputStream();
		os.write(outputBytes);
		os.close();
		
		connection.disconnect();
		
		System.out.println(connection.getResponseCode());
    }
	
	private static void deployApp(long modelId) throws Exception {
		URL url = new URL(String.format("http://localhost:9999/activiti-app/api/enterprise/runtime-app-definitions"));
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		connection.setRequestProperty("Authorization", "Basic YWRtaW5AYXBwLmFjdGl2aXRpLmNvbTprMW5nazBuZw==");
		connection.setDoOutput(true);
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setRequestProperty("Accept", "application/json");
		connection.setRequestMethod("POST");
		
		String json = "{\"appDefinitions\" : [ {\"id\": " + modelId + "} ] }";
		System.out.println(json);
		byte[] outputBytes = json.getBytes("UTF-8");
		OutputStream os = connection.getOutputStream();
		os.write(outputBytes);
		os.close();
		
		connection.disconnect();
		
		System.out.println(connection.getResponseCode());
    }
	
	private static long generateUser(String firstName, String lastName, long tenantId) throws Exception {
		URL url = new URL(String.format("http://localhost:9999/activiti-app/api/enterprise/admin/users"));
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		connection.setRequestProperty("Authorization", "Basic YWRtaW5AYXBwLmFjdGl2aXRpLmNvbTprMW5nazBuZw==");
		connection.setDoOutput(true);
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setRequestProperty("Accept", "application/json");
		connection.setRequestMethod("POST");
		
		String json = "{" +
				"\"email\" : \"" + MessageFormat.format("{0}@test.activiti.com", UUID.randomUUID().toString()) + "\"," +
				"\"firstName\" : \"" + firstName + "\"," +
				"\"lastName\" : \"" + lastName + "\"," +
				"\"password\" : \"password\"," +
				"\"tenantId\" : \"" + tenantId + "\"" +
				"}";
		
		byte[] outputBytes = json.getBytes("UTF-8");
		OutputStream os = connection.getOutputStream();
		os.write(outputBytes);
		os.close();
		
		int responseCode = connection.getResponseCode();
		if (responseCode != 200) {
			System.out.println("Error: got back response code " + responseCode);
		} else {
			System.out.println(responseCode);
		}
		
		StringBuilder stringBuilder = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String line = null;
		while ((line = reader.readLine()) != null) {
			stringBuilder.append(line);
		}
		reader.close();
		connection.disconnect();
		
		
		ScriptEngineManager factory = new ScriptEngineManager();
		ScriptEngine engine = factory.getEngineByName("JavaScript");
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("json", stringBuilder.toString());
		engine.put("map", map);
		
		String script = "var model = JSON.parse(map.get('json')); map.put('id', model.id);";
		engine.eval(script);
		
		long userId = ((Number) map.get("id")).longValue(); 
		return userId;
	}
	
	private static void generateData(int nrOfEvents, List<Long> userIds) throws Exception {
		
		Random random = new Random();
		for (int i=0; i<nrOfEvents; i++) {
			if (random.nextBoolean()) {
				
				System.out.println("Starting process instance");
				startProcessInstance("accountOpening", userIds.get(random.nextInt(userIds.size())));
				
			} else {
				
				System.out.println("Executing task");
				completeTasks("accountOpening", userIds.get(random.nextInt(userIds.size())));
				
			}
		}
	}
	
	private static void startProcessInstance(String processDefinitionKey, Long userId) throws Exception {
		
		URL url = new URL("http://localhost:9999/activiti-app/api/temporary/generate-report-data/start-process?userId="+userId+"&processDefinitionKey="+processDefinitionKey);
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		connection.setRequestProperty("Authorization", "Basic YWRtaW5AYXBwLmFjdGl2aXRpLmNvbTprMW5nazBuZw==");
//		connection.setRequestProperty("Accept", "application/json");
		connection.setRequestMethod("GET");
		
		int responseCode = connection.getResponseCode();
		if (responseCode != 200) {
			System.out.println("Error: got back response code " + responseCode);
		} else {
			System.out.println(responseCode);
		}
	}
	
	private static void completeTasks(String processDefinitionKey, Long userId) throws Exception {
		
		URL url = new URL("http://localhost:9999/activiti-app/api/temporary/generate-report-data/complete-tasks?userId="+userId+"&processDefinitionKey="+processDefinitionKey);
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		connection.setRequestProperty("Authorization", "Basic YWRtaW5AYXBwLmFjdGl2aXRpLmNvbTprMW5nazBuZw==");
//		connection.setRequestProperty("Accept", "application/json");
		connection.setRequestMethod("GET");
		
		int responseCode = connection.getResponseCode();
		if (responseCode != 200) {
			System.out.println("Error: got back response code " + responseCode);
		} else {
			System.out.println(responseCode);
		}
	}
	
	
	public static class MultipartUtility {
		
	    private final String boundary;
	    private static final String LINE_FEED = "\r\n";
	    private HttpURLConnection httpConn;
	    private String charset;
	    private OutputStream outputStream;
	    private PrintWriter writer;
	 
	    /**
	     * This constructor initializes a new HTTP POST request with content type
	     * is set to multipart/form-data
	     * @param requestURL
	     * @param charset
	     * @throws IOException
	     */
	    public MultipartUtility(String requestURL, String charset) throws IOException {
	        this.charset = charset;
	         
	        // creates a unique boundary based on time stamp
	        boundary = "===" + System.currentTimeMillis() + "===";
	         
	        URL url = new URL(requestURL);
	        httpConn = (HttpURLConnection) url.openConnection();
	        httpConn.setUseCaches(false);
	        httpConn.setDoOutput(true); // indicates POST method
	        httpConn.setDoInput(true);
	        httpConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
	        httpConn.setRequestProperty("Authorization", "Basic YWRtaW5AYXBwLmFjdGl2aXRpLmNvbTprMW5nazBuZw==");
	        httpConn.setRequestProperty("Accept", "application/json");
	        outputStream = httpConn.getOutputStream();
	        writer = new PrintWriter(new OutputStreamWriter(outputStream, charset), true);
	    }
	 
	    /**
	     * Adds a form field to the request
	     * @param name field name
	     * @param value field value
	     */
	    public void addFormField(String name, String value) {
	        writer.append("--" + boundary).append(LINE_FEED);
	        writer.append("Content-Disposition: form-data; name=\"" + name + "\"").append(LINE_FEED);
	        writer.append("Content-Type: text/plain; charset=" + charset).append(LINE_FEED);
	        writer.append(LINE_FEED);
	        writer.append(value).append(LINE_FEED);
	        writer.flush();
	    }
	 
	    /**
	     * Adds a upload file section to the request
	     * @param fieldName name attribute in <input type="file" name="..." />
	     * @param uploadFile a File to be uploaded
	     * @throws IOException
	     */
	    public void addFilePart(String fieldName, File uploadFile) throws IOException {
	        String fileName = uploadFile.getName();
	        writer.append("--" + boundary).append(LINE_FEED);
	        writer.append("Content-Disposition: form-data; name=\"" + fieldName
	                        + "\"; filename=\"" + fileName + "\"").append(LINE_FEED);
	        writer.append(
	                "Content-Type: " + URLConnection.guessContentTypeFromName(fileName))
	                .append(LINE_FEED);
	        writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
	        writer.append(LINE_FEED);
	        writer.flush();
	 
	        FileInputStream inputStream = new FileInputStream(uploadFile);
	        byte[] buffer = new byte[4096];
	        int bytesRead = -1;
	        while ((bytesRead = inputStream.read(buffer)) != -1) {
	            outputStream.write(buffer, 0, bytesRead);
	        }
	        outputStream.flush();
	        inputStream.close();
	         
	        writer.append(LINE_FEED);
	        writer.flush();    
	    }
	 
	    /**
	     * Adds a header field to the request.
	     * @param name - name of the header field
	     * @param value - value of the header field
	     */
	    public void addHeaderField(String name, String value) {
	        writer.append(name + ": " + value).append(LINE_FEED);
	        writer.flush();
	    }
	     
	    /**
	     * Completes the request and receives response from the server.
	     * @return a list of Strings as response in case the server returned
	     * status OK, otherwise an exception is thrown.
	     * @throws IOException
	     */
	    public List<String> finish() throws IOException {
	        List<String> response = new ArrayList<String>();
	 
	        writer.append(LINE_FEED).flush();
	        writer.append("--" + boundary + "--").append(LINE_FEED);
	        writer.close();
	 
	        // checks server's status code first
	        int status = httpConn.getResponseCode();
	        if (status == HttpURLConnection.HTTP_OK) {
	            BufferedReader reader = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
	            String line = null;
	            while ((line = reader.readLine()) != null) {
	                response.add(line);
	            }
	            reader.close();
	            httpConn.disconnect();
	        } else {
	            throw new IOException("Server returned non-OK status: " + status);
	        }
	 
	        return response;
	    }
	}

}
