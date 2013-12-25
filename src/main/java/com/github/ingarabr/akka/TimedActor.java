package com.github.ingarabr.akka;

import com.codahale.metrics.MetricRegistry;
import scala.Option;

public abstract class TimedActor extends AbstractTimedActor {

    protected TimedActor(MetricRegistry metrics) {
        super(metrics);
    }

    protected abstract void onTimedReceive(Object message);

    @Override
    public void onReceive(Object message) throws Exception {
        startTimer();
        onTimedReceive(message);
        stopTimer();
    }

    @Override
    public void preRestart(Throwable reason, Option<Object> message) throws Exception {
        super.preRestart(reason, message);
        stopTimer();
    }

}
