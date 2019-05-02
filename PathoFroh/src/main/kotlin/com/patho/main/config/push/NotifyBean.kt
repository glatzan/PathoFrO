package com.patho.main.config.push

import org.primefaces.push.EventBus
import org.primefaces.push.RemoteEndpoint
import org.primefaces.push.annotation.OnClose
import org.primefaces.push.annotation.OnMessage
import org.primefaces.push.annotation.OnOpen
import org.primefaces.push.annotation.PushEndpoint
import org.primefaces.push.impl.JSONEncoder

@PushEndpoint("/test")
class NotifyBean {

    @OnOpen
    fun onOpen(r: RemoteEndpoint?, eventBus: EventBus?) {
    }

    @OnClose
    fun onClose(r: RemoteEndpoint?, eventBus: EventBus?) {
    }


    @OnMessage(encoders = [JSONEncoder::class])
    fun onMessage(count: String?): String? {
        println("OnMessage $count")
        return count
    }
}

//<script>
//function test(message) {
//    console.log('jsf push message::' + message + ", channel ::" );
//}
//</script>
//
//<p:socket onMessage="test" channel="/test" autoConnect="true" widgetVar='subscriber' />


//private val eventBus = EventBusFactory.getDefault().eventBus()
//
//fun sendMessage() {
//    eventBus.publish("/test", "asdas");
//    logger.debug(eventBus?.toString())
//}