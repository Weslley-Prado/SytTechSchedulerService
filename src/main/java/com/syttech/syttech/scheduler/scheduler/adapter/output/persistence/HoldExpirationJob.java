package com.syttech.syttech.scheduler.scheduler.adapter.output.persistence;

import com.syttech.syttech.scheduler.scheduler.ports.out.HoldRepositoryPort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Periodically releases holds whose TTL has expired. */
@Component
public class HoldExpirationJob {

    private static final Logger LOG = LoggerFactory.getLogger(HoldExpirationJob.class);

    private final HoldRepositoryPort holds;

    public HoldExpirationJob(final HoldRepositoryPort holds) {
        this.holds = holds;
    }

    /** Runs every minute. */
    @Scheduled(
            fixedDelayString = "${syttech.scheduler.hold-expiration.fixed-delay-ms:60000}",
            initialDelay = 10_000)
    public void releaseExpiredHolds() {
        int released = holds.releaseExpired();
        if (released > 0) {
            LOG.info("[hold-expiration-job] released {} expired holds", released);
        }
    }
}
