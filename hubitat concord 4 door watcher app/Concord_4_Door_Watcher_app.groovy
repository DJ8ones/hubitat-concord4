/**
 *  Concord 4 Integration via REST API Callback
 *
 *  Make sure and publish smartapp after pasting in code.
 *  Author: Daniel Jones
 *  Based on work by: Scott Dozier
 */
definition(
    name: "Concord 4 Door Watcher",
    namespace: "djones",
    author: "djones",
    description: "Handles the REST callback from concord and set virtual devices",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Labs/Cat-ST-Labs.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Labs/Cat-ST-Labs@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Labs/Cat-ST-Labs@3x.png")

preferences {
    section("Pick your alarm") {
		input "concord", "device.Concord4", required: true, multiple: true
	}
    
    section("Which contact sensors are required closed for Arm Home command?") {
		input "doors", "capability.contactSensor", required: true, multiple: true
	}
}

mappings {
	path("/concord/:id/:item/:state") {
		action: [
			GET: "updateStatus"
		]
	}

}

def alarmHandler(evt) {
     
    log.debug "Alarm Handler evt.value: ${evt.value}"
    
    doors.each{
        log.debug "ContactSensor: ${it.currentValue("contact")}"
        log.debug "ContactSensorIT: ${it}"
        
    }
    
    if (doors.every{it.currentValue("contact") == "closed"}){
        log.debug "All alarm-monitored doors are closed."
        concord.setReadyToArm("True")
    }
    else {
        log.debug "There are alarm-monitored doors still open!"
        concord.setReadyToArm("False")
    }
 
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
    subscribe(doors, "contact", alarmHandler)
    concord.setAppEnabled()
    if (doors.every{it.currentValue("contact") == "closed"}){
        log.debug "All alarm-monitored doors are closed."
        concord.setReadyToArm("True")
    }
    else {
        log.debug "There are alarm-monitored doors still open!"
        concord.setReadyToArm("False")
    }
}
