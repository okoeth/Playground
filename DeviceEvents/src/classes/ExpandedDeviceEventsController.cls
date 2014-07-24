/*=============================================================================
 |      Project:  Prototype for Compressing Device Events
 |       Author:  Oliver.Koeth@nttdata.com
 |
 |       Status:  Prototype
 |  Create Date:  24 July 2014
 |
 |    Component:  ExpandedDeviceEventsController
 |         Type:  Visualforce Controller Extension
 |
 |  Description:  Provide a list of device events for an Account.
 |                Device events which were compressed are expaned
 |                so that all events can be shown in a single table.
 |                
 *===========================================================================*/
 
public with sharing class ExpandedDeviceEventsController {
	private final ApexPages.StandardController standardController;
	
	public List<Device_Event__c> deviceEvents { get; set; }

	/*---------------------------------------------------------------------
     |  Constructor
     |
     |     Purpose:  Create controller extsion and establish link 
     |               with standard controller.
     |
     |  Parameters:  standardController -- of which this is an extension
	 |
     |     Returns:  N/A
     *-------------------------------------------------------------------*/	
	public ExpandedDeviceEventsController (ApexPages.StandardController standardController) {
		this.standardController = standardController;
		deviceEvents = expandDeviceEvents();	
	}

	/*---------------------------------------------------------------------
     |      Method:  expandedDeviceEvents
     |
     |     Purpose:  Takes a device event. If device event is compressed
     |               (i.e. is comprised of multiple events) then the
     |               events are expanded and returned. Otherwise only the 
     |               uncompressed event is returned.
     |
     |  Parameters:  Device event (compressed or not)
	 |
     |     Returns:  List of all device events in that event
     *-------------------------------------------------------------------*/
     private List<Device_Event__c> expandDeviceEvent (Device_Event__c deviceEvent) {
     	final List<Device_Event__c> allDeviceEvents = new List<Device_Event__c> ();
     	if (deviceEvent!=null) {
     		if (deviceEvent.Is_Compressed__c) {
     			final List<Device_Event__c> expandedDeviceEvents =
     				(List<Device_Event__c>) System.JSON.deserialize(
     					deviceEvent.Compressed_Device_Events__c, List<Device_Event__c>.class);
     			allDeviceEvents.addAll(expandedDeviceEvents); 
     		} else {
     			allDeviceEvents.add(deviceEvent);
     		}
     	}
     	return allDeviceEvents;
     }	

	/*---------------------------------------------------------------------
     |      Method:  getExpandedDeviceEvents
     |
     |     Purpose:  Retrieve all deeivce events in single list.
     |               Where device events are compressed, they get expanded
     |               again.
     |
     |  Parameters:  N/A
	 |
     |     Returns:  List of all device events
     *-------------------------------------------------------------------*/	
	private List<Device_Event__c> expandDeviceEvents () {
		if (standardController == null) {
			return null;
		}
		final Account account = (Account) standardController.getRecord();
		if (account == null) {
			return null;
		}
		final List<Device_Event__c> allDeviceEvents = new List<Device_Event__c>();
		final List<Device_Event__c> compressedDeviceEvents =  
			[
				SELECT Id, Name, Severity__c, Description__c, Is_Compressed__c, Compressed_Device_Events__c 
				FROM Device_Event__c 
			];	
		for (Device_Event__c deviceEvent : compressedDeviceEvents) {
			allDeviceEvents.addAll(expandDeviceEvent(deviceEvent));				
		}
		return allDeviceEvents;
	}
	
	/*---------------------------------------------------------------------
     |      Method:  testExpandedDeviceEvent
     |
     |     Purpose:  Wrapper for private method expandedDeviceEvents.
     |               Method can only be invoked from a running test. 
     |
     |  Parameters:  N/A
	 |
     |     Returns:  List of all device events
     *-------------------------------------------------------------------*/	
	public List<Device_Event__c> testExpandDeviceEvent (Device_Event__c deviceEvent) {
	    if (!test.isRunningTest()){
	        // TODO: Create Exception
	        // throw new Exception('This method can only be called from a test.');
	    } 
	    return expandDeviceEvent(deviceEvent); 
	}
		
	/*---------------------------------------------------------------------
     |      Method:  testExpandedDeviceEvents
     |
     |     Purpose:  Wrapper for private method expandedDeviceEvents.
     |               Method can only be invoked from a running test. 
     |
     |  Parameters:  N/A
	 |
     |     Returns:  List of all device events
     *-------------------------------------------------------------------*/	
	public List<Device_Event__c> testExpandDeviceEvents () {
	    if (!test.isRunningTest()){
	        // TODO: Create Exception
	        // throw new Exception('This method can only be called from a test.');
	    } 
	    return expandDeviceEvents(); 
	}	
}