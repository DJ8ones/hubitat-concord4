/**
 *  Concord 4 Device Handler
 *  
 *  Daniel Jones 2/11/2022
 *  Based off the SmartThings driver by Scott Dozier 4/1/2016
 */

metadata {
	// Automatically generated. Make future change here.
	definition (name: "Concord4", author: "djones", namespace: "djones") {
        capability "Polling"
        capability "Refresh"
        capability "SecurityKeypad" 
        command "childSwitchOn", ["num"]
        command "childSwitchOff", ["num"]    
        command "componentOn"
        command "componentOff"
        command "maintainChildZoneDevices"
        command "initialize"
        
        /////////Transmit commands -> These send something to Concord Server
        // Note that disarm, armHome, and armAway are not called out here because they are included as part of the SecurityKeypad capability
        command "sendConcordConfig"
               
        /////////Receive commands -> These are commands that can be issued from Concord Server via Maker API
        command "statusdisarmed"
        command "statusarmedaway"
        command "statusarmedstay"
        command "statusConfiguredConcord"
        command "statusHelpConcord"
        command "statusUndefined"
        
        /////////Pushbutton command to allow alarm mode changes
        //command "push"
        
        command "setReadyToArm", ["bool"]
        command "setAppEnabled"

	}

	simulator {
		// not used
	}
    preferences {
        input("concord_server_ip_address", "text", title: "IP", description: "Concord 4 Server IP Address",defaultValue: "8.8.8.8")
        input("concord_server_port", "number", title: "Port", description: "Concord 4 Server Port Number (8066)",defaultValue: 8066)
        input("concord_server_api_password", "text", title: "API Password", description: "Concord 4 Server API password (case sensitive",defaultValue: "")
        
        input("maker_api_app_id", "text", title: "Maker API App ID", description: "See Maker API ",defaultValue: "")
        input("maker_api_app_token", "text", title: "Maker API App Token", description: "See Maker API",defaultValue: "")
        
        input("zonename1", "text", title:"Zone 1 Name", description: "(leave blank to remove)", defaultValue: "")
        input("zonename2", "text", title:"Zone 2 Name", description: "(leave blank to remove)", defaultValue: "")
        input("zonename3", "text", title:"Zone 3 Name", description: "(leave blank to remove)", defaultValue: "")
        input("zonename4", "text", title:"Zone 4 Name", description: "(leave blank to remove)", defaultValue: "")
        input("zonename5", "text", title:"Zone 5 Name", description: "(leave blank to remove)", defaultValue: "")
        input("zonename6", "text", title:"Zone 6 Name", description: "(leave blank to remove)", defaultValue: "")
 		input("zonename7", "text", title:"Zone 7 Name", description: "(leave blank to remove)", defaultValue: "")
        input("zonename8", "text", title:"Zone 8 Name", description: "(leave blank to remove)", defaultValue: "")
        input("zonename9", "text", title:"Zone 9 Name", description: "(leave blank to remove)", defaultValue: "")
        input("zonename10", "text", title:"Zone 10 Name", description: "(leave blank to remove)", defaultValue: "")
        input("zonename11", "text", title:"Zone 11 Name", description: "(leave blank to remove)", defaultValue: "")
        input("zonename12", "text", title:"Zone 12 Name", description: "(leave blank to remove)", defaultValue: "")
        input("zonename13", "text", title:"Zone 13 Name", description: "(leave blank to remove)", defaultValue: "")
        input("zonename14", "text", title:"Zone 14 Name", description: "(leave blank to remove)", defaultValue: "")
        input("zonename15", "text", title:"Zone 15 Name", description: "(leave blank to remove)", defaultValue: "")
    }
}

// parse events into attributes
def parse(String description) {
 
}

def installed()
{
	state.bLoud = "False"
    state.concordConfigured = "False"
    state.appEnabled = "False"
    state.readyToArm = "False"
    sendEvent(name: "securityKeypad", value: "unknown")
    sendEvent(name: "switch", value: "off")
    poll()
}
    
