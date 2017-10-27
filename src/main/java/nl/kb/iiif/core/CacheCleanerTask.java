package nl.kb.iiif.core;

import com.google.common.util.concurrent.AbstractScheduledService;

import java.util.concurrent.TimeUnit;

public class CacheCleanerTask extends AbstractScheduledService {
    private final FileCacher fileCacher;

    public CacheCleanerTask(FileCacher fileCacher) {
        this.fileCacher = fileCacher;
    }

    @Override
    protected void runOneIteration() throws Exception {
        fileCacher.expire();
    }

    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedRateSchedule(0, 1, TimeUnit.MINUTES);
    }
}
