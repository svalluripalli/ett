package gov.nist.healthcare.ttt.webapp.common.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.codec.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger; 
import org.apache.logging.log4j.LogManager;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping("/api/ccdadocuments")
public class GetCCDADocumentsController {

	private static Logger logger = LogManager.getLogger(GetCCDADocumentsController.class.getName());

	@Value("${server.tomcat.basedir}")
	String ccdaFileDirectory;

	@Value("${github.test.data}")
	String githubTestData;

	@Value("${github.sha}")
	String githubSha;

	@Value("${github.tree}")
	String githubTree;

	@Value("${github.cures.test.data}")
	String githubCuresTestData;

	@Value("${github.cures.sha}")
	String githubCuresSha;

	@Value("${github.cures.tree}")
	String githubCuresTree;
	
	@Value("${github.svap.test.data}")
	String githubSvapTestData;

	@Value("${github.svap.sha}")
	String githubSvapSha;

	@Value("${github.svap.tree}")
	String githubSvapTree;
	
	public List<String> files2ignore = Arrays.asList("LICENSE", "README.md","README.MD");
	public List<String> extension2ignore = Arrays.asList("");
	public String extensionRegex = ".*\\.[a-zA-Z0-9]{3,4}$";

	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody HashMap<String, Object> getDocuments(@RequestParam(value = "testCaseType") String testCaseType) throws Exception {
		// Result map
		HashMap<String, Object> resultMap = new HashMap<>();
		try{
		// CCDA cache File path
		String ccdaFilePath = getFilterFiles(testCaseType);
		File ccdaObjectivesFile = new File(ccdaFilePath);

			String sha = getHTML(githubSha).getJSONObject("commit").get("sha").toString();
			JSONArray filesArray = getHTML(githubTree + sha + "?recursive=1").getJSONArray("tree");

			for(int i=0; i < filesArray.length(); i++) {
				JSONObject file = filesArray.getJSONObject(i);
				if(!files2ignore.contains(file.get("path"))) {
					// Get path array
					String[] path = file.get("path").toString().split("/");
					buildJson(resultMap, path,false,false);
				}

			}
			
			// cures files
			if (StringUtils.isNotBlank(githubCuresTestData) && githubCuresTestData.length() > 1){
				sha = getHTML(githubCuresSha).getJSONObject("commit").get("sha").toString();
				filesArray = getHTML(githubCuresTree + sha + "?recursive=1").getJSONArray("tree");
				
				for(int i=0; i < filesArray.length(); i++) {
					JSONObject file = filesArray.getJSONObject(i);
					if(!files2ignore.contains(file.get("path"))) {
						// Get path array
						String[] path = file.get("path").toString().split("/");
						buildJson(resultMap, path,true,false);
					}

				}				
			}
			
			// SVAP files
			if (StringUtils.isNotBlank(githubSvapTestData) && githubSvapTestData.length() > 1){
				sha = getHTML(githubSvapSha).getJSONObject("commit").get("sha").toString();
				filesArray = getHTML(githubSvapTree + sha + "?recursive=1").getJSONArray("tree");
				
				for(int i=0; i < filesArray.length(); i++) {
					JSONObject file = filesArray.getJSONObject(i);
					if(!files2ignore.contains(file.get("path"))) {
						// Get path array
						String[] path = file.get("path").toString().split("/");
						buildJson(resultMap, path,false,true);
					}

				}				
			}
			
			// Write the cache file
				JSONObject cacheFile = new JSONObject(resultMap);
				HashMap<String, Object> existingResultMap = new HashMap<>();
				// check exiting file before creating
				if(ccdaObjectivesFile.exists() && !ccdaObjectivesFile.isDirectory()) {
					JsonFactory factory = new JsonFactory();
					ObjectMapper mapper = new ObjectMapper(factory);
					TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {};
					existingResultMap = mapper.readValue(ccdaObjectivesFile, typeRef);
				}
				//Create Json object from the existing file
				JSONObject existingFile = new JSONObject(existingResultMap);
				
				//Create File if the content is different
				if (!cacheFile.toString().equals(existingFile.toString())) {
					logger.info("Creating CCDA OBJECTIVES file....");
					FileUtils.writeStringToFile(ccdaObjectivesFile, cacheFile.toString(2),Charsets.UTF_8);					
				}
				
			} catch(Exception e) {
				logger.error("Could not create ccda cache file: " + e.getMessage());
				e.printStackTrace();
			}
		return resultMap;
	}

