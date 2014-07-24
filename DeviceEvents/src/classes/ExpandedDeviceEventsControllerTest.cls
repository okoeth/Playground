/*=============================================================================
 |      Project:  Prototype for Compressing Device Events
 |       Author:  Oliver.Koeth@nttdata.com
 |
 |       Status:  Prototype
 |  Create Date:  24 July 2014
 |
 |    Component:  ExpandedDeviceEventsController
 |         Type:  Visualforce Controller Extension Test
 |
 |  Description:  Provides unit tests for the ExpandedDeviceEventsController.
 |                
 *===========================================================================*/
@isTest
private class ExpandedDeviceEventsControllerTest {
	static ExpandedDeviceEventsController controller;
	
	static ExpandedDeviceEventsController getController() {
		if (controller==null) {
			// TODO: Create transient test data
			Account testAccount = new Account();
			testAccount.Name='Test Account';
			insert testAccount;
    		System.assert(testAccount.Id!=null, 'Fail to instantiate test account');
    		Device_Event__c de1 = getDeviceEvent1();
    		de1.Device_Owner__c = testAccount.Id;
    		insert de1;    	
    		System.assert(de1.Id!=null, 'Fail to instantiate test event');
    		Device_Event__c de2 = getDeviceEvent2();
    		de2.Device_Owner__c = testAccount.Id;
    		insert de2;    	
    		System.assert(de2.Id!=null, 'Fail to instantiate test event');
			controller = new ExpandedDeviceEventsController (
				new ApexPages.StandardController(testAccount));
		}
		return controller;
	}
	
	static private Device_Event__c getDeviceEvent1() {
		Device_Event__c de1 = new Device_Event__c();
		de1.Is_Compressed__c = false;
		de1.Description__c = 'Hello';
		de1.Severity__c = 'INFO';
		return de1;		
	}

	static private Device_Event__c getDeviceEvent2() {
		Device_Event__c de2 = new Device_Event__c();
		de2.Is_Compressed__c = true;
		de2.Compressed_Device_Events__c = '[{"attributes":{"type":"Device_Event__c"},"Description__c":"Hello","Is_Compressed__c":false,"Severity__c":"INFO"},{"attributes":{"type":"Device_Event__c"},"Description__c":"World","Is_Compressed__c":false,"Severity__c":"INFO"}]';
		return de2;
	}
	
    private static TestMethod void testExpandDeviceEvents() {
    	System.assert(getController()!=null, 'Fail to instantiate test class');    	
		
	 List<Device_Event__c> de_simple_list = getController().testExpandDeviceEvent(getDeviceEvent1());
    	System.assert(de_simple_list.size()==1, 'Fail to expand device events');    	
		List<Device_Event__c> de_expanded_list = getController().testExpandDeviceEvent(getDeviceEvent2());
    	System.assert(de_expanded_list.size()==2, 'Fail to expand device events');    	
    }

    private static TestMethod void testGetExpandedDeviceEvents() {
    	System.assert(getController()!=null, 'Fail to instantiate test class');    	
		
		List<Device_Event__c> de_expanded_list = getController().testExpandDeviceEvents();
    	System.assert(de_expanded_list.size()==3, 'Fail to expand device events');    	
    }
    
    /*

[
{"attributes":{"type":"Device_Event__c"},"Name":"Z1.1","Description__c":"Container temprature raising","Is_Compressed__c":false,"Severity__c":"INFO"},
{"attributes":{"type":"Device_Event__c"},"Name":"Z1.2","Description__c":"Container temprature raising","Is_Compressed__c":false,"Severity__c":"INFO"},
{"attributes":{"type":"Device_Event__c"},"Name":"Z1.3","Description__c":"Container full","Is_Compressed__c":false,"Severity__c":"INFO"},
{"attributes":{"type":"Device_Event__c"},"Name":"Z1.4","Description__c":"Container refilled","Is_Compressed__c":false,"Severity__c":"INFO"}
]

    
    */
}