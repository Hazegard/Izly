package retrofit2;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.CallAdapter.Factory;

public final class Retrofit {
    private final List<Factory> adapterFactories;
    private final HttpUrl baseUrl;
    private final Call.Factory callFactory;
    private final Executor callbackExecutor;
    private final List<Converter.Factory> converterFactories;
    private final Map<Method, ServiceMethod> serviceMethodCache = new LinkedHashMap();
    private final boolean validateEagerly;

    public static final class Builder {
        private List<Factory> adapterFactories;
        private HttpUrl baseUrl;
        private Call.Factory callFactory;
        private Executor callbackExecutor;
        private List<Converter.Factory> converterFactories;
        private Platform platform;
        private boolean validateEagerly;

        public Builder() {
            this(Platform.get());
        }

        Builder(Platform platform) {
            this.converterFactories = new ArrayList();
            this.adapterFactories = new ArrayList();
            this.platform = platform;
            this.converterFactories.add(new BuiltInConverters());
        }

        public final Builder addCallAdapterFactory(Factory factory) {
            this.adapterFactories.add(Utils.checkNotNull(factory, "factory == null"));
            return this;
        }

        public final Builder addConverterFactory(Converter.Factory factory) {
            this.converterFactories.add(Utils.checkNotNull(factory, "factory == null"));
            return this;
        }

        public final Builder baseUrl(String str) {
            Utils.checkNotNull(str, "baseUrl == null");
            HttpUrl parse = HttpUrl.parse(str);
            if (parse != null) {
                return baseUrl(parse);
            }
            throw new IllegalArgumentException("Illegal URL: " + str);
        }

        public final Builder baseUrl(HttpUrl httpUrl) {
            Utils.checkNotNull(httpUrl, "baseUrl == null");
            List pathSegments = httpUrl.pathSegments();
            if ("".equals(pathSegments.get(pathSegments.size() - 1))) {
                this.baseUrl = httpUrl;
                return this;
            }
            throw new IllegalArgumentException("baseUrl must end in /: " + httpUrl);
        }

        public final Retrofit build() {
            if (this.baseUrl == null) {
                throw new IllegalStateException("Base URL required.");
            }
            Call.Factory factory = this.callFactory;
            if (factory == null) {
                factory = new OkHttpClient();
            }
            Executor executor = this.callbackExecutor;
            if (executor == null) {
                executor = this.platform.defaultCallbackExecutor();
            }
            List arrayList = new ArrayList(this.adapterFactories);
            arrayList.add(this.platform.defaultCallAdapterFactory(executor));
            return new Retrofit(factory, this.baseUrl, new ArrayList(this.converterFactories), arrayList, executor, this.validateEagerly);
        }

        public final Builder callFactory(Call.Factory factory) {
            this.callFactory = (Call.Factory) Utils.checkNotNull(factory, "factory == null");
            return this;
        }

        public final Builder callbackExecutor(Executor executor) {
            this.callbackExecutor = (Executor) Utils.checkNotNull(executor, "executor == null");
            return this;
        }

        public final Builder client(OkHttpClient okHttpClient) {
            return callFactory((Call.Factory) Utils.checkNotNull(okHttpClient, "client == null"));
        }

        public final Builder validateEagerly(boolean z) {
            this.validateEagerly = z;
            return this;
        }
    }

    Retrofit(Call.Factory factory, HttpUrl httpUrl, List<Converter.Factory> list, List<Factory> list2, Executor executor, boolean z) {
        this.callFactory = factory;
        this.baseUrl = httpUrl;
        this.converterFactories = Collections.unmodifiableList(list);
        this.adapterFactories = Collections.unmodifiableList(list2);
        this.callbackExecutor = executor;
        this.validateEagerly = z;
    }

    private void eagerlyValidateMethods(Class<?> cls) {
        Platform platform = Platform.get();
        for (Method method : cls.getDeclaredMethods()) {
            if (!platform.isDefaultMethod(method)) {
                loadServiceMethod(method);
            }
        }
    }

    public final HttpUrl baseUrl() {
        return this.baseUrl;
    }

    public final CallAdapter<?> callAdapter(Type type, Annotation[] annotationArr) {
        return nextCallAdapter(null, type, annotationArr);
    }

    public final List<Factory> callAdapterFactories() {
        return this.adapterFactories;
    }

    public final Call.Factory callFactory() {
        return this.callFactory;
    }

    public final Executor callbackExecutor() {
        return this.callbackExecutor;
    }

    public final List<Converter.Factory> converterFactories() {
        return this.converterFactories;
    }

    public final <T> T create(final Class<T> cls) {
        Utils.validateServiceInterface(cls);
        if (this.validateEagerly) {
            eagerlyValidateMethods(cls);
        }
        return Proxy.newProxyInstance(cls.getClassLoader(), new Class[]{cls}, new InvocationHandler() {
            private final Platform platform = Platform.get();

            public Object invoke(Object obj, Method method, Object... objArr) throws Throwable {
                if (method.getDeclaringClass() == Object.class) {
                    return method.invoke(this, objArr);
                }
                if (this.platform.isDefaultMethod(method)) {
                    return this.platform.invokeDefaultMethod(method, cls, obj, objArr);
                }
                ServiceMethod loadServiceMethod = Retrofit.this.loadServiceMethod(method);
                return loadServiceMethod.callAdapter.adapt(new OkHttpCall(loadServiceMethod, objArr));
            }
        });
    }

