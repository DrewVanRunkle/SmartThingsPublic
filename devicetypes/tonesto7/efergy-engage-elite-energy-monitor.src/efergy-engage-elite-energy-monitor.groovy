/**
*  Efergy Engage Energy Monitor
*	
*  Copyright 2015 Anthony S.
*
*  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License. You may obtain a copy of the License at:
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
*  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
*  for the specific language governing permissions and limitations under the License.
*
*  ---------------------------
*  v1.1 (August 23rd, 2015)
*  	- Reworked and optimized most of http code to handle the different clamp types
*   - Added Refresh button to manually update the info as you wish
*  	- Added preference setting to enable to debug logging if you are having issues
*	- Uploading to GitHub
*
*  V1.0.2 (August 20th, 2015)
*  Commented out debug logging to prevent log spamming
*
*  V1.0.1 (July 23, 2015)
*  Minor Code Cleanup

*  V1.0.0 (June 5, 2015)
*  Initial Release 
*  ---------------------------
*/
import groovy.json.JsonSlurper
import java.text.SimpleDateFormat 
    
def devTypeVer() {"1.2"}
def versionDate() {"9-10-2015"}
	
metadata {
	definition (name: "Efergy Engage Elite Energy Monitor", namespace: "tonesto7", author: "Anthony S.") {
		capability "Energy Meter"
        capability "Power Meter"
        capability "Polling"
        capability "Refresh"
        command "refresh"
        command "poll"
	}

	tiles {
    	valueTile("energy", "device.energy") {
			state "default", label: 'Right\nNow\n${currentValue} kW',
            foregroundColors:[
            	//[value: 1, color: "#000000"], //Black
            	[value: 1, color: "#ffffff"] //White
            ], 
            foregroundColor: "#000000", //Black
			backgroundColors:[
				[value: 1, color: "#00cc00"], //Light Green
                [value: 2000, color: "#79b821"], //Darker Green
                [value: 3000, color: "#ffa81e"], //Orange
				[value: 4000, color: "#fb1b42"] //Bright Red
			]
        }
        
        valueTile("todayUsage", "device.todayUsage") {
			state "default", label: 'Today\'s\nUsage\n${currentValue} kWh',
            foregroundColors:[
            	//[value: 1, color: "#000000"],  //Black
                [value: 1, color: "#ffffff"]  //White
            ], 
            foregroundColor: "#000000", //Black
            backgroundColors:[
            	[value: 0, color: "#153591"],  //Dark Blue
                [value: 10, color: "#778899"],  //light slate grey
				[value: 15, color: "#ffd500"],  //Yellow
                [value: 20, color: "#ffa500"],  //Orange
				[value: 30, color: "#bc2323"]  //Dark Red
			]
		}
                
        valueTile("todayCost", "device.todayCost") {
			state "default", label: 'Today\'s\nUsage Cost\n \$${currentValue}',
            foregroundColors:[
            	//[value: 1, color: "#000000"],  //Black
                [value: 1, color: "#ffffff"]  //White
            ], 
            foregroundColor: "#000000",  //Black
            backgroundColors:[
            	[value: 0, color: "#153591"],  //Dark Blue
				[value: 3, color: "#ffd500"],  //Yellow
                [value: 5, color: "#ffa500"],  //Orange
				[value: 7, color: "#bc2323"]  //Dark Red
			]
        }
        
        valueTile("monthUsage", "device.monthUsage") {
			state "default", label: 'This\nMonth\'s Use\n${currentValue} kWh',
            foregroundColors:[
            	//[value: 1, color: "#000000"],  //Black
                [value: 1, color: "#ffffff"]  //White
            ], 
            foregroundColor: "#000000",  //Black
            backgroundColors:[
            	[value: 10, color: "#00cc00"],  //Dark Blue
				[value: 400, color: "#ffd500"],  //Yellow
                [value: 600, color: "#ffa500"],  //Orange
				[value: 800, color: "#bc2323"]  //Dark Red
			]
		}    
        
        valueTile("monthCost", "device.monthCost") {
			state "default", label: 'This\nMonth\'s Cost\n \$${currentValue}',
            foregroundColors:[
            	//[value: 1, color: "#000000"],  //Black
                [value: 1, color: "#ffffff"]  //White
            ], 
            foregroundColor: "#000000",  //Black
            backgroundColors:[
            	[value: 50, color: "#153591"],  //Dark Blue
				[value: 100, color: "#ffd500"],  //Yellow
                [value: 150, color: "#ffa500"],  //Orange
				[value: 200, color: "#bc2323"]  //Dark Red
			]
        }    
            
        valueTile("monthEstCost", "device.monthEstCost", width: 2, height: 1, decoration: "flat") {
			state "default", label: 'This Month\'s\nEstimated Cost\n\$${currentValue}'
		}   
        
        valueTile("readingUpdated", "device.readingUpdated", width: 2, height: 1, decoration: "flat") {
			state "default", label:'Last Updated:\n${currentValue}'
	    }
        
        valueTile("hubStatus", "device.hubStatus", width: 1, height: 1, decoration: "flat") {
			state "default", label: 'Hub Status:\n${currentValue}'
		}
        
        valueTile("hubVersion", "device.hubVersion", width: 1, height: 1, decoration: "flat") {
			state "default", label: 'Hub Version:\n${currentValue}'
		}
        
        standardTile("refresh", "command.refresh", inactiveLabel: false, decoration: "flat") {
		state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
            
        main (["energy"])
        details(["energy","todayUsage","todayCost","monthUsage","monthCost","monthEstCost","readingUpdated","refresh","hubStatus","hubVersion"])
	}
}

preferences {
	section() {
        paragraph "Efergy API Token Entry"
        	input "token", "text", title: "Access Token", required: true
        
        paragraph "Enable this if you are having issues with tiles not updating...\n** This will create alot of Log Entries so its recommended that you disable when it's not needed **"
        	input "showLogging", "bool", title: "Enable Debug Logging", required: false, displayDuringSetup: false, defaultValue: false, refreshAfterSelection: true
        	if(showLogging == true){ 
            	state.showLogging = true
            	log.debug "Debug Logging Enabled!!!"    
            }
        	if(showLogging == false){ 
            	state.showLogging = false 
            	log.debug "Debug Logging Disabled!!!"                
            }
        //paragraph "This page will show you all of the info available from efergy..."
       	//href "hubInfoPage", title:"Efergy Hub Info", description:"Tap to view Hub Info"
    }
    /*
    section("Hub Info") {
       	paragraph ("Hub Name: ${state.hubName}" + 
            		"\nHub ID: ${state.hubId}" + 
                    "\nHub Type: ${state.hubType}" +
       				"\nHub Status: ${state.hubStatus}" + 
        			"\nLast energy Reading: ${state.cidReading}" + 
        			"\nHub Mac Address: ${state.hubMacAddr}"+
        			"\nLast Checkin: ${state.hubTsHuman}" + 
        			"\nHub Firmware: v${state.hubVersion}")
    }
    */
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
}
	
// refresh command
def refresh() {
	log.debug "Refreshing data"
    getMonth()
    getSummaryReading()
 	getEstUsage()
    getStatusData()
}
    
// Poll command
def poll() {
	log.debug "Poll command received..."
    refresh()
}
def getMonth() {
	def date = new Date()
    def sdf
    sdf = new SimpleDateFormat("MMMM").format(date)
    log.debug sdf
}
//Matches hubType to a full name
def getHubName(String hubType) {
	def hubName = ""
    switch (hubType) {
   		case 'EEEHub':
       		hubName = "Efergy Engage Elite Hub"
       	break
        default:
       		hubName "unknown"
	}
    state.hubName = hubName
}

// Get extended energy metrics
private getEstUsage() {
	def estUseClosure = { 
        estUseResp -> 
            //Sends extended metrics to tiles
            sendEvent(name: "todayUsage", value: estUseResp.data.day_kwh.estimate)
            sendEvent(name: "todayCost", value: estUseResp.data.day_tariff.estimate)
            sendEvent(name: "monthUsage", value: estUseResp.data.month_kwh.previousSum)
            sendEvent(name: "monthCost", value: estUseResp.data.month_tariff.previousSum)
            sendEvent(name: "monthEstCost", value: estUseResp.data.month_tariff.estimate)
            
            //Show Debug logging if enabled in preferences
            if(state.showLogging) {
            	log.debug "Est Usage Response: $estUseResp.data"
            	log.debug "Today's Estimated Usage: $estUseResp.data.day_kwh.estimate"
            	log.debug "Today's Estimated Cost: $estUseResp.data.day_tariff.estimate"
            	log.debug "This Month's Estimated Usage: $estUseResp.data.month_kwh.previousSum"
            	log.debug "This Month's Current Cost: $estUseResp.data.month_tariff.previousSum"
            	log.debug "This Month's Estimated Cost: $estUseResp.data.month_tariff.estimate"
            }
		}
        
	def params = [
    	uri: "https://engage.efergy.com",
    	path: "/mobile_proxy/getEstCombined",
        query: ["token": token],
        contentType: 'application/json'
    	]
	httpGet(params, estUseClosure)
}
 
/* Get the sensor reading
****  Json Returned: {"cid":"PWER","data":[{"1440023704000":0}],"sid":"123456","units":"kWm","age":142156},{"cid":"PWER_GAC","data":[{"1440165858000":1343}],"sid":"123456","units":null,"age":2}
*/
private getSummaryReading() {
    def today = new java.util.Date()
    def sdf = new java.text.SimpleDateFormat("MMMM");
    def tf = new java.text.SimpleDateFormat("MMM d, yyyy - h:mm:ss a")
    	tf.setTimeZone(TimeZone.getTimeZone("America/New_York"))
    state.curMonthName = sdf.format(today)
    def cidVal = "" 
	def cidData = [{}]
    def cidUnit = ""
    def timeVal
	def cidReading
    def cidReadingAge
    def readingUpdated
    def summaryClosure = { 
        summaryResp -> 
        	def respData = summaryResp.data.text
            
            //Converts http response data to list
			def cidList = new JsonSlurper().parseText(respData)
			
            //Search through the list for age to determine Cid Type
			for (rec in cidList) { 
    			if ((((rec.age >= 0) && (rec.age <= 10)) || (rec.age == 0) ) ) { 
                	cidVal = rec.cid 
        			cidData = rec.data
                    cidReadingAge = rec.age
                    if(rec.units != null)
                    	cidUnit = rec.units
        			break 
     			}
			}
            
 			//Convert data: values to individual strings
			for (item in cidData[0]) {
     			timeVal =  item.key
    			cidReading = item.value
			}
            
            //Converts timeVal string to long integer
            def longTimeVal = timeVal.toLong()

			//Save Cid Type to device state
			state.cidType = cidVal
            
            //Save Cid Unit to device state
            state.cidUnit = cidUnit
            
            //Save last Cid reading value to device state
            state.cidReading = cidReading
            
            //Formats epoch time to Human DateTime Format
            readingUpdated = "${tf.format(longTimeVal)}"
            
            //Send Device events to tiles
            sendEvent(name: "energy", unit: cidUnit, value: cidReading)
        	sendEvent(name: "readingUpdated", value: readingUpdated)
            sendEvent(name: "cidType", value: state.cidType)
            sendEvent(name: "curMonthName", value: curMonth)
            
			//Show Debug logging if enabled in preferences
            if(state.showLogging) {
            	log.debug "Summary Response: " + respData
				log.debug "Cid Type: " + state.cidType
            	log.debug "Cid Unit: " + cidUnit
            	log.debug "Timestamp: " + timeVal
            	log.debug "reading: " + cidReading
            	log.debug "Last Updated: " + readingUpdated
                log.debug "Reading Age: " + cidReadingAge
                log.debug "Current Month: ${state.curMonthName}"
            }
    }
	def summaryParams = [
    	uri: "https://engage.efergy.com",
    	path: "/mobile_proxy/getCurrentValuesSummary",
        query: ["token": token],
        contentType: "json"
    	]
	httpGet(summaryParams, summaryClosure)
}

// Get Status 
private getStatusData() {
	def hubId = ""
    def hubMacAddr = ""
    def hubStatus = ""
    def hubTsHuman
    def hubType = ""
    def hubVersion = ""
    
    def getStatusClosure = { 
        getStatusResp ->  
        	def respData = getStatusResp.data.text
            //Converts http response data to list
			def statusList = new JsonSlurper().parseText(respData)
			
            hubId = statusList.hid
            hubMacAddr = statusList.listOfMacs.mac
    		hubStatus = statusList.listOfMacs.status
    		hubTsHuman = statusList.listOfMacs.tsHuman
    		hubType = statusList.listOfMacs.type
    		hubVersion = statusList.listOfMacs.version
            
            //Save info to device state store
            state.hubId = hubId
            state.hubMacAddr = hubMacAddr.toString().replaceAll("\\[|\\{|\\]|\\}", "")
            state.hubStatus = hubStatus.toString().replaceAll("\\[|\\{|\\]|\\}", "")
            state.hubTsHuman = hubTsHuman.toString().replaceAll("\\[|\\{|\\]|\\}", "")
            state.hubType = hubType.toString().replaceAll("\\[|\\{|\\]|\\}", "")
            state.hubVersion = hubVersion.toString().replaceAll("\\[|\\{|\\]|\\}", "")
            state.hubName = getHubName(hubType)


			sendEvent(name: "hubVersion", value: state.hubVersion)
            sendEvent(name: "hubStatus", value: state.hubStatus)
			//Show Debug logging if enabled in preferences            
            if (state.showLogging) {
            	log.debug "Status Response: " + respData
            	log.debug "Hub ID: " + state.hubId
            	log.debug "Hub Mac: " + state.hubMacAddr
            	log.debug "Hub Status: " + state.hubStatus
            	log.debug "Hub TimeStamp: " + state.hubTsHuman
            	log.debug "Hub Type: " + state.hubType
            	log.debug "Hub Firmware: " + state.hubVersion
                log.debug "Hub Name: " + state.hubName
            }
    }
    //Http Get Parameters     
	def statusParams = [
    	uri: "https://engage.efergy.com",
    	path: "/mobile_proxy/getStatus",
        query: ["token": token],
        contentType: 'json'
    ]
	httpGet(statusParams, getStatusClosure)
}    
