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

package org.maestro.client.notes;

import org.maestro.common.client.notes.MaestroCommand;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;

public class InternalError extends MaestroResponse {
    private String message;

    public InternalError(final String message) {
        super(MaestroCommand.MAESTRO_NOTE_INTERNAL_ERROR);

        this.message = message;
    }

    public InternalError(MessageUnpacker unpacker) throws IOException {
        super(MaestroCommand.MAESTRO_NOTE_INTERNAL_ERROR, unpacker);

        message = unpacker.unpackString();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    protected MessageBufferPacker pack() throws IOException {
        MessageBufferPacker packer = super.pack();

        packer.packString(message);

        return packer;
    }

    @Override
    public String toString() {
        return "InternalError{} " + super.toString();
    }
}