	public void buildJson(HashMap<String, Object> json, String[] path, boolean curesFiles, boolean svapFiles) {
		if(path.length == 1 && !files2ignore.contains(path[0].toUpperCase())) {
			HashMap<String, Object> newObj = new HashMap<>();
			newObj.put("dirs", new ArrayList<HashMap<String, Object>>());
			newObj.put("files", new ArrayList<HashMap<String, Object>>());
			json.put(path[0], newObj);

		} else {
			HashMap<String, Object> current = (HashMap<String, Object>) json.get(path[0]);
			String fileName = path[path.length-1];
			String fileExtnAry[] = fileName.split("\\.");
			String fileExtn = "";
			if (fileExtnAry.length > 0){
				fileExtn = fileExtnAry[fileExtnAry.length-1];
		    }
			//create directory only when at least one valid file exist
			if(Pattern.matches(extensionRegex, fileName) && !files2ignore.contains(fileName) && !extension2ignore.contains(fileExtn) ) {
				for(int i = 1 ; i < path.length-1 ; i++) {
					String  currentName = path[i];
					boolean firstFile = false;
					//For the first filename the direcotry is not found
					if(containsName((List<Map>) current.get("dirs"), currentName)) {
						List<Map> directories = (List<Map>) current.get("dirs");
						current = (HashMap<String, Object>) directories.get(getObjByName(directories, currentName));
						HashMap<String, Object> newFile = new HashMap<>();
						newFile.put("name", fileName);
						newFile.put("link", getLink(path,curesFiles,svapFiles));
						newFile.put("cures",curesFiles);
						newFile.put("svap",svapFiles);
						List filesList = (List) current.get("files");
						filesList.add(newFile);
					} else {
						firstFile = true;
						HashMap<String, Object> newObj = new HashMap<>();
						newObj.put("name", currentName);
						newObj.put("dirs", new ArrayList<HashMap<String, Object>>());
						newObj.put("files", new ArrayList<HashMap<String, Object>>());
						List dirsList = (List) current.get("dirs");
						dirsList.add(newObj);
					}
					//For the first filename the when direcotry is not found and the files
					if(firstFile && containsName((List<Map>) current.get("dirs"), currentName)) {
						current = (HashMap<String, Object>) json.get(path[0]);
						List<Map> directories = (List<Map>) current.get("dirs");
						current = (HashMap<String, Object>) directories.get(getObjByName(directories, currentName));
						HashMap<String, Object> newFile = new HashMap<>();
						newFile.put("name", fileName);
						newFile.put("link", getLink(path,curesFiles,svapFiles));
						newFile.put("cures",curesFiles);
						newFile.put("svap",svapFiles);
						List filesList = (List) current.get("files");
						filesList.add(newFile);
					}
				} // end of For loop
			}
		}
	}

	public String getLink(String[] path,boolean curesFiles,boolean svapFiles) {
		String linkMaster = String.join("/", path).replace(" ", "%20");
		String link = githubTestData + linkMaster;
		//link = githubTestData"https://raw.githubusercontent.com/onc-healthit/2015-certification-ccda-testdata/master/" + link;
		if (curesFiles){
			link = githubCuresTestData + linkMaster;
		}

		if (svapFiles){
			link = githubSvapTestData + linkMaster;
		}
		
		return link;
	}

	public static boolean containsName(List<Map> json, String value) {
		for(Map obj : json) {
			if(obj.containsValue(value)) {
				return true;
			}
		}
		return false;
	}

	public static int getObjByName(List<Map> json, String value) {
		for(int i = 0 ; i < json.size() ; i++) {
			if(json.get(i).containsValue(value)) {
				return i;
			}
		}
		return -1;
	}

	public static JSONObject getHTML(String urlToRead) throws Exception {
		StringBuilder result = new StringBuilder();
		URL url = new URL(urlToRead);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line;
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		rd.close();
		return new JSONObject(result.toString());
	}

	private String getFilterFiles(String testCaseType){
		String fileName = ccdaFileDirectory + File.separator + "ccda_objectives.txt";
		extension2ignore = Arrays.asList("");
		if (testCaseType !=null && testCaseType.equalsIgnoreCase("xdr")){
			fileName = ccdaFileDirectory + File.separator + "ccda_objectives_xdr.txt";
			extension2ignore = Arrays.asList("ZIP","zip");
		}
		return fileName;
	}

}
