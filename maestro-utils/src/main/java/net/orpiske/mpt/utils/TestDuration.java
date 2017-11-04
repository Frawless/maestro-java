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

package net.orpiske.mpt.utils;

import net.orpiske.mpt.utils.exceptions.DurationParseException;

public class TestDuration<T> {
    private T duration;

    private TestDuration(T duration) {
        this.duration = duration;

    }

    public T getDuration() {
        return duration;
    }

    public long getNumericDuration() throws DurationParseException {
        if (duration instanceof String) {
            String durationStr = (String) duration;
            return DurationUtils.parse(durationStr);
        }

        if (Long.class.isInstance(duration)) {
            return (Long) duration;
        }

        // Should never happen
        return 0;
    }


    @Override
    public String toString() {
        if (duration instanceof String) {
            return (String) duration;
        }

        if (Long.class.isInstance(duration)) {
            return Long.toString((Long) duration);
        }

        // Should never happen
        return null;
    }

    public static TestDuration<String> newInstance(String duration) {
        return new TestDuration<>(duration);
    }

    public TestDuration<Long> newInstance(long duration) {
        return new TestDuration<>(duration);
    }


}