package com.cmex.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.ibm.team.filesystem.client.internal.rest.util.LoginUtil.LoginHandler;
import com.ibm.team.process.client.IProcessClientService;
import com.ibm.team.process.client.IProcessItemService;
import com.ibm.team.process.common.IProjectArea;
import com.ibm.team.repository.client.ITeamRepository;
import com.ibm.team.repository.client.TeamPlatform;
import com.ibm.team.repository.common.IContributor;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.workitem.client.IWorkItemClient;



public class AssignHelper {
	
	static Properties prop;
	static RtcConfigValueHolder rtcConfigHolder;
	private static IProgressMonitor nullProgressMonitor = new NullProgressMonitor();
//	public static List<IContributor> CONTRIBUTER_LIST=new ArrayList<IContributor>();
	public  Map<String,List<IContributor>> ICONTRIBUTOR_FLAG_MAP = new HashMap<String,List<IContributor>>();
	
	
//static block to load properties
	static{
	     prop = new Properties();
		InputStream input = null;
		try{
			
	     input=AssignHelper.class.getClassLoader().getResourceAsStream("rtc_config.properties");
		//input = new FileInputStream("rtc_config.properties");
		// load a properties file
		prop.load(input);
		System.out.println(prop.getProperty("password"));
		}catch(Exception e){
		    System.out.println("Error in loading properties file,exiting");
		    System.exit(1);
		}
		finally{
			try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		List<String> emailIDList=null;
		//List<DataVO> csvDataList=null;
		if(args.length==0){
			System.out.println(" Arg length zero \n");
		    System.out.println("  Usage : java  [Path to csv file]");
		    return;
		}
		String inputCsvFile=args[0];
		//creating a new file instance
		if(!FilenameUtils.getExtension(inputCsvFile).equals("csv")){
		    System.out.println("Input file is not a csv file");
		    System.out.println("Process failed,exiting");
		    return;
		}
		try{
			//loading csv file values
			//emailIDList = loadEmailIdList(inputCsvFile);
			if(prop!=null){
			     rtcConfigHolder=new RtcConfigValueHolder();
			     rtcConfigHolder.setRtcUrl(prop.getProperty("rtc.url"));
			     rtcConfigHolder.setUserName(prop.getProperty("username"));
			     rtcConfigHolder.setPassword(prop.getProperty("password"));
			     rtcConfigHolder.setProjectArea(prop.getProperty("project.area"));
			     rtcConfigHolder.setProjectAreaFlag(prop.getProperty("project.area.flag"));
			     rtcConfigHolder.setCsvFile(new File(inputCsvFile));
				loginToRTCServer();
				try{
				List<DataVO> contributorInfoMap= new AssignHelper().loadCSVEmailIdList(inputCsvFile);
				ProcessUserAssignment userAssignment=new ProcessUserAssignment();
				userAssignment.assignUsers(rtcConfigHolder,contributorInfoMap);			
				System.out.println("=====Process completed===== ");
				}catch(FileNotFoundException fe){
					System.out.println("CSV NOT FOUND");
					fe.printStackTrace();	
				}catch(IOException ioe){
					System.out.println("IOException while processing CSV");
					ioe.printStackTrace();
				}catch(TeamRepositoryException teamRepoEx){
					System.out.println("TEam Repository exception while adding contributor");
					teamRepoEx.printStackTrace();
				}
			}
//			//populating  icontributer list
//			for(String emailId:emailIDList){
//				CONTRIBUTER_LIST.add(findContributorHandleByID(rtcConfigHolder,emailId));
//			}
			//assigning users to team area
			
		}

		catch(Exception e){
			System.out.println("Error in process "+e);
		}

	}

	
	


	private  List<DataVO> loadCSVEmailIdList(String inputCsvFile) throws TeamRepositoryException, IOException {
		List<IContributor> emailIDList=new ArrayList<IContributor>();
		List<DataVO> datavoList = new ArrayList<DataVO>();
		Map<String, List<IContributor>>  contributorFlagMap = new HashMap<String, List<IContributor>>();
		File inputCsv=new File(inputCsvFile);
		if(inputCsv!=null){
		    BufferedReader br=new BufferedReader(new FileReader(inputCsv));
		    String id;
		    String[] idFlag;
		    //for header
		    br.readLine();
		    while((id=br.readLine())!=null){
		    	id = id.replace("\"", "");
		    	idFlag = id.split(",");
		    	System.out.println("[load CSV values] "+id);
		    	DataVO datavo = new DataVO();
		    	datavo.setEmailId(idFlag[0]);
		    	datavo.setFlag(idFlag[1]);
		    	if(idFlag[2].contains("-")){
		    		datavo.setRoles(Arrays.asList((idFlag[2].split("-"))));
		    	}else{
		    		datavo.setRoles(Arrays.asList((idFlag[2])));
		    	}
		    	datavo.setContributor(findContributorHandleByID(rtcConfigHolder,idFlag[0].trim()));
		    	if(idFlag.length==4 && idFlag[3]!=null && !idFlag[3].trim().isEmpty()){
		    		if(idFlag[3].contains("-")){
		    			datavo.setTeamAreas((Arrays.asList((idFlag[3].split("-")))));
		    		}else{
		    			datavo.setTeamAreas((Arrays.asList((idFlag[3]))));
		    		}
		    	}
		    	datavoList.add(datavo);
		    }
		    	
		}
		return datavoList;
	}










	private static List<String> loadEmailIdList( String inputCsvFile)throws FileNotFoundException, IOException {
		List<String> emailIDList=new ArrayList<String>();
		File inputCsv=new File(inputCsvFile);
		if(inputCsv!=null){
			emailIDList=new ArrayList<String>();
		    BufferedReader br=new BufferedReader(new FileReader(inputCsv));
		    String id;
		    while((id=br.readLine())!=null){
		    	emailIDList.add(id.trim());
		    }
		    	
		}
		return emailIDList;
	}

	private static void loginToRTCServer() {
		  TeamPlatform.startup();


	        try {

	        	ITeamRepository rtcRepository = TeamPlatform.getTeamRepositoryService().getTeamRepository(rtcConfigHolder.getRtcUrl());

	        	rtcRepository.registerLoginHandler(new LoginHandler(rtcConfigHolder.getUserName(),rtcConfigHolder.getPassword()));

	            if (rtcRepository != null && !rtcRepository.loggedIn()) {

	               System.out.println("Logging in to RTC ");

	                rtcRepository.login(null);
	            }

	            IWorkItemClient  rtcWIClient = (IWorkItemClient)rtcRepository.getClientLibrary(IWorkItemClient.class);
	            rtcConfigHolder.setRtcWIClient(rtcWIClient);     
	            rtcConfigHolder.setPojectAreaInstance(findProjectArea(rtcRepository,rtcConfigHolder.getProjectArea(),nullProgressMonitor));
	            rtcConfigHolder.setRtcRepository(rtcRepository);
	            System.out.println("Logged In to RTC start up completed ");
	        } catch (TeamRepositoryException e) {
	            e.printStackTrace();
	        }
		
	}
	
	private static  IProjectArea  findProjectArea(ITeamRepository teamRepository, String projectName,	 
			IProgressMonitor monitor)throws TeamRepositoryException {

		IProcessItemService service = (IProcessItemService) teamRepository
				.getClientLibrary(IProcessItemService.class);
		IProjectArea area = null;
		List areas = service.findAllProjectAreas(
				IProcessClientService.ALL_PROPERTIES, monitor);
		for (Object anArea : areas) {
			if (anArea instanceof IProjectArea) {
				IProjectArea foundArea = (IProjectArea) anArea;
				if (foundArea.getName().equals(projectName)) {
					area = foundArea;
					System.out.println("Project found: " + projectName);				
					
					break;
				}
			}
		}
		return area;

	}

	
	 private static IContributor  findContributorHandleByID(RtcConfigValueHolder configValueHolder, String emailId) throws TeamRepositoryException{
	        return     configValueHolder.getRtcRepository().contributorManager().fetchContributorByUserId(emailId, null);
	    
	    }

}
