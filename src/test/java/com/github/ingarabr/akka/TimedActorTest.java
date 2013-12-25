package com.github.ingarabr.akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import com.codahale.metrics.MetricRegistry;
import org.hamcrest.number.IsCloseTo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class TimedActorTest {

    private MetricRegistry metricRegistry;
    private ActorSystem testActorSystem;

    @Before
    public void setUp() throws Exception {
        metricRegistry = new MetricRegistry();
        testActorSystem = ActorSystem.create("testActorSystem");
    }

    @After
    public void tearDown() throws Exception {
        testActorSystem.shutdown();
    }

    @Test
    public void shouldHaveMedianValuesAround10() throws Exception {
        new JavaTestKit(testActorSystem) {{
            ActorRef myTimedActor = getSystem().actorOf(Props.create(MyTimedActor.class, metricRegistry));

            sendMessages(myTimedActor);

            expectNoMsg(duration("55 ms"));
            String key = metricRegistry.getTimers().firstKey();
            long medianInNano = (long) metricRegistry.getTimers().get(key).getSnapshot().getMean();
            assertThat((double) TimeUnit.NANOSECONDS.toMillis(medianInNano), IsCloseTo.closeTo(10d, 2d));
        }};
    }

    @Test
    public void shouldHaveCountAllTells() throws Exception {
        new JavaTestKit(testActorSystem) {{
            ActorRef myTimedActor = getSystem().actorOf(Props.create(MyTimedActor.class, metricRegistry));

            sendMessages(myTimedActor);

            expectNoMsg(duration("55 ms"));
            String key = metricRegistry.getTimers().firstKey();
            assertThat(metricRegistry.getTimers().get(key).getCount(), is(4L));
        }};
    }

    private void sendMessages(ActorRef actor) {
        Long val = 10l;
        actor.tell(val, ActorRef.noSender());
        actor.tell(val, ActorRef.noSender());
        actor.tell(val, ActorRef.noSender());
        actor.tell(val, ActorRef.noSender());
    }

    static class MyTimedActor extends TimedActor {

        protected MyTimedActor(MetricRegistry metrics) {
            super(metrics);
        }

        @Override
        protected void onTimedReceive(Object message) {
            if (message instanceof Long) {
                try {
                    Thread.sleep((Long) message);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                unhandled(message);
            }
        }
    }
}
