/*
 * Copyright 2018 Otavio R. Piske <angusyoung@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.maestro.common.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Test properties used/saved by inspectors
 */
@SuppressWarnings("ALL")
public class InspectorProperties extends CommonProperties {
    public static String FILENAME = "inspector.properties";
    public static int UNSET_INT = 0;

    private static final Logger logger = LoggerFactory.getLogger(InspectorProperties.class);

    private String productName;
    private String productVersion;

    public void load(final File testProperties) throws IOException {
        logger.trace("Reading properties from {}", testProperties.getPath());

        Properties prop = new Properties();

        try (FileInputStream in = new FileInputStream(testProperties)) {
            prop.load(in);

            productName = prop.getProperty("productName");
            productVersion = prop.getProperty("productVersion");

            super.load(prop);
        }

    }

    public void write(final File testProperties) throws IOException {
        logger.trace("Writing properties to {}", testProperties.getPath());

        Properties prop = new Properties();

        if (productName != null) {
            prop.setProperty("productName", productName);
        }

        if (productVersion != null) {
            prop.setProperty("productVersion", productVersion);
        }

        super.write(prop);

        try (FileOutputStream fos = new FileOutputStream(testProperties)) {
            prop.store(fos, "maestro-inspector");
        }
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductVersion() {
        return productVersion;
    }

    public void setProductVersion(String productVersion) {
        this.productVersion = productVersion;
    }

    @Override
    public String toString() {
        return "InspectorProperties{" +
                "productName='" + productName + '\'' +
                ", productVersion='" + productVersion + '\'' +
                "} " + super.toString();
    }
}
