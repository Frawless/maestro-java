/*
 *  Copyright 2017 Otavio R. Piske <angusyoung@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.maestro.maestro;

import org.junit.Test;
import org.maestro.client.exchange.MaestroDeserializer;
import org.maestro.client.notes.*;
import org.maestro.client.notes.InternalError;
import org.maestro.common.client.notes.GetOption;
import org.maestro.common.client.notes.MaestroCommand;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.common.client.notes.MaestroNoteType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MaestroProtocolTest {

    private byte[] doSerialize(MaestroNote note) throws Exception {
        return note.serialize();
    }


    @Test
    public void serializePingRequest() throws Exception {
        MaestroNote parsed = MaestroDeserializer.deserialize(doSerialize(new PingRequest()));

        assertTrue(parsed instanceof PingRequest);
        assertTrue("Parsed object is not a ping request",
                parsed.getNoteType() == MaestroNoteType.MAESTRO_TYPE_REQUEST);
        assertTrue(parsed.getMaestroCommand() == MaestroCommand.MAESTRO_NOTE_PING);

        assertTrue(((PingRequest) parsed).getSec() != 0);
        assertTrue(((PingRequest) parsed).getUsec() != 0);
    }

    @Test
    public void serializeFlushRequest() throws Exception {
        MaestroNote parsed = MaestroDeserializer.deserialize(doSerialize(new FlushRequest()));

        assertTrue("Parsed object is not a flush request",
                parsed instanceof FlushRequest);

        assertTrue(parsed.getNoteType() == MaestroNoteType.MAESTRO_TYPE_REQUEST);
        assertTrue(parsed.getMaestroCommand() == MaestroCommand.MAESTRO_NOTE_FLUSH);
    }


    @Test
    public void serializeOkResponse() throws Exception {
        MaestroNote parsed = MaestroDeserializer.deserialize(doSerialize(new OkResponse()));

        assertTrue(parsed instanceof OkResponse);
        assertTrue("Parsed object is not a OK response",
                parsed.getNoteType() == MaestroNoteType.MAESTRO_TYPE_RESPONSE);
        assertTrue(parsed.getMaestroCommand() == MaestroCommand.MAESTRO_NOTE_OK);
    }


    @Test
    public void serializeTestSuccessfulNotification() throws Exception {
        TestSuccessfulNotification tsn = new TestSuccessfulNotification();

        tsn.setId("asfas45");
        tsn.setName("unittest@localhost");
        tsn.setMessage("Test completed successfully");

        MaestroNote parsed = MaestroDeserializer.deserialize(doSerialize(tsn));

        assertTrue(parsed instanceof TestSuccessfulNotification);
        assertTrue("Parsed object is not a Test Successful Notification",
                parsed.getNoteType() == MaestroNoteType.MAESTRO_TYPE_NOTIFICATION);
        assertTrue(parsed.getMaestroCommand() == MaestroCommand.MAESTRO_NOTE_NOTIFY_SUCCESS);
    }

    @Test
    public void serializeTestFailedNotification() throws Exception {
        TestFailedNotification tsn = new TestFailedNotification();

        tsn.setId("asfas45");
        tsn.setName("unittest@localhost");
        tsn.setMessage("Test failed");

        MaestroNote parsed = MaestroDeserializer.deserialize(doSerialize(tsn));

        assertTrue(parsed instanceof TestFailedNotification);
        assertTrue("Parsed object is not a Test failed Notification",
                parsed.getNoteType() == MaestroNoteType.MAESTRO_TYPE_NOTIFICATION);
        assertTrue(parsed.getMaestroCommand() == MaestroCommand.MAESTRO_NOTE_NOTIFY_FAIL);
    }


    @Test
    public void serializeGetRequest() throws Exception {
        GetRequest getRequest = new GetRequest();
        getRequest.setGetOption(GetOption.MAESTRO_NOTE_OPT_GET_DS);

        MaestroNote parsed = MaestroDeserializer.deserialize(doSerialize(getRequest));

        assertTrue(parsed instanceof GetRequest);
        assertTrue("Parsed object is not a GET request",
                parsed.getNoteType() == MaestroNoteType.MAESTRO_TYPE_REQUEST);
        assertTrue(parsed.getMaestroCommand() == MaestroCommand.MAESTRO_NOTE_GET);
    }

    @Test
    public void serializeGetResponse() throws Exception {
        final String url = "http://0.0.0.0:8101/";
        GetResponse getResponse = new GetResponse();
        getResponse.setOption(GetOption.MAESTRO_NOTE_OPT_GET_DS);
        getResponse.setValue(url);

        MaestroNote parsed = MaestroDeserializer.deserialize(doSerialize(getResponse));

        assertTrue(parsed instanceof GetResponse);
        assertTrue("Parsed object is not a GET response",
                parsed.getNoteType() == MaestroNoteType.MAESTRO_TYPE_RESPONSE);
        assertTrue(parsed.getMaestroCommand() == MaestroCommand.MAESTRO_NOTE_GET);
        assertEquals("URLs do not match", url, ((GetResponse)parsed).getValue());
    }

    @Test
    public void serializeStatsRequest() throws Exception {
        StatsRequest statsRequest = new StatsRequest();

        MaestroNote parsed = MaestroDeserializer.deserialize(doSerialize(statsRequest));

        assertTrue(parsed instanceof StatsRequest);
        assertTrue("Parsed object is not a STATS Request",
                parsed.getNoteType() == MaestroNoteType.MAESTRO_TYPE_REQUEST);
        assertTrue(parsed.getMaestroCommand() == MaestroCommand.MAESTRO_NOTE_STATS);
    }


    @Test
    public void serializeStatsResponse() throws Exception {
        StatsResponse statsResponse = new StatsResponse();

        statsResponse.setChildCount(0);

        statsResponse.setRole("tester");
        statsResponse.setLatency(1.123);
        statsResponse.setRate(1122);
        statsResponse.setRoleInfo("");
        statsResponse.setTimestamp("1521027548");

        MaestroNote parsed = MaestroDeserializer.deserialize(doSerialize(statsResponse));

        assertTrue(parsed instanceof StatsResponse);
        assertTrue("Parsed object is not a STATS Request",
                parsed.getNoteType() == MaestroNoteType.MAESTRO_TYPE_RESPONSE);
        assertTrue(parsed.getMaestroCommand() == MaestroCommand.MAESTRO_NOTE_STATS);
        assertEquals("tester", ((StatsResponse) parsed).getRole());
        assertTrue(1.123 == ((StatsResponse) parsed).getLatency());
        assertEquals("1521027548", ((StatsResponse) parsed).getTimestamp());
    }


    @Test
    public void serializeDrainCompleteNotification() throws Exception {
        DrainCompleteNotification note = new DrainCompleteNotification();

        note.setId("asfas45");
        note.setName("unittest@localhost");
        note.setMessage("Test failed");

        MaestroNote parsed = MaestroDeserializer.deserialize(doSerialize(note));

        assertTrue(parsed instanceof DrainCompleteNotification);
        assertTrue("Parsed object is not a Drain Complete Notification",
                parsed.getNoteType() == MaestroNoteType.MAESTRO_TYPE_NOTIFICATION);
        assertTrue(parsed.getMaestroCommand() == MaestroCommand.MAESTRO_NOTE_NOTIFY_DRAIN_COMPLETE);
    }

    @Test
    public void serializeInternalError() throws Exception {
        InternalError note = new InternalError("Something bad happened");

        note.setId("asfas45");
        note.setName("unittest@localhost");
        note.setMessage("Test failed");

        MaestroNote parsed = MaestroDeserializer.deserialize(doSerialize(note));

        assertTrue(parsed instanceof InternalError);
        assertTrue("Parsed object is not a Drain Complete Notification",
                parsed.getNoteType() == MaestroNoteType.MAESTRO_TYPE_RESPONSE);
        assertTrue(parsed.getMaestroCommand() == MaestroCommand.MAESTRO_NOTE_INTERNAL_ERROR);
    }


    @Test
    public void serializeStartSenderRequest() throws Exception {
        StartSender note = new StartSender();

        MaestroNote parsed = MaestroDeserializer.deserialize(doSerialize(note));

        assertTrue(parsed instanceof StartSender);
        assertTrue("Parsed object is not a Start Sender message",
                parsed.getNoteType() == MaestroNoteType.MAESTRO_TYPE_REQUEST);
        assertTrue(parsed.getMaestroCommand() == MaestroCommand.MAESTRO_NOTE_START_SENDER);
    }

    @Test
    public void serializeStartReceiverRequest() throws Exception {
        StartReceiver note = new StartReceiver();

        MaestroNote parsed = MaestroDeserializer.deserialize(doSerialize(note));

        assertTrue(parsed instanceof StartReceiver);
        assertTrue("Parsed object is not a Start Receiver message",
                parsed.getNoteType() == MaestroNoteType.MAESTRO_TYPE_REQUEST);
        assertTrue(parsed.getMaestroCommand() == MaestroCommand.MAESTRO_NOTE_START_RECEIVER);
    }

    @Test
    public void serializeStartInspectorRequest() throws Exception {
        StartInspector note = new StartInspector("testInspector");

        MaestroNote parsed = MaestroDeserializer.deserialize(doSerialize(note));

        assertTrue(parsed instanceof StartInspector);
        assertTrue("Parsed object is not a Start Inspector message",
                parsed.getNoteType() == MaestroNoteType.MAESTRO_TYPE_REQUEST);
        assertTrue(parsed.getMaestroCommand() == MaestroCommand.MAESTRO_NOTE_START_INSPECTOR);
        assertEquals("Inspector name don't match", "testInspector",
                ((StartInspector) parsed).getPayload());
    }

    @Test
    public void serializeStopSenderRequest() throws Exception {
        StopSender note = new StopSender();

        MaestroNote parsed = MaestroDeserializer.deserialize(doSerialize(note));

        assertTrue(parsed instanceof StopSender);
        assertTrue("Parsed object is not a Stop Sender message",
                parsed.getNoteType() == MaestroNoteType.MAESTRO_TYPE_REQUEST);
        assertTrue(parsed.getMaestroCommand() == MaestroCommand.MAESTRO_NOTE_STOP_SENDER);
    }

    @Test
    public void serializeStopReceiverRequest() throws Exception {
        StopReceiver note = new StopReceiver();

        MaestroNote parsed = MaestroDeserializer.deserialize(doSerialize(note));

        assertTrue(parsed instanceof StopReceiver);
        assertTrue("Parsed object is not a Start Receiver message",
                parsed.getNoteType() == MaestroNoteType.MAESTRO_TYPE_REQUEST);
        assertTrue(parsed.getMaestroCommand() == MaestroCommand.MAESTRO_NOTE_STOP_RECEIVER);
    }

    @Test
    public void serializeStopInspectorRequest() throws Exception {
        StopInspector note = new StopInspector();

        MaestroNote parsed = MaestroDeserializer.deserialize(doSerialize(note));

        assertTrue(parsed instanceof StopInspector);
        assertTrue("Parsed object is not a Start Inspector message",
                parsed.getNoteType() == MaestroNoteType.MAESTRO_TYPE_REQUEST);
        assertTrue(parsed.getMaestroCommand() == MaestroCommand.MAESTRO_NOTE_STOP_INSPECTOR);
    }

}
