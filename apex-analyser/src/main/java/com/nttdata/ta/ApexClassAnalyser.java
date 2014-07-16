package com.nttdata.ta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.nttdata.sf.tooling.ApexClass;
import com.nttdata.sf.tooling.ApexClassMember;
import com.nttdata.sf.tooling.ApexTrigger;
import com.nttdata.sf.tooling.ApexTriggerMember;
import com.nttdata.sf.tooling.ContainerAsyncRequest;
import com.nttdata.sf.tooling.DeleteResult;
import com.nttdata.sf.tooling.Error;
import com.nttdata.sf.tooling.ExternalMethod;
import com.nttdata.sf.tooling.ExternalReference;
import com.nttdata.sf.tooling.MetadataContainer;
import com.nttdata.sf.tooling.Method;
import com.nttdata.sf.tooling.ObjectFactory;
import com.nttdata.sf.tooling.Position;
import com.nttdata.sf.tooling.SObject;
import com.nttdata.sf.tooling.SaveResult;
import com.nttdata.sf.tooling.SymbolTable;

public class ApexClassAnalyser {
	final private static String METADATA_CONTAINER_NAME="ApexClassAnalyser";
	final private static String BASE_OUTPUT_DIR="/Users/oliverkoeth/temp/apex-analyser/";
		
	private ObjectFactory objectFactory = new ObjectFactory();
	private String metadataContainerId;
	private ArcHandler arcHandler = new ArcHandler();
	private LinkedList<String> newDependencies = null;
	private LinkedList<String> allDependencies = null;
	private int numOfCompiles=0;
	
	private List<SaveResult> createObject (SObject sobject) {
		List<SObject> list = new ArrayList<SObject> ();
		list.add(sobject);	
		List<SaveResult> results = ToolingDriver.getPort().create(list);
		for (SaveResult result : results) {
			for (Error error : result.getErrors()) {
				System.out.println("ERROR: "+error.getMessage());
			}
		}
		return results;
	}

	public void deleteMetadataContainer () {
		System.out.println("INFO: Delete Metadata Container "+METADATA_CONTAINER_NAME);
		MetadataContainer[] containers =
				ToolingDriver.getPort().query("select Id, Name from MetadataContainer where Name = '"+METADATA_CONTAINER_NAME+"'")
		        .getRecords().toArray(new MetadataContainer[0]);    		
		System.out.println("INFO: Delete Metadata Container "+containers.length);
		if(containers.length>0) {
			System.out.println("INFO: Delete Metadata Container "+containers[0].getId());
			List<DeleteResult> results = ToolingDriver.getPort().delete(Arrays.asList(containers[0].getId()));
			for (DeleteResult result : results) {
				for (Error error : result.getErrors()) {
					System.out.println("ERROR: "+error.getMessage());
				}
			}
		}
	}
	
	private String createMetadataContainer () {
		System.out.println("INFO: Create Metadata Container "+METADATA_CONTAINER_NAME);
		MetadataContainer container = new MetadataContainer();
		container.setName(objectFactory.createMetadataContainerName(METADATA_CONTAINER_NAME));
		
		List<SaveResult> saveResult = createObject(container);
		String metadataContainerId = saveResult.get(0).getId();
		System.out.println("INFO: Metadata Container created "+metadataContainerId);
		
		return metadataContainerId;
	}
	
	public String getMetadataContainerId() {
		System.out.println("INFO: Metadata Container Id from Name "+METADATA_CONTAINER_NAME);
		if (metadataContainerId != null) {
			return metadataContainerId;
		}
		
		MetadataContainer[] metadataContainers =
				ToolingDriver.getPort().query("select Id, Name from MetadataContainer where Name = '"+METADATA_CONTAINER_NAME+"'")
			        .getRecords().toArray(new MetadataContainer[0]);    		
		
		if (metadataContainers.length == 0) {
			metadataContainerId = createMetadataContainer();
		}
		else {
			metadataContainerId = metadataContainers[0].getId();
		}
		
		return metadataContainerId;
	}
	
	public String loadApexClass(String className) {
		System.out.println("INFO: Load Apex Class "+className);
		ApexClass apexClass =
				ToolingDriver.getPort().query("select Id, Name, FullName, Body from ApexClass where NamespacePrefix = null and Name = '"+className+"'")
			        .getRecords().toArray(new ApexClass[0]) [0];    		
		ApexClassMember apexClassMember = new ApexClassMember();
		apexClassMember.setBody(
				objectFactory.createApexClassMemberBody(apexClass.getBody().getValue()));
		apexClassMember.setContentEntityId(
				objectFactory.createApexClassMemberContentEntityId(apexClass.getId()));
		apexClassMember.setMetadataContainerId(
				objectFactory.createApexClassMemberMetadataContainerId(
						getMetadataContainerId()));
		
		List<SaveResult> saveResult = createObject(apexClassMember);
		return saveResult.get(0).getId();
	}

	public String loadApexTrigger(String triggerName) {
		System.out.println("INFO: Load Apex Trigger "+triggerName);
		ApexTrigger apexTrigger =
				ToolingDriver.getPort().query("select Id, Name, Body from ApexTrigger where NamespacePrefix = null and Name = '"+triggerName+"'")
			        .getRecords().toArray(new ApexTrigger[0]) [0];    		
		ApexTriggerMember apexTriggerMember = new ApexTriggerMember();
		apexTriggerMember.setBody(
				objectFactory.createApexTriggerMemberBody(apexTrigger.getBody().getValue()));
		apexTriggerMember.setContentEntityId(
				objectFactory.createApexTriggerMemberContentEntityId(apexTrigger.getId()));
		apexTriggerMember.setMetadataContainerId(
				objectFactory.createApexTriggerMemberMetadataContainerId(
						getMetadataContainerId()));
		
		List<SaveResult> saveResult = createObject(apexTriggerMember);
		return saveResult.get(0).getId();
	}
	
//	// Use: 1drN0000000F7aVIAS
//	public void checkMetadataContainer () throws Exception {
//		ContainerAsyncRequest result =
//				ToolingDriver.getPort().query("SELECT Id, State, CompilerErrors, ErrorMsg FROM ContainerAsyncRequest where id = '1drN0000000F7aVIAS'")
//        				.getRecords().toArray(new ContainerAsyncRequest[0]) [0];    		
//		
//		String state = result.getState().getValue();
//		System.out.println(state);
//	}	
	
	public void compileMetadataContainer () throws Exception {		
		System.out.println("INFO: Compile Metadata Container");
		ContainerAsyncRequest request = new ContainerAsyncRequest();		
        request.setIsCheckOnly(
        		objectFactory.createContainerAsyncRequestIsCheckOnly(true));
        request.setMetadataContainerId(
				objectFactory.createContainerAsyncRequestMetadataContainerId(
						getMetadataContainerId()));
        
		List<SaveResult> saveResult = createObject(request);
		String requestId = saveResult.get(0).getId();
		System.out.println("INFO: New ContainerAsyncRequestId:"+requestId);
		numOfCompiles++;
		
		while (true) {
			ContainerAsyncRequest results =
					ToolingDriver.getPort().query("SELECT Id, State, CompilerErrors, ErrorMsg FROM ContainerAsyncRequest where id = '" + requestId + "'")
	        				.getRecords().toArray(new ContainerAsyncRequest[0]) [0];    		
			
			String state = results.getState().getValue();
			System.out.println("INFO: "+state);
			
			if ("Queued".equals(state)) {
				Thread.sleep(2000);
				continue;
			} else {
				if ("Failed".equals(state)) {
					System.out.println("ERROR: "+results.getErrorMsg().getValue());
					throw new Exception (results.getErrorMsg().getValue());
				}
				break;				
			}			
		}
	}
	
	public String apexClassMemberIdFromName(String name) throws Exception {
		System.out.println("INFO: Apex Class Member Id from Name "+name);
		ApexClass[] apexClasses =
				ToolingDriver.getPort().query("select Id, Name from ApexClass where Name = '"+name+"'")
			        .getRecords().toArray(new ApexClass[0]);    		
		
		if (apexClasses.length != 1) {
			throw new Exception("ApexClass name could not be resolved: "+name);
		}
		
		System.out.println("INFO: Using ApexClassId "+apexClasses[0].getId());
		ApexClassMember[] apexClassMembers =
				ToolingDriver.getPort().query("select Id, ContentEntityId, SymbolTable from ApexClassMember where ContentEntityId = '"+apexClasses[0].getId()+"'")
			        .getRecords().toArray(new ApexClassMember[0]);
		
		String apexClassMemberId = null;
		for (ApexClassMember apexClassMember : apexClassMembers) {
			if (apexClassMember.getSymbolTable()!=null) {
				apexClassMemberId = apexClassMember.getId();
			}
		}
		
		return apexClassMemberId;
	}

	public String apexTriggerMemberIdFromName(String name) throws Exception {
		System.out.println("INFO: Apex Trigger Member Id from Name"+name);
		ApexTrigger[] apexTriggers =
				ToolingDriver.getPort().query("select Id, Name from ApexTrigger where Name = '"+name+"'")
			        .getRecords().toArray(new ApexTrigger[0]);    		
		
		if (apexTriggers.length != 1) {
			throw new Exception("ApexTrigger name could not be resolved: "+name);
		}
		
		System.out.println("INFO: Using ApexTriggerId "+apexTriggers[0].getId());
		ApexTriggerMember[] apexTriggerMembers =
				ToolingDriver.getPort().query("select Id, ContentEntityId, SymbolTable from ApexTriggerMember where ContentEntityId = '"+apexTriggers[0].getId()+"'")
			        .getRecords().toArray(new ApexTriggerMember[0]);
		
		String apexTriggerMemberId = null;
		for (ApexTriggerMember apexTriggerMember : apexTriggerMembers) {
			if (apexTriggerMember.getSymbolTable()!=null) {
				apexTriggerMemberId = apexTriggerMember.getId();
			}
		}
		
		return apexTriggerMemberId;
	}
	
	public void analyseApexClassMember(String id) {
		System.out.println("INFO: Analyse Apex Class Member "+id);
		ApexClassMember apexClassMember =
				ToolingDriver.getPort().query("select Id, SymbolTable from ApexClassMember where Id = '"+id+"'")
			        .getRecords().toArray(new ApexClassMember[0])[0];    		
					
		SymbolTable symbolTable = apexClassMember.getSymbolTable().getValue();
		analyseApexMember(symbolTable);
	}

	// Selection done by naming convention only
	public void analyseApexControllers() throws Exception {
		System.out.println("INFO: Analyse all Apex Controllers");
		ApexClass[] apexClasses =
				ToolingDriver.getPort().query("select Id, Name from ApexClass where NamespacePrefix = null and Name like '%Controller' order by Name")
			        .getRecords().toArray(new ApexClass[0]);    		

		for (ApexClass apexClass : apexClasses) {
			analyseApexClass(apexClass.getName().getValue());
		}
	}
	
	public void analyseApexClass(String className) throws Exception {
		System.out.println("INFO: Analyse Apex Class "+className);
		arcHandler.initArcs();
		initDependencies();
		String apexClassMemberId = apexClassMemberIdFromName(className);
		if (apexClassMemberId == null) {
			loadApexClass(className);
			compileMetadataContainer();
			apexClassMemberId = apexClassMemberIdFromName(className);
		}
		analyseApexClassMember(apexClassMemberId);
		while(!newDependencies.isEmpty()) {
			String apexClassName = newDependencies.remove();
			try {
				apexClassMemberId = apexClassMemberIdFromName(apexClassName);
				if (apexClassMemberId == null) {
					loadApexClass(apexClassName);
					compileMetadataContainer();
					apexClassMemberId = apexClassMemberIdFromName(apexClassName);
				}
				analyseApexClassMember(apexClassMemberId);
			} catch (Exception e) {
				System.out.println("WARNING: Skipping Apex Class "+apexClassName);
			}
		}
		printDependencies();
		arcHandler.printArcsAsTree(BASE_OUTPUT_DIR+className, className+"_"+className);
	}

