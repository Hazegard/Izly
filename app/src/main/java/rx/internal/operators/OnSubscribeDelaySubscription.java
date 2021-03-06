package rx.internal.operators;

import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.Observable$OnSubscribe;
import rx.Scheduler;
import rx.Scheduler.Worker;
import rx.Subscriber;
import rx.functions.Action0;
import rx.observers.Subscribers;

public final class OnSubscribeDelaySubscription<T> implements Observable$OnSubscribe<T> {
    final Scheduler scheduler;
    final Observable<? extends T> source;
    final long time;
    final TimeUnit unit;

    public OnSubscribeDelaySubscription(Observable<? extends T> observable, long j, TimeUnit timeUnit, Scheduler scheduler) {
        this.source = observable;
        this.time = j;
        this.unit = timeUnit;
        this.scheduler = scheduler;
    }

    public final void call(final Subscriber<? super T> subscriber) {
        Worker createWorker = this.scheduler.createWorker();
        subscriber.add(createWorker);
        createWorker.schedule(new Action0() {
            public void call() {
                if (!subscriber.isUnsubscribed()) {
                    OnSubscribeDelaySubscription.this.source.unsafeSubscribe(Subscribers.wrap(subscriber));
                }
            }
        }, this.time, this.unit);
    }
}
