/**
 *  Efergy Engage Updater Service
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
 *  Last Updated : 8/23/2015
 *
 */
definition(
	name: "Efergy Service Manager",
	namespace: "tonesto7",
	author: "Anthony S.",
	description: "Updates Efergy Engage Energy Usage",
	category: "My Apps",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@3x.png"
)

preferences {
	for (int n = 1; n <= 1; n++) {
        section("Polling Group ${n}") {
            input "group_${n}", "capability.polling", title:"Select devices to be polled", multiple:true, required:false
            input "refresh_${n}", "capability.refresh", title:"Select devices to be refreshed", multiple:true, required:false
            input "interval_${n}", "number", title:"Set polling interval (in seconds)", defaultValue:1
        }
    }
	
    section("About") {
    	paragraph "This app in it currents state is based on Pollster.  It updates roughly ever 30 seconds."
        paragraph "${textVersion()}\n${textCopyright()}"
    }
}

def installed() {
    initialize()
}

def updated() {
    unschedule()
    initialize()
}

def onAppTouch(event) {
    TRACE("onAppTouch(${event.value})")

    def devPoll = []
    def devRefresh = []

    for (int n = 1; n <= 1; n++) {
        if (settings["group_${n}"]) {
            devPoll.addAll(settings["group_${n}"])
        }

        if (settings["refresh_${n}"]) {
            devRefresh.addAll(settings["refresh_${n}"])
        }
    }

    defPoll*.poll()
    defRefresh*.refresh()
}

def pollingTask1(Integer sec) {
    TRACE("pollingTask1()")
    log.debug "sec: ${sec}"
	
    if (settings.group_1) {
        settings.group_1*.poll()
    }

    if (settings.refresh_1) {
        settings.refresh_1*.refresh()
    }
    def seconds = settings.interval_1.toInteger()
    log.debug "seconds: ${seconds}"
    
}


 def initialize() {
    log.info "Efergy Device Manager. ${textVersion()}. ${textCopyright()}"
    TRACE("initialize() with settings: ${settings}")

    for (int n = 1; n <= 1; n++) {
        def sec1 = settings."interval_${n}".toInteger()
        def size1 = settings["group_${n}"]?.size() ?: 0
        def size2 = settings["refresh_${n}"]?.size() ?: 0

        if (sec1 > 0 && (size1 + size2) > 0) {
            log.debug "Scheduling polling task ${n} to run every ${sec1} seconds."
            TRACE("Scheduling polling task ${n} to run every ${sec1} seconds.")
            
            def sched = "1/${sec1} * * * * ?"
            switch (n) {
            case 1:
                schedule(sched, "pollingTask1")
                break;
            }
        }
    }

    subscribe(app, onAppTouch)
    schedule("32 3 0/4 1/1 * ?", "updated")
}

private def textVersion() {
    def text = "Version 1.0.0 (8/23/2015)"
}

private def textCopyright() {
    def text = "Copyright © 2015 Anthony S."
}

private def TRACE(message) {
    log.trace message
}