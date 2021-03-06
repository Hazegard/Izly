package rx.internal.operators;

import java.util.NoSuchElementException;
import rx.Observable$Operator;
import rx.Subscriber;
import rx.internal.producers.SingleProducer;
import rx.internal.util.RxJavaPluginUtils;

public final class OperatorSingle<T> implements Observable$Operator<T, T> {
    private final T defaultValue;
    private final boolean hasDefaultValue;

    static class Holder {
        static final OperatorSingle<?> INSTANCE = new OperatorSingle();

        private Holder() {
        }
    }

    static final class ParentSubscriber<T> extends Subscriber<T> {
        private final Subscriber<? super T> child;
        private final T defaultValue;
        private final boolean hasDefaultValue;
        private boolean hasTooManyElements;
        private boolean isNonEmpty;
        private T value;

        ParentSubscriber(Subscriber<? super T> subscriber, boolean z, T t) {
            this.child = subscriber;
            this.hasDefaultValue = z;
            this.defaultValue = t;
            request(2);
        }

        public final void onCompleted() {
            if (!this.hasTooManyElements) {
                if (this.isNonEmpty) {
                    this.child.setProducer(new SingleProducer(this.child, this.value));
                } else if (this.hasDefaultValue) {
                    this.child.setProducer(new SingleProducer(this.child, this.defaultValue));
                } else {
                    this.child.onError(new NoSuchElementException("Sequence contains no elements"));
                }
            }
        }

        public final void onError(Throwable th) {
            if (this.hasTooManyElements) {
                RxJavaPluginUtils.handleException(th);
            } else {
                this.child.onError(th);
            }
        }

        public final void onNext(T t) {
            if (!this.hasTooManyElements) {
                if (this.isNonEmpty) {
                    this.hasTooManyElements = true;
                    this.child.onError(new IllegalArgumentException("Sequence contains too many elements"));
                    unsubscribe();
                    return;
                }
                this.value = t;
                this.isNonEmpty = true;
            }
        }
    }

    OperatorSingle() {
        this(false, null);
    }

    public OperatorSingle(T t) {
        this(true, t);
    }

    private OperatorSingle(boolean z, T t) {
        this.hasDefaultValue = z;
        this.defaultValue = t;
    }

    public static <T> OperatorSingle<T> instance() {
        return Holder.INSTANCE;
    }

    public final Subscriber<? super T> call(Subscriber<? super T> subscriber) {
        ParentSubscriber parentSubscriber = new ParentSubscriber(subscriber, this.hasDefaultValue, this.defaultValue);
        subscriber.add(parentSubscriber);
        return parentSubscriber;
    }
}