	public void analyseApexTriggers() throws Exception {
		System.out.println("INFO: Analyse all Apex Triggers");
		ApexTrigger[] apexTriggers =
				ToolingDriver.getPort().query("select Id, Name from ApexTrigger where NamespacePrefix = null order by Name")
			        .getRecords().toArray(new ApexTrigger[0]);    		

		for (ApexTrigger apexTrigger : apexTriggers) {
			analyseApexTrigger(apexTrigger.getName().getValue());
		}
	}
	
	public void analyseApexTrigger(String triggerName) throws Exception {
		System.out.println("INFO: Analyse Apex Trigger "+triggerName);
		arcHandler.initArcs();
		initDependencies();
		String apexTriggerMemberId = apexTriggerMemberIdFromName(triggerName);
		if (apexTriggerMemberId == null) {
			loadApexTrigger(triggerName);
			compileMetadataContainer();
			apexTriggerMemberId = apexTriggerMemberIdFromName(triggerName);
		}
		analyseApexTriggerMember(apexTriggerMemberId);
		while(!newDependencies.isEmpty()) {
			String apexClassName = newDependencies.remove();
			try {
				String apexClassMemberId = apexClassMemberIdFromName(apexClassName);
				if (apexClassMemberId == null) {
					loadApexClass(apexClassName);
					compileMetadataContainer();
					apexClassMemberId = apexClassMemberIdFromName(apexClassName);
				}
				analyseApexClassMember(apexClassMemberId);
			} catch (Exception e) {
				System.out.println("WARNING: Skipping Apex Class "+apexClassName);
			}
		}
		printDependencies();
		arcHandler.printArcsAsTree(BASE_OUTPUT_DIR+triggerName, triggerName+"_"+triggerName);
	}
	
	public void analyseApexTriggerMember(String id) {
		System.out.println("INFO: Analyse Apex Trigger Member "+id);
		ApexTriggerMember apexTriggerMember =
				ToolingDriver.getPort().query("select Id, SymbolTable from ApexTriggerMember where Id = '"+id+"'")
			        .getRecords().toArray(new ApexTriggerMember[0])[0];    		
					
		SymbolTable symbolTable = apexTriggerMember.getSymbolTable().getValue();
		analyseApexMember(symbolTable);
	}
	
	private void analyseApexMember(SymbolTable symbolTable) {
		System.out.println("INFO: Analyse Apex Member "+symbolTable.getName());
		List<ArcHandler.MethodPosition> items = new ArrayList<ArcHandler.MethodPosition>();
		items.add(
				new ArcHandler.MethodPosition(
						0, 
						symbolTable.getName()+"_"+symbolTable.getName(),
						true));
		
		// External References			
		for (ExternalReference externalReference : symbolTable.getExternalReferences()) {
			if (! allDependencies.contains(externalReference.getName())) {
				newDependencies.add(externalReference.getName());
				allDependencies.add(externalReference.getName());
			}
			for (ExternalMethod externalMethod : externalReference.getMethods()) {
				for (Position position : externalMethod.getReferences()) {
					items.add(
							new ArcHandler.MethodPosition(
									position.getLine(), 
									externalReference.getName()+"_"+externalMethod.getName(),
									false));
				}
			}
		}
		
		// Methods
		for (Method method : symbolTable.getMethods()) {
			items.add(
					new ArcHandler.MethodPosition(
							method.getLocation().getLine(), 
							symbolTable.getName()+"_"+method.getName(),
							true));
			for (Position position : method.getReferences()) {
				items.add(
						new ArcHandler.MethodPosition(
								position.getLine(), 
								symbolTable.getName()+"_"+method.getName(),
								false));
			}
		}
		
		// Process results
		arcHandler.addArcs(items);
	}
	
	public void initDependencies() {
		newDependencies = new LinkedList<String>();
		allDependencies = new LinkedList<String>();
	}
	
	public void printDependencies() {
		System.out.println("Dependencies {");
		for (String dependency : allDependencies) {
			System.out.println(dependency+";");
		}
		System.out.println("}");
	}
	
	public int getNumOfCompiles() {		
		return numOfCompiles;
	}
}