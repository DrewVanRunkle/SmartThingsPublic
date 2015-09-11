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
*  v1.1 (Sept 11th, 2015)
*	- Trying to update the tile view to reflect the new app features
*  	- Add Month Name to tiles.
*  
*  v1.1 (August 23rd, 2015)
*  	- Reworked and optimized most of http code to handle the different clamp types
*   - Added Refresh button to manually update the info as you wish
*  	- Added preference setting to enable to debug logging if you are having issues
*	- Uploading to GitHub
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
        command "poll"
        command "refresh"
	}
    
	tiles (scale: 2) {
    	
        multiAttributeTile(name:"richpower", type:"generic", width:6, height:4) {
    		tileAttribute("device.power", key: "PRIMARY_CONTROL") {
      			attributeState "default", label: '${currentValue} W', icon: "https://cdn4.iconfinder.com/data/icons/miu/22/circle_dashboard_meter_fuel_gauge-128.png", wordWrap: true, 
                foregroundColor: "#000000",
                backgroundColors:[
					[value: 1, color: "#00cc00"], //Light Green
                	[value: 2000, color: "#79b821"], //Darker Green
                	[value: 3000, color: "#ffa81e"], //Orange
					[value: 4000, color: "#fb1b42"] //Bright Red
				]
    		}
			tileAttribute("device.todayUsage", key: "SECONDARY_CONTROL") {
      			attributeState "default", label: '${currentValue}'
            }
  		}
        
        valueTile("monthUsage", "device.monthUsage", width: 4, height: 2, decoration: "flat") {
			state "default", label: '${currentValue}'
		}    
        
        valueTile("monthEstCost", "device.monthEstCost", width: 4, height: 2, decoration: "flat") {
			state "default", label: '${currentValue}'
		}   
        
        valueTile("readingUpdated", "device.readingUpdated", width: 6, height: 2, decoration: "flat") {
			state "default", label:'${currentValue}'
	    }
        
        valueTile("hubStatus", "device.hubStatus", width: 2, height: 1, decoration: "flat") {
			state "default", label: 'Hub Status:\n${currentValue}'
		}
        
        valueTile("hubVersion", "device.hubVersion", width: 2, height: 1, decoration: "flat") {
			state "default", label: 'Hub Version:\n${currentValue}'
		}
        
        standardTile("refresh", "command.refresh", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
		state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        
        main (["richpower"])
        details(["richpower", "monthUsage", "monthEstCost", "readingUpdated", "refresh", "hubStatus", "hubVersion"])
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
    }
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
}
	
// refresh command
def refresh() {
	log.debug "Refreshing data"
    getDayMonth()
    getSummaryReading()
 	getEstUsage()
    getStatusData()
}
    
// Poll command
def poll() {
	log.debug "Poll command received..."
    refresh()
}

//Converts Today's DateTime into Day of Week and Month Name ("September")
def getDayMonth() {
	def today = new Date()
    def month
    def day
    month = new SimpleDateFormat("MMMM").format(today)
    day = new SimpleDateFormat("EEEE").format(today)
   
    if (month != null && day != null) {
    	state.monthName = month
        state.dayOfWeek = day
    }  
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
            sendEvent(name: "todayUsage", value: "Today\'s Usage: \$${estUseResp.data.day_tariff.estimate} (${estUseResp.data.day_kwh.estimate} kWh)")
            sendEvent(name: "monthUsage", value: "${state.monthName} Usage\n\$${estUseResp.data.month_tariff.previousSum} (${estUseResp.data.month_kwh.previousSum} kWh)")
            sendEvent(name: "monthEstCost", value: "${state.monthName}\'s Cost\n(Est.)\n\$${estUseResp.data.month_tariff.estimate}")
            
            //Show Debug logging if enabled in preferences
            if(state.showLogging) {
            	log.debug "Est Usage Response: $estUseResp.data"
            	log.debug "Today's Estimated Usage: $estUseResp.data.day_kwh.estimate"
            	log.debug "Today's Estimated Cost: $estUseResp.data.day_tariff.estimate"
            	log.debug "${state.monthName}\'s Estimated Usage: $estUseResp.data.month_kwh.previousSum"
            	log.debug "${state.monthName}\'s Current Cost: $estUseResp.data.month_tariff.previousSum"
            	log.debug "${state.monthName}\'s Estimated Cost: $estUseResp.data.month_tariff.estimate"
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
    def tf = new java.text.SimpleDateFormat("M/d/yyyy - h:mm:ss a")
    	tf.setTimeZone(TimeZone.getTimeZone("America/New_York"))
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
            state.cidPowerReading = cidReading
            
            //Formats epoch time to Human DateTime Format
            readingUpdated = "${tf.format(longTimeVal)}"
            
            //Send Device events to tiles
            sendEvent(name: "energy", unit: "kWh", value: cidReading.toInteger()/1000)
            sendEvent(name: "power", unit: "W", value: cidReading)
        	sendEvent(name: "readingUpdated", value: "Last Updated:\n${readingUpdated}")
            sendEvent(name: "cidType", value: state.cidType)
            sendEvent(name: "monthName", value: state.monthName)
            sendEvent(name: "dayOfWeek", value: state.dayOfWeek)
            
			//Show Debug logging if enabled in preferences
            if(state.showLogging) {
            	log.debug "Summary Response: " + respData
				log.debug "Cid Type: " + state.cidType
            	log.debug "Cid Unit: " + cidUnit
            	log.debug "Timestamp: " + timeVal
            	log.debug "reading: " + cidReading
            	log.debug "Last Updated: " + readingUpdated
                log.debug "Reading Age: " + cidReadingAge
                log.debug "Current Month: ${state.monthName}"
                log.debug "Day of Week: ${state.dayOfWeek}"
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