def updated()
{
    maintainChildZoneDevices()
    runEvery5Minutes(poll)
    sendConcordConfig()
}


// handle commands
def poll()
{
    log.debug "polling"        
    if (state.concordConfigured == "False"){
        sendConcordConfig()
    }
}

def refresh(){
    return request('/refresh')   
}

////// Helper function used by updated()
def maintainChildZoneDevices()
{
    def zonename = ["","","","","","","","","","","","","","",""]
    
    // There has to be a more elegant way to do this, but for the life of me I couldn't figure it out. As zones are capped at 15, this works.
    zonename[0] = zonename1
    zonename[1] = zonename2
    zonename[2] = zonename3
    zonename[3] = zonename4
    zonename[4] = zonename5
    zonename[5] = zonename6
    zonename[6] = zonename7
    zonename[7] = zonename8
    zonename[8] = zonename9
    zonename[9] = zonename10
    zonename[10] = zonename11
    zonename[11] = zonename12
    zonename[12] = zonename13
    zonename[13] = zonename14
    zonename[14] = zonename15
    
    log.debug "Creating Zone Contact children"
 
    // Create Zone Contact Sensors if they don't exist
    for (i in 1..15) {
        log.debug "${zonename[i-1]}"
        	if ("${zonename[i-1]}" != "null") {
            	log.debug "Text read for Zone ${i}. Creating a child for it if not already there."
                def currentchild = getChildDevices()?.find { it.deviceNetworkId == "${device.deviceNetworkId}-zone${i}"}
                if (currentchild == null) {
                    addChildDevice("hubitat", "Virtual Contact Sensor", "${device.deviceNetworkId}-zone${i}", [name: "${zonename[i-1]}", isComponent: false])
                    log.debug "No device found. Creating Zone ${i} contact."
                }
                else {
                    log.debug "Zone ${i} contact already exists."
                }
            }
            else {
                log.debug "Text read for Zone ${i}. Delete child if it is no longer in preferences."
                
                def currentchilddel = getChildDevices()?.find { it.deviceNetworkId == "${device.deviceNetworkId}-zone${i}"}
                if (currentchilddel != null) {
                    deleteChildDevice("${device.deviceNetworkId}-zone${i}")
                    log.debug "Zone ${i} preference is blank. Deleting child contact sensor."
                }
                else {
                    log.debug "Null child with no preference. No action."
                }
            }
    }
    
    log.debug "Creating Switch children"
    /// Create silence switch
    def silenceSwitch = getChildDevice("${device.deviceNetworkId}-Switch1")
    def disarmSwitch = getChildDevice("${device.deviceNetworkId}-Switch2")
    def armHomeSwitch = getChildDevice("${device.deviceNetworkId}-Switch3")
    def armAwaySwitch = getChildDevice("${device.deviceNetworkId}-Switch4")
    
    if (silenceSwitch == null) {
        addChildDevice("hubitat", "Generic Component Switch", "${device.deviceNetworkId}-Switch1", [name: "Silence", isComponent: false])
        log.debug "Creating Silence Switch"
    }
    if (disarmSwitch == null) {
        addChildDevice("hubitat", "Generic Component Switch", "${device.deviceNetworkId}-Switch2", [name: "Disarm", isComponent: false])
        log.debug "Creating Disarm Switch"
    }
    if (armHomeSwitch == null) {
        addChildDevice("hubitat", "Generic Component Switch", "${device.deviceNetworkId}-Switch3", [name: "Arm Home", isComponent: false])
        log.debug "Creating Arm Home Switch"
    }
    if (armAwaySwitch == null) {
        addChildDevice("hubitat", "Generic Component Switch", "${device.deviceNetworkId}-Switch4", [name: "Arm Away", isComponent: false])
        log.debug "Creating Arm Away Switch"
    }
        
    String thisId = device.id
    deleteChildDevice("${thisId}-Switch")      

}