    final ServiceMethod loadServiceMethod(Method method) {
        ServiceMethod serviceMethod;
        synchronized (this.serviceMethodCache) {
            serviceMethod = (ServiceMethod) this.serviceMethodCache.get(method);
            if (serviceMethod == null) {
                serviceMethod = new Builder(this, method).build();
                this.serviceMethodCache.put(method, serviceMethod);
            }
        }
        return serviceMethod;
    }

    public final CallAdapter<?> nextCallAdapter(Factory factory, Type type, Annotation[] annotationArr) {
        int i;
        Utils.checkNotNull(type, "returnType == null");
        Utils.checkNotNull(annotationArr, "annotations == null");
        int indexOf = this.adapterFactories.indexOf(factory) + 1;
        int size = this.adapterFactories.size();
        for (i = indexOf; i < size; i++) {
            CallAdapter<?> callAdapter = ((Factory) this.adapterFactories.get(i)).get(type, annotationArr, this);
            if (callAdapter != null) {
                return callAdapter;
            }
        }
        StringBuilder append = new StringBuilder("Could not locate call adapter for ").append(type).append(".\n");
        if (factory != null) {
            append.append("  Skipped:");
            for (i = 0; i < indexOf; i++) {
                append.append("\n   * ").append(((Factory) this.adapterFactories.get(i)).getClass().getName());
            }
            append.append('\n');
        }
        append.append("  Tried:");
        i = this.adapterFactories.size();
        while (indexOf < i) {
            append.append("\n   * ").append(((Factory) this.adapterFactories.get(indexOf)).getClass().getName());
            indexOf++;
        }
        throw new IllegalArgumentException(append.toString());
    }

    public final <T> Converter<T, RequestBody> nextRequestBodyConverter(Converter.Factory factory, Type type, Annotation[] annotationArr, Annotation[] annotationArr2) {
        int i;
        Utils.checkNotNull(type, "type == null");
        Utils.checkNotNull(annotationArr, "parameterAnnotations == null");
        Utils.checkNotNull(annotationArr2, "methodAnnotations == null");
        int indexOf = this.converterFactories.indexOf(factory) + 1;
        int size = this.converterFactories.size();
        for (i = indexOf; i < size; i++) {
            Converter<T, RequestBody> requestBodyConverter = ((Converter.Factory) this.converterFactories.get(i)).requestBodyConverter(type, annotationArr, annotationArr2, this);
            if (requestBodyConverter != null) {
                return requestBodyConverter;
            }
        }
        StringBuilder append = new StringBuilder("Could not locate RequestBody converter for ").append(type).append(".\n");
        if (factory != null) {
            append.append("  Skipped:");
            for (i = 0; i < indexOf; i++) {
                append.append("\n   * ").append(((Converter.Factory) this.converterFactories.get(i)).getClass().getName());
            }
            append.append('\n');
        }
        append.append("  Tried:");
        i = this.converterFactories.size();
        while (indexOf < i) {
            append.append("\n   * ").append(((Converter.Factory) this.converterFactories.get(indexOf)).getClass().getName());
            indexOf++;
        }
        throw new IllegalArgumentException(append.toString());
    }

    public final <T> Converter<ResponseBody, T> nextResponseBodyConverter(Converter.Factory factory, Type type, Annotation[] annotationArr) {
        int i;
        Utils.checkNotNull(type, "type == null");
        Utils.checkNotNull(annotationArr, "annotations == null");
        int indexOf = this.converterFactories.indexOf(factory) + 1;
        int size = this.converterFactories.size();
        for (i = indexOf; i < size; i++) {
            Converter<ResponseBody, T> responseBodyConverter = ((Converter.Factory) this.converterFactories.get(i)).responseBodyConverter(type, annotationArr, this);
            if (responseBodyConverter != null) {
                return responseBodyConverter;
            }
        }
        StringBuilder append = new StringBuilder("Could not locate ResponseBody converter for ").append(type).append(".\n");
        if (factory != null) {
            append.append("  Skipped:");
            for (i = 0; i < indexOf; i++) {
                append.append("\n   * ").append(((Converter.Factory) this.converterFactories.get(i)).getClass().getName());
            }
            append.append('\n');
        }
        append.append("  Tried:");
        i = this.converterFactories.size();
        while (indexOf < i) {
            append.append("\n   * ").append(((Converter.Factory) this.converterFactories.get(indexOf)).getClass().getName());
            indexOf++;
        }
        throw new IllegalArgumentException(append.toString());
    }

    public final <T> Converter<T, RequestBody> requestBodyConverter(Type type, Annotation[] annotationArr, Annotation[] annotationArr2) {
        return nextRequestBodyConverter(null, type, annotationArr, annotationArr2);
    }

    public final <T> Converter<ResponseBody, T> responseBodyConverter(Type type, Annotation[] annotationArr) {
        return nextResponseBodyConverter(null, type, annotationArr);
    }

    public final <T> Converter<T, String> stringConverter(Type type, Annotation[] annotationArr) {
        Utils.checkNotNull(type, "type == null");
        Utils.checkNotNull(annotationArr, "annotations == null");
        int size = this.converterFactories.size();
        for (int i = 0; i < size; i++) {
            Converter<T, String> stringConverter = ((Converter.Factory) this.converterFactories.get(i)).stringConverter(type, annotationArr, this);
            if (stringConverter != null) {
                return stringConverter;
            }
        }
        return ToStringConverter.INSTANCE;
    }
}
