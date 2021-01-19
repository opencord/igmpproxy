/*
 * Copyright 2017-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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