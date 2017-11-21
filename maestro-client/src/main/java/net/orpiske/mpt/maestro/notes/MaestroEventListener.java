/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.orpiske.mpt.maestro.notes;

/**
 * Visitor that handles {@link MaestroEvent} instances.
 */
public interface MaestroEventListener {

    void handle(PingRequest note);

    void handle(StatsRequest note);

    void handle(FlushRequest note);

    void handle(Halt note);

    void handle(SetRequest note);

    void handle(StartInspector note);

    void handle(StartReceiver note);

    void handle(StartSender note);

    void handle(StopInspector note);

    void handle(StopReceiver note);

    void handle(StopSender note);

    void handle(TestFailedNotification note);

    void handle(TestSuccessfulNotification note);

    void handle(AbnormalDisconnect note);
}