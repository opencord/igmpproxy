package org.opencord.igmpproxy.impl;

import org.opencord.igmpproxy.IgmpReport;
import org.slf4j.Logger;

import java.util.concurrent.BlockingQueue;

import static org.slf4j.LoggerFactory.getLogger;

public class IgmpReportProcessor implements Runnable {

    private final Logger log;

    protected final BlockingQueue<IgmpReport> queue;
    private final IgmpManager igmpManager;
    private boolean isConsuming;

    public IgmpReportProcessor(BlockingQueue<IgmpReport> queue, IgmpManager igmpManager, String threadName) {
        this.queue = queue;
        this.igmpManager = igmpManager;
        this.isConsuming = true;
        this.log = getLogger(threadName);
    }

    public void run() {
        while (isConsuming) {
            try {
                IgmpReport report = queue.take();
                log.debug("Taken this job: {}", report);
                igmpManager.processIgmpReport(report);
            } catch (InterruptedException e) {
                stopConsuming();
                log.info("Interrupted: ", e);
                break;
            } catch (Exception e) {
                log.error("Error occurred: ", e);
            }
        }
        log.info("Thread terminated.");
    }

    public void stopConsuming() {
        isConsuming = false;
    }
}