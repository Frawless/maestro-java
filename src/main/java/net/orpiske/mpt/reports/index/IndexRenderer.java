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

package net.orpiske.mpt.reports.index;

import net.orpiske.mpt.reports.AbstractRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


public class IndexRenderer extends AbstractRenderer {
    private static final Logger logger = LoggerFactory.getLogger(IndexRenderer.class);

    public IndexRenderer(Map<String, Object> context) {
        super(context);
    }

    @Override
    public String render() throws Exception {
        return super.render("/net/orpiske/mpt/reports/index-main.html");
    }
}
