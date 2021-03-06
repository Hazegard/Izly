package rx.internal.operators;

import rx.Observable$Operator;
import rx.Subscriber;
import rx.observers.SerializedSubscriber;

public final class OperatorSerialize<T> implements Observable$Operator<T, T> {

    static final class Holder {
        static final OperatorSerialize<Object> INSTANCE = new OperatorSerialize();

        private Holder() {
        }
    }

    OperatorSerialize() {
    }

    public static <T> OperatorSerialize<T> instance() {
        return Holder.INSTANCE;
    }

    public final Subscriber<? super T> call(final Subscriber<? super T> subscriber) {
        return new SerializedSubscriber(new Subscriber<T>(subscriber) {
            public void onCompleted() {
                subscriber.onCompleted();
            }

            public void onError(Throwable th) {
                subscriber.onError(th);
            }

            public void onNext(T t) {
                subscriber.onNext(t);
            }
        });
    }
}
