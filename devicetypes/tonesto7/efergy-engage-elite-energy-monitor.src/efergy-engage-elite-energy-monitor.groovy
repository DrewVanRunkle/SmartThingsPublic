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
 *  V1.0.2 (August 20th, 2015)
 *  Commented out debug logging to prevent log spamming
 *  V1.0.1 (July 23, 2015)
 *  Minor Code Cleanup
 *  V1.0.0 (June 5, 2015)
 *  Initial Working Release 
 *  ---------------------------
 */
	preferences {
        input "token", "text", title: "Access Token", required: true
	}

	metadata {
	definition (name: "Efergy Engage Elite Energy Monitor", namespace: "tonesto7", author: "tonesto7") {
		capability "Energy Meter"
        capability "Power Meter"
        capability "Polling"
	}

	// simulator metadata
	simulator {
		for (int i = 0; i <= 100; i += 10) {
			status "energy  ${i} kWh": new physicalgraph.zwave.Zwave().meterV1.meterReport(
				scaledMeterValue: i, precision: 3, meterType: 0, scale: 0, size: 4).incomingMessage()
		}
	}
	
	tiles {
    	valueTile("energy", "device.energy") {
			state "default", label: 'Right\nNow\n${currentValue} kWh',
            	foregroundColors:[
            		[value: 1, color: "#000000"], //Black
            		[value: 1000, color: "#ffffff"] //White
            	], 
            	foregroundColor: "#000000", //Black
				backgroundColors:[
					[value: 1, color: "#00cc00"], //Light Green
                    [value: 1000, color: "#79b821"], //Darker Green
                   	[value: 1800, color: "#ffa81e"], //Orange
					[value: 4000, color: "#fb1b42"] //Bright Red
				]
        }
        
        valueTile("todayUsage", "device.todayUsage") {
			state "default", label: 'Today\'s\nUsage\n${currentValue} kW',
            foregroundColors:[
            		[value: 1, color: "#000000"],  //Black
                    [value: 20, color: "#ffffff"]  //White
            	], 
            foregroundColor: "#000000", //Black
            backgroundColors:[
            		[value: 0, color: "#153591"],  //Dark Blue
					[value: 10, color: "#ffd500"],  //Yellow
                    [value: 20, color: "#ffa500"],  //Orange
					[value: 30, color: "#bc2323"]  //Dark Red
				]
			}
                
        valueTile("todayCost", "device.todayCost") {
			state "default", label: 'Today\'s\nUsage Cost\n \$${currentValue}',
            foregroundColors:[
            		[value: 1, color: "#000000"],  //Black
                    [value: 3, color: "#ffffff"]  //White
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
			state "default", label: 'This\nMonth\'s Use\n${currentValue} kW',
            foregroundColors:[
            		[value: 1, color: "#000000"],  //Black
                    [value: 200, color: "#ffffff"]  //White
            	], 
            foregroundColor: "#000000",  //Black
            backgroundColors:[
            		[value: 0, color: "#153591"],  //Dark Blue
					[value: 200, color: "#ffd500"],  //Yellow
                    [value: 400, color: "#ffa500"],  //Orange
					[value: 600, color: "#bc2323"]  //Dark Red
				]
			}    
        
        valueTile("monthCost", "device.monthCost") {
			state "default", label: 'This\nMonth\'s Cost\n \$${currentValue}',
            foregroundColors:[
            		[value: 1, color: "#000000"],  //Black
                    [value: 100, color: "#ffffff"]  //White
            	], 
            foregroundColor: "#000000",  //Black
            backgroundColors:[
            		[value: 0, color: "#153591"],  //Dark Blue
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
            
        main (["energy"])
        details(["energy","todayUsage","todayCost","monthUsage","monthCost","monthEstCost","readingUpdated"])
		}
 	}

	// parse events into attributes
	def parse(String description) {
		log.debug "Parsing '${description}'"
	}

	// handle commands
	def poll() {
    	log.debug "Executing 'poll'"
       	getReading()
        getEstUsage()
	}

	// Get the sensor reading
	private getReading() {
    def today = new java.util.Date()
    def strDateFormat = "MMMM";
    def sdf = new java.text.SimpleDateFormat(strDateFormat);
    def curMonthName = sdf.format(today)
    def curMonth = device.currentValue('monthName')
    def readingClosure = { 
        response -> 
            log.debug "result: $response.data"
            //log.debug "last reading time: $response.data.last_reading_time"
            //log.debug "reading: $response.data.reading"
            //log.debug "Current Month: $curMonthName"
            sendEvent(name: "energy", unit: "watts", value: response.data.reading)
            def tf = new java.text.SimpleDateFormat("MMM d, yyyy - h:mm:ss a")
        	tf.setTimeZone(TimeZone.getTimeZone("America/New_York"))
        	def readingUpdated = "${tf.format(response.data.last_reading_time)}"
            log.debug "Last Updated: $readingUpdated"
        	sendEvent(name: "readingUpdated", value: readingUpdated)
            sendEvent(name: "curMonthName", value: curMonth)
        }
	def params = [
    	uri: "https://engage.efergy.com",
    	path: "/mobile_proxy/getInstant",
        query: ["token": token],
        contentType: 'application/json'
    	]
	httpGet(params, readingClosure)
	}
    
    // Get extended energy metrics
    private getEstUsage() {
    def estUsageClosure = { 
        response -> 
            log.debug "result: $response.data"
            //log.debug "Today's Estimated Usage: $response.data.day_kwh.estimate"
            //log.debug "Today's Estimated Cost: $response.data.day_tariff.estimate"
            //log.debug "This Month's Estimated Usage: $response.data.month_kwh.previousSum"
            //log.debug "This Month's Current Cost: $response.data.month_tariff.previousSum"
            //log.debug "This Month's Estimated Cost: $response.data.month_tariff.estimate"
            sendEvent(name: "todayUsage", value: response.data.day_kwh.estimate)
            sendEvent(name: "todayCost", value: response.data.day_tariff.estimate)
            sendEvent(name: "monthUsage", value: response.data.month_kwh.previousSum)
            sendEvent(name: "monthCost", value: response.data.month_tariff.previousSum)
            sendEvent(name: "monthEstCost", value: response.data.month_tariff.estimate)
		}
        
	def params = [
    	uri: "https://engage.efergy.com",
    	path: "/mobile_proxy/getEstCombined",
        query: ["token": token],
        contentType: 'application/json'
    	]
	
	httpGet(params, estUsageClosure)
	}