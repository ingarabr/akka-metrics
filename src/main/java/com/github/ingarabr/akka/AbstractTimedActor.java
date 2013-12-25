package com.github.ingarabr.akka;

import akka.actor.UntypedActor;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import scala.Option;

public abstract class AbstractTimedActor extends UntypedActor {

    private final Timer timer;
    private Timer.Context time;

    protected AbstractTimedActor(MetricRegistry metrics) {
        timer = metrics.timer(getTimerName());
    }

    protected String getTimerName() {
        return MetricRegistry.name(getClass(), "actor");
    }

    protected void startTimer() {
        if (time == null) {
            time = timer.time();
        }
    }

    protected void stopTimer() {
        if (time != null) {
            time.stop();
            time = null;
        }
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();
        stopTimer();
    }

    @Override
    public void preRestart(Throwable reason, Option<Object> message) throws Exception {
        super.preRestart(reason, message);
        stopTimer();
    }

}