////////////// Receive commands (from Concord 4 to Hub through Maker API)
def statusdisarmed(){
    sendEvent(name: "securityKeypad", value: "disarmed")
    sendLocationEvent (name: "hsmSetArm", value: disarm)
    sendEvent(name: "Disarm", value: "on", isStateChange: true)
    log.info "Concord 4 indicated panel is in Disarmed status"

}

def statusarmedaway(){
    sendEvent(name: "securityKeypad", value: "armed away")
    sendLocationEvent (name: "hsmSetArm", value: armAway)
    childSwitchOn(4)
    log.info "Concord 4 indicated panel is in Armed Away status"
    
}

def statusarmedstay(){
    sendEvent(name: "securityKeypad", value: "armed home")
    sendLocationEvent (name: "hsmSetArm", value: armHome)
    sendEvent(name: "Arm Stay", value: "on", isStateChange: true)
    log.info "Concord 4 indicated panel is in Armed Stay status"
   
}

def statusUndefined(){
    sendEvent(name: "securityKeypad", value: "unknown")
    state.concordConfigured = "False"
    sendConcordConfig()
    log.info "Concord 4 Server started and does not know alarm state yet"
}

def statusHelpConcord(){ //If the concord loses config (e.g., due to power outage), it will need to send a re-initialization command
    state.concordConfigured = "False"  
    log.debug "Concord 4 configuration needed. Initiating sendConcordConfig."
    sendConcordConfig()
}

def statusConfiguredConcord()
{
     state.concordConfigured = "True"  
     log.debug "Concord 4 acknowledged configuration"
}

////////////// Transmit commands (from Hub to Concord 4)
def armHome() {
    log.debug "Executing 'ArmStay'"
    if( state.bLoud == "False" )
    {
        return request('/arm/stay')
    }
    else 
    {
        return request('/arm/stay/loud')
    }
  
}

def armAway() {
    log.debug "Executing 'ArmAway'"
    if( state.bLoud == "False" )
    {
        return request('/arm/away')
    }
    else 
    {
        return request('/arm/away/loud')
    }
}

def disarm() {
    log.debug "Executing 'Disarm'"
    if( state.bLoud == "False" )
    {
        return request('/disarm')
    }
    else
    {
        return request('/disarm/loud')
    }
}
   
def sendConcordConfig(){
    log.debug("Request: /config (with data)")
	def userpassascii = "admin:${concord_server_api_password}"
    def userpass = "Basic " + userpassascii.encodeAsBase64().toString()
    log.debug device.deviceId
    
    def zoneDeviceID = ["","","","","","","","","","","","","","",""]
    
    log.debug "Zone Contact Sensor DNI Search"
    for (i in 1..15) {
        log.debug "Checking Zone ${i}"
            def currentchild = getChildDevices()?.find { it.deviceNetworkId == "${device.deviceNetworkId}-zone${i}"}
            if (currentchild != null) {
                //addChildDevice("hubitat", "Virtual Contact Sensor", "${device.deviceNetworkId}-zone${i}", [name: "${zonename[i-1]}", isComponent: false])
                zoneDeviceID[i-1] = (getChildDevice("${device.deviceNetworkId}-zone${i}")).id
                log.debug "Device found. Populating Zone ${i} DNI."
            }
            else {
                log.debug "Zone ${i} not found."
            }
    }
    
    def concordconfigdata = "${device.deviceId}&${zoneDeviceID[0]}&${zoneDeviceID[1]}&${zoneDeviceID[2]}&${zoneDeviceID[3]}&${zoneDeviceID[4]}&${zoneDeviceID[5]}&${zoneDeviceID[6]}&${zoneDeviceID[7]}&${zoneDeviceID[8]}&${zoneDeviceID[9]}&${zoneDeviceID[10]}&${zoneDeviceID[11]}&${zoneDeviceID[12]}&${zoneDeviceID[13]}&${zoneDeviceID[14]}"
     
    def hubAction = new hubitat.device.HubAction(
   	 		'method': 'POST',
            'path': "/concord/config=${concordconfigdata}",
        	'body': '',
            'headers': [ HOST: "${concord_server_ip_address}:${concord_server_port}" , Authorization:userpass]
		)

    log.debug hubAction
    return hubAction
}

def request(request) {
	log.debug("Request:'${request}'")
	def userpassascii = "admin:${concord_server_api_password}"
    def userpass = "Basic " + userpassascii.encodeAsBase64().toString()
    def hubAction = new hubitat.device.HubAction(
   	 		'method': 'POST',
            'path': "/concord${request}",
        	'body': '',
        	'headers': [ HOST: "${concord_server_ip_address}:${concord_server_port}" , Authorization:userpass]
		)

    log.debug hubAction
    log.debug "/concord${request}"
    return hubAction
}

////////////// Silence Switch commands
def off(){
    state.bLoud = "False"
    sendEvent(name: "switch", value: "off")
}

def on(){
    state.bLoud = "True"
    sendEvent(name: "switch", value: "on")
}

////// Child arm/disarm switches

// These two commands will turn switches on and off
def childSwitchOn(num){
    def cd = getChildDevice("${device.deviceNetworkId}-Switch${num}")
    cd.parse([[name:"switch", value:"on", descriptionText:"${cd.displayName} was turned on in childSwitchOn"]])
}

def childSwitchOff(num){
    def cd = getChildDevice("${device.deviceNetworkId}-Switch${num}")
    cd.parse([[name:"switch", value:"off", descriptionText:"${cd.displayName} was turned off in childSwitchOff"]])
}
  
// These two commands are executed when switch status changes
def componentOn(cd){
    if (logEnable) log.info "received on request from ${cd.displayName}"
    getChildDevice(cd.deviceNetworkId).parse([[name:"switch", value:"on", descriptionText:"${cd.displayName} was turned on in componentOn"]])
    if (cd.name == "Disarm"){
        childSwitchOff(3)
        childSwitchOff(4)
        disarm()
    }
    else if (cd.name == "Arm Home"){
        if( state.appEnabled == "False"){
            childSwitchOff(2)
            childSwitchOff(4)
            armHome()
        }
        else{
            // If you are using the app, the driver will not be able to arm home unless the app indicates applicable doors (usually monitored zones plus garage door) are closed
            if( state.readyToArm == "True"){ 
                childSwitchOff(2)
                childSwitchOff(4)
                armHome()
            }
            else {
                log.debug "Cannot arm due to open door"
                childSwitchOn(2)
                childSwitchOff(3)
                //sendNotificationEvent("Cannot arm due to open door a")
                //sendPush("Cannot arm due to open door b")
             }
        }
    }
    else if (cd.name == "Arm Away"){
        // Note that the readyToArm signal isn't applicable here because we presumably want to exit the house... garage door can be open for this one.
        childSwitchOff(2)
        childSwitchOff(3)  
        armAway()
    }
    else if (cd.name == "Silence"){
        state.bLoud = "True"
    }
}

def componentOff(cd){
    if (logEnable) log.info "received off request from ${cd.displayName}"
    getChildDevice(cd.deviceNetworkId).parse([[name:"switch", value:"off", descriptionText:"${cd.displayName} was turned off in componentOff"]])
     if (cd.name == "Disarm"){
        childSwitchOn(2)
        disarm()
     }
     else if (cd.name == "Arm Home"){
        childSwitchOn(3)
        armHome() 
     }
     else if (cd.name == "Arm Away"){
        childSwitchOn(4)
        armAway()
    }
    else if(cd.name == "Silence"){
        state.bLoud = "False"
    }
    
}

////// App-related commands
def setReadyToArm(bool){
    state.readyToArm = bool
}

def setAppEnabled(){
    state.appEnabled = "True"
}