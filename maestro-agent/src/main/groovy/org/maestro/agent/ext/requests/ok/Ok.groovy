package org.maestro.agent.ext.requests.ok

import org.maestro.agent.base.AbstractHandler
import org.maestro.client.exchange.MaestroTopics
import org.maestro.client.notes.OkResponse

class OkHandler extends AbstractHandler {

    @Override
    Object handle() {
        this.getClient().publish(MaestroTopics.MAESTRO_TOPIC, new OkResponse())
        return null
    }
}