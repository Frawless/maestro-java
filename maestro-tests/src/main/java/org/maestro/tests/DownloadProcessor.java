package org.maestro.tests;

import org.maestro.client.notes.GetResponse;
import org.maestro.client.notes.TestFailedNotification;
import org.maestro.client.notes.TestSuccessfulNotification;
import org.maestro.common.NodeUtils;
import org.maestro.common.client.notes.GetOption;
import org.maestro.reports.downloaders.ReportsDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Download the test reports
 */
public class DownloadProcessor {
    private static final Logger logger = LoggerFactory.getLogger(DownloadProcessor.class);
    private final Map<String, String> dataServers = new HashMap<>();
    private final ReportsDownloader reportsDownloader;

    public DownloadProcessor(final ReportsDownloader reportsDownloader) {
        this.reportsDownloader = reportsDownloader;
    }

    /**
     * Download the reports for test successful notifications
     * @param note the success notification
     * @return true if downloaded successfully or false otherwise
     */
    public boolean download(final TestSuccessfulNotification note) {
        String name = note.getName();
        if (name != null) {
            final String type = NodeUtils.getTypeFromName(name);
            final String host = dataServers.get(name);

            reportsDownloader.downloadLastSuccessful(type, host);

            return true;
        }

        return false;
    }


    /**
     * Download the reports for test failed notifications
     * @param note the failed notification
     * @return true if downloaded successfully or false otherwise
     */
    public boolean download(final TestFailedNotification note) {
        String name = note.getName();
        if (name != null) {
            final String type = NodeUtils.getTypeFromName(name);
            final String host = dataServers.get(name);

            reportsDownloader.downloadLastFailed(type, host);

            return true;
        }
        return false;
    }


    /**
     * Register a data server
     * @param note the get response note containing the data server address
     */
    public void addDataServer(final GetResponse note) {
        if (note.getOption() == GetOption.MAESTRO_NOTE_OPT_GET_DS) {
            logger.info("Registering data server at {}", note.getValue());
            dataServers.put(note.getName(), note.getValue());
        }
    }
}
