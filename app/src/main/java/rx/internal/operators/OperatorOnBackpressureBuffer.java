package rx.internal.operators;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import rx.BackpressureOverflow;
import rx.BackpressureOverflow.Strategy;
import rx.Observable$Operator;
import rx.Producer;
import rx.Subscriber;
import rx.exceptions.Exceptions;
import rx.functions.Action0;
import rx.internal.util.BackpressureDrainManager;
import rx.internal.util.BackpressureDrainManager.BackpressureQueueCallback;

public class OperatorOnBackpressureBuffer<T> implements Observable$Operator<T, T> {
    private final Long capacity;
    private final Action0 onOverflow;
    private final Strategy overflowStrategy;

    static final class BufferSubscriber<T> extends Subscriber<T> implements BackpressureQueueCallback {
        private final AtomicLong capacity;
        private final Subscriber<? super T> child;
        private final BackpressureDrainManager manager;
        private final NotificationLite<T> on = NotificationLite.instance();
        private final Action0 onOverflow;
        private final Strategy overflowStrategy;
        private final ConcurrentLinkedQueue<Object> queue = new ConcurrentLinkedQueue();
        private final AtomicBoolean saturated = new AtomicBoolean(false);

        public BufferSubscriber(Subscriber<? super T> subscriber, Long l, Action0 action0, Strategy strategy) {
            this.child = subscriber;
            this.capacity = l != null ? new AtomicLong(l.longValue()) : null;
            this.onOverflow = action0;
            this.manager = new BackpressureDrainManager(this);
            this.overflowStrategy = strategy;
        }

        private boolean assertCapacity() {
            if (this.capacity == null) {
                return true;
            }
            long j;
            do {
                j = this.capacity.get();
                if (j <= 0) {
                    boolean z;
                    try {
                        z = this.overflowStrategy.mayAttemptDrop() && poll() != null;
                    } catch (Throwable e) {
                        if (this.saturated.compareAndSet(false, true)) {
                            unsubscribe();
                            this.child.onError(e);
                        }
                        z = false;
                    }
                    if (this.onOverflow != null) {
                        try {
                            this.onOverflow.call();
                        } catch (Throwable e2) {
                            Exceptions.throwIfFatal(e2);
                            this.manager.terminateAndDrain(e2);
                            return false;
                        }
                    }
                    if (!z) {
                        return false;
                    }
                }
            } while (!this.capacity.compareAndSet(j, j - 1));
            return true;
        }

        public final boolean accept(Object obj) {
            return this.on.accept(this.child, obj);
        }

        public final void complete(Throwable th) {
            if (th != null) {
                this.child.onError(th);
            } else {
                this.child.onCompleted();
            }
        }

        protected final Producer manager() {
            return this.manager;
        }

        public final void onCompleted() {
            if (!this.saturated.get()) {
                this.manager.terminateAndDrain();
            }
        }

        public final void onError(Throwable th) {
            if (!this.saturated.get()) {
                this.manager.terminateAndDrain(th);
            }
        }

        public final void onNext(T t) {
            if (assertCapacity()) {
                this.queue.offer(this.on.next(t));
                this.manager.drain();
            }
        }

        public final void onStart() {
            request(Long.MAX_VALUE);
        }

        public final Object peek() {
            return this.queue.peek();
        }

        public final Object poll() {
            Object poll = this.queue.poll();
            if (!(this.capacity == null || poll == null)) {
                this.capacity.incrementAndGet();
            }
            return poll;
        }
    }

    static class Holder {
        static final OperatorOnBackpressureBuffer<?> INSTANCE = new OperatorOnBackpressureBuffer();

        private Holder() {
        }
    }

    OperatorOnBackpressureBuffer() {
        this.capacity = null;
        this.onOverflow = null;
        this.overflowStrategy = BackpressureOverflow.ON_OVERFLOW_DEFAULT;
    }

    public OperatorOnBackpressureBuffer(long j) {
        this(j, null, BackpressureOverflow.ON_OVERFLOW_DEFAULT);
    }

    public OperatorOnBackpressureBuffer(long j, Action0 action0) {
        this(j, action0, BackpressureOverflow.ON_OVERFLOW_DEFAULT);
    }

    public OperatorOnBackpressureBuffer(long j, Action0 action0, Strategy strategy) {
        if (j <= 0) {
            throw new IllegalArgumentException("Buffer capacity must be > 0");
        } else if (strategy == null) {
            throw new NullPointerException("The BackpressureOverflow strategy must not be null");
        } else {
            this.capacity = Long.valueOf(j);
            this.onOverflow = action0;
            this.overflowStrategy = strategy;
        }
    }

    public static <T> OperatorOnBackpressureBuffer<T> instance() {
        return Holder.INSTANCE;
    }

    public Subscriber<? super T> call(Subscriber<? super T> subscriber) {
        BufferSubscriber bufferSubscriber = new BufferSubscriber(subscriber, this.capacity, this.onOverflow, this.overflowStrategy);
        subscriber.add(bufferSubscriber);
        subscriber.setProducer(bufferSubscriber.manager());
        return bufferSubscriber;
    }
}
