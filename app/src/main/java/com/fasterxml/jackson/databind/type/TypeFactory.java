package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.util.ArrayBuilders;
import com.fasterxml.jackson.databind.util.ClassUtil;
import com.fasterxml.jackson.databind.util.LRUMap;
import java.io.Serializable;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;

public final class TypeFactory implements Serializable {
    private static final Class<?> CLS_BOOL = Boolean.TYPE;
    private static final Class<?> CLS_CLASS = Class.class;
    private static final Class<?> CLS_COMPARABLE = Comparable.class;
    private static final Class<?> CLS_ENUM = Enum.class;
    private static final Class<?> CLS_INT = Integer.TYPE;
    private static final Class<?> CLS_LONG = Long.TYPE;
    private static final Class<?> CLS_OBJECT = Object.class;
    private static final Class<?> CLS_STRING = String.class;
    protected static final SimpleType CORE_TYPE_BOOL = new SimpleType(CLS_BOOL);
    protected static final SimpleType CORE_TYPE_CLASS = new SimpleType(CLS_CLASS);
    protected static final SimpleType CORE_TYPE_COMPARABLE = new SimpleType(CLS_COMPARABLE);
    protected static final SimpleType CORE_TYPE_ENUM = new SimpleType(CLS_ENUM);
    protected static final SimpleType CORE_TYPE_INT = new SimpleType(CLS_INT);
    protected static final SimpleType CORE_TYPE_LONG = new SimpleType(CLS_LONG);
    protected static final SimpleType CORE_TYPE_OBJECT = new SimpleType(CLS_OBJECT);
    protected static final SimpleType CORE_TYPE_STRING = new SimpleType(CLS_STRING);
    protected static final TypeBindings EMPTY_BINDINGS = TypeBindings.emptyBindings();
    private static final JavaType[] NO_TYPES = new JavaType[0];
    protected static final TypeFactory instance = new TypeFactory();
    private static final long serialVersionUID = 1;
    protected final ClassLoader _classLoader;
    protected final TypeModifier[] _modifiers;
    protected final TypeParser _parser;
    protected final LRUMap<Class<?>, JavaType> _typeCache;

    private TypeFactory() {
        this._typeCache = new LRUMap(16, 100);
        this._parser = new TypeParser(this);
        this._modifiers = null;
        this._classLoader = null;
    }

    protected TypeFactory(TypeParser typeParser, TypeModifier[] typeModifierArr) {
        this(typeParser, typeModifierArr, null);
    }

    protected TypeFactory(TypeParser typeParser, TypeModifier[] typeModifierArr, ClassLoader classLoader) {
        this._typeCache = new LRUMap(16, 100);
        this._parser = typeParser.withFactory(this);
        this._modifiers = typeModifierArr;
        this._classLoader = classLoader;
    }

    private JavaType _collectionType(Class<?> cls, TypeBindings typeBindings, JavaType javaType, JavaType[] javaTypeArr) {
        JavaType _unknownType;
        List typeParameters = typeBindings.getTypeParameters();
        if (typeParameters.isEmpty()) {
            _unknownType = _unknownType();
        } else if (typeParameters.size() == 1) {
            _unknownType = (JavaType) typeParameters.get(0);
        } else {
            throw new IllegalArgumentException("Strange Collection type " + cls.getName() + ": can not determine type parameters");
        }
        return CollectionType.construct(cls, typeBindings, javaType, javaTypeArr, _unknownType);
    }

    private JavaType _mapType(Class<?> cls, TypeBindings typeBindings, JavaType javaType, JavaType[] javaTypeArr) {
        JavaType javaType2;
        JavaType javaType3;
        if (cls == Properties.class) {
            javaType2 = CORE_TYPE_STRING;
            javaType3 = javaType2;
        } else {
            List typeParameters = typeBindings.getTypeParameters();
            switch (typeParameters.size()) {
                case 0:
                    javaType2 = _unknownType();
                    javaType3 = javaType2;
                    break;
                case 2:
                    javaType2 = (JavaType) typeParameters.get(1);
                    javaType3 = (JavaType) typeParameters.get(0);
                    break;
                default:
                    throw new IllegalArgumentException("Strange Map type " + cls.getName() + ": can not determine type parameters");
            }
        }
        return MapType.construct(cls, typeBindings, javaType, javaTypeArr, javaType3, javaType2);
    }

    private JavaType _referenceType(Class<?> cls, TypeBindings typeBindings, JavaType javaType, JavaType[] javaTypeArr) {
        JavaType _unknownType;
        List typeParameters = typeBindings.getTypeParameters();
        if (typeParameters.isEmpty()) {
            _unknownType = _unknownType();
        } else if (typeParameters.size() == 1) {
            _unknownType = (JavaType) typeParameters.get(0);
        } else {
            throw new IllegalArgumentException("Strange Reference type " + cls.getName() + ": can not determine type parameters");
        }
        return ReferenceType.construct(cls, typeBindings, javaType, javaTypeArr, _unknownType);
    }

    public static TypeFactory defaultInstance() {
        return instance;
    }

    public static Class<?> rawClass(Type type) {
        return type instanceof Class ? (Class) type : defaultInstance().constructType(type).getRawClass();
    }

    public static JavaType unknownType() {
        return defaultInstance()._unknownType();
    }

    protected final JavaType _constructSimple(Class<?> cls, TypeBindings typeBindings, JavaType javaType, JavaType[] javaTypeArr) {
        if (typeBindings.isEmpty()) {
            JavaType _findWellKnownSimple = _findWellKnownSimple(cls);
            if (_findWellKnownSimple != null) {
                return _findWellKnownSimple;
            }
        }
        return _newSimpleType(cls, typeBindings, javaType, javaTypeArr);
    }

    protected final Class<?> _findPrimitive(String str) {
        return "int".equals(str) ? Integer.TYPE : "long".equals(str) ? Long.TYPE : "float".equals(str) ? Float.TYPE : "double".equals(str) ? Double.TYPE : "boolean".equals(str) ? Boolean.TYPE : "byte".equals(str) ? Byte.TYPE : "char".equals(str) ? Character.TYPE : "short".equals(str) ? Short.TYPE : "void".equals(str) ? Void.TYPE : null;
    }

    protected final JavaType _findWellKnownSimple(Class<?> cls) {
        if (cls.isPrimitive()) {
            if (cls == CLS_BOOL) {
                return CORE_TYPE_BOOL;
            }
            if (cls == CLS_INT) {
                return CORE_TYPE_INT;
            }
            if (cls == CLS_LONG) {
                return CORE_TYPE_LONG;
            }
        } else if (cls == CLS_STRING) {
            return CORE_TYPE_STRING;
        } else {
            if (cls == CLS_OBJECT) {
                return CORE_TYPE_OBJECT;
            }
        }
        return null;
    }

    protected final JavaType _fromAny(ClassStack classStack, Type type, TypeBindings typeBindings) {
        JavaType _fromClass;
        if (type instanceof Class) {
            _fromClass = _fromClass(classStack, (Class) type, EMPTY_BINDINGS);
        } else if (type instanceof ParameterizedType) {
            _fromClass = _fromParamType(classStack, (ParameterizedType) type, typeBindings);
        } else if (type instanceof JavaType) {
            return (JavaType) type;
        } else {
            if (type instanceof GenericArrayType) {
                _fromClass = _fromArrayType(classStack, (GenericArrayType) type, typeBindings);
            } else if (type instanceof TypeVariable) {
                _fromClass = _fromVariable(classStack, (TypeVariable) type, typeBindings);
            } else if (type instanceof WildcardType) {
                _fromClass = _fromWildcard(classStack, (WildcardType) type, typeBindings);
            } else {
                throw new IllegalArgumentException("Unrecognized Type: " + (type == null ? "[null]" : type.toString()));
            }
        }
        if (this._modifiers == null) {
            return _fromClass;
        }
        TypeBindings bindings = _fromClass.getBindings();
        if (bindings == null) {
            bindings = EMPTY_BINDINGS;
        }
        TypeModifier[] typeModifierArr = this._modifiers;
        int length = typeModifierArr.length;
        int i = 0;
        while (i < length) {
            JavaType modifyType = typeModifierArr[i].modifyType(_fromClass, type, bindings, this);
            if (modifyType == null) {
                throw new IllegalStateException(String.format("TypeModifier %s (of type %s) return null for type %s", new Object[]{r7, r7.getClass().getName(), _fromClass}));
            }
            i++;
            _fromClass = modifyType;
        }
        return _fromClass;
    }

    protected final JavaType _fromArrayType(ClassStack classStack, GenericArrayType genericArrayType, TypeBindings typeBindings) {
        return ArrayType.construct(_fromAny(classStack, genericArrayType.getGenericComponentType(), typeBindings), typeBindings);
    }

    protected final JavaType _fromClass(ClassStack classStack, Class<?> cls, TypeBindings typeBindings) {
        JavaType _findWellKnownSimple = _findWellKnownSimple(cls);
        if (_findWellKnownSimple != null) {
            return _findWellKnownSimple;
        }
        JavaType javaType;
        ClassStack classStack2;
        Object obj = (typeBindings == null || typeBindings.isEmpty()) ? 1 : null;
        if (obj != null) {
            _findWellKnownSimple = (JavaType) this._typeCache.get(cls);
            if (_findWellKnownSimple != null) {
                return _findWellKnownSimple;
            }
            javaType = _findWellKnownSimple;
        } else {
            javaType = _findWellKnownSimple;
        }
        if (classStack == null) {
            classStack2 = new ClassStack(cls);
        } else {
            ClassStack find = classStack.find(cls);
            if (find != null) {
                _findWellKnownSimple = new ResolvedRecursiveType(cls, EMPTY_BINDINGS);
                find.addSelfReference(_findWellKnownSimple);
                return _findWellKnownSimple;
            }
            classStack2 = classStack.child(cls);
        }
        if (cls.isArray()) {
            _findWellKnownSimple = ArrayType.construct(_fromAny(classStack2, cls.getComponentType(), typeBindings), typeBindings);
        } else {
            JavaType javaType2;
            JavaType[] _resolveSuperInterfaces;
            if (cls.isInterface()) {
                javaType2 = null;
                _resolveSuperInterfaces = _resolveSuperInterfaces(classStack2, cls, typeBindings);
            } else {
                javaType2 = _resolveSuperClass(classStack2, cls, typeBindings);
                _resolveSuperInterfaces = _resolveSuperInterfaces(classStack2, cls, typeBindings);
            }
            _findWellKnownSimple = cls == Properties.class ? MapType.construct(cls, typeBindings, javaType2, _resolveSuperInterfaces, CORE_TYPE_STRING, CORE_TYPE_STRING) : javaType2 != null ? javaType2.refine(cls, typeBindings, javaType2, _resolveSuperInterfaces) : javaType;
            if (_findWellKnownSimple == null) {
                _findWellKnownSimple = _fromWellKnownClass(classStack2, cls, typeBindings, javaType2, _resolveSuperInterfaces);
                if (_findWellKnownSimple == null) {
                    _findWellKnownSimple = _fromWellKnownInterface(classStack2, cls, typeBindings, javaType2, _resolveSuperInterfaces);
                    if (_findWellKnownSimple == null) {
                        _findWellKnownSimple = _newSimpleType(cls, typeBindings, javaType2, _resolveSuperInterfaces);
                    }
                }
            }
        }
        classStack2.resolveSelfReferences(_findWellKnownSimple);
        if (obj == null) {
            return _findWellKnownSimple;
        }
        this._typeCache.putIfAbsent(cls, _findWellKnownSimple);
        return _findWellKnownSimple;
    }

    protected final JavaType _fromParamType(ClassStack classStack, ParameterizedType parameterizedType, TypeBindings typeBindings) {
        int i = 0;
        Class cls = (Class) parameterizedType.getRawType();
        if (cls == CLS_ENUM) {
            return CORE_TYPE_ENUM;
        }
        if (cls == CLS_COMPARABLE) {
            return CORE_TYPE_COMPARABLE;
        }
        if (cls == CLS_CLASS) {
            return CORE_TYPE_CLASS;
        }
        TypeBindings typeBindings2;
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        int length = actualTypeArguments == null ? 0 : actualTypeArguments.length;
        if (length == 0) {
            typeBindings2 = EMPTY_BINDINGS;
        } else {
            JavaType[] javaTypeArr = new JavaType[length];
            while (i < length) {
                javaTypeArr[i] = _fromAny(classStack, actualTypeArguments[i], typeBindings);
                i++;
            }
            typeBindings2 = TypeBindings.create(cls, javaTypeArr);
        }
        return _fromClass(classStack, cls, typeBindings2);
    }

    protected final JavaType _fromVariable(ClassStack classStack, TypeVariable<?> typeVariable, TypeBindings typeBindings) {
        String name = typeVariable.getName();
        JavaType findBoundType = typeBindings.findBoundType(name);
        if (findBoundType != null) {
            return findBoundType;
        }
        if (typeBindings.hasUnbound(name)) {
            return CORE_TYPE_OBJECT;
        }
        return _fromAny(classStack, typeVariable.getBounds()[0], typeBindings.withUnboundVariable(name));
    }

    protected final JavaType _fromWellKnownClass(ClassStack classStack, Class<?> cls, TypeBindings typeBindings, JavaType javaType, JavaType[] javaTypeArr) {
        return cls == Map.class ? _mapType(cls, typeBindings, javaType, javaTypeArr) : cls == Collection.class ? _collectionType(cls, typeBindings, javaType, javaTypeArr) : cls == AtomicReference.class ? _referenceType(cls, typeBindings, javaType, javaTypeArr) : null;
    }

    protected final JavaType _fromWellKnownInterface(ClassStack classStack, Class<?> cls, TypeBindings typeBindings, JavaType javaType, JavaType[] javaTypeArr) {
        for (JavaType refine : javaTypeArr) {
            JavaType refine2 = refine2.refine(cls, typeBindings, javaType, javaTypeArr);
            if (refine2 != null) {
                return refine2;
            }
        }
        return null;
    }

    protected final JavaType _fromWildcard(ClassStack classStack, WildcardType wildcardType, TypeBindings typeBindings) {
        return _fromAny(classStack, wildcardType.getUpperBounds()[0], typeBindings);
    }

    protected final JavaType _newSimpleType(Class<?> cls, TypeBindings typeBindings, JavaType javaType, JavaType[] javaTypeArr) {
        return new SimpleType(cls, typeBindings, javaType, javaTypeArr);
    }

    protected final JavaType _resolveSuperClass(ClassStack classStack, Class<?> cls, TypeBindings typeBindings) {
        Type genericSuperclass = ClassUtil.getGenericSuperclass(cls);
        return genericSuperclass == null ? null : _fromAny(classStack, genericSuperclass, typeBindings);
    }

    protected final JavaType[] _resolveSuperInterfaces(ClassStack classStack, Class<?> cls, TypeBindings typeBindings) {
        Type[] genericInterfaces = ClassUtil.getGenericInterfaces(cls);
        if (genericInterfaces == null || genericInterfaces.length == 0) {
            return NO_TYPES;
        }
        int length = genericInterfaces.length;
        JavaType[] javaTypeArr = new JavaType[length];
        for (int i = 0; i < length; i++) {
            javaTypeArr[i] = _fromAny(classStack, genericInterfaces[i], typeBindings);
        }
        return javaTypeArr;
    }

    protected final JavaType _unknownType() {
        return CORE_TYPE_OBJECT;
    }

    protected final Class<?> classForName(String str) throws ClassNotFoundException {
        return Class.forName(str);
    }

    protected final Class<?> classForName(String str, boolean z, ClassLoader classLoader) throws ClassNotFoundException {
        return Class.forName(str, true, classLoader);
    }

    public final void clearCache() {
        this._typeCache.clear();
    }

    public final ArrayType constructArrayType(JavaType javaType) {
        return ArrayType.construct(javaType, null);
    }

    public final ArrayType constructArrayType(Class<?> cls) {
        return ArrayType.construct(_fromAny(null, cls, null), null);
    }

    public final CollectionLikeType constructCollectionLikeType(Class<?> cls, JavaType javaType) {
        JavaType _fromClass = _fromClass(null, cls, TypeBindings.createIfNeeded((Class) cls, javaType));
        return _fromClass instanceof CollectionLikeType ? (CollectionLikeType) _fromClass : CollectionLikeType.upgradeFrom(_fromClass, javaType);
    }

    public final CollectionLikeType constructCollectionLikeType(Class<?> cls, Class<?> cls2) {
        return constructCollectionLikeType((Class) cls, _fromClass(null, cls2, EMPTY_BINDINGS));
    }

    public final CollectionType constructCollectionType(Class<? extends Collection> cls, JavaType javaType) {
        return (CollectionType) _fromClass(null, cls, TypeBindings.create((Class) cls, javaType));
    }

    public final CollectionType constructCollectionType(Class<? extends Collection> cls, Class<?> cls2) {
        return constructCollectionType((Class) cls, _fromClass(null, cls2, EMPTY_BINDINGS));
    }

    public final JavaType constructFromCanonical(String str) throws IllegalArgumentException {
        return this._parser.parse(str);
    }

    public final JavaType constructGeneralizedType(JavaType javaType, Class<?> cls) {
        Class<?> rawClass = javaType.getRawClass();
        if (rawClass == cls) {
            return javaType;
        }
        JavaType findSuperType = javaType.findSuperType(cls);
        if (findSuperType != null) {
            return findSuperType;
        }
        if (cls.isAssignableFrom(rawClass)) {
            throw new IllegalArgumentException(String.format("Internal error: class %s not included as super-type for %s", new Object[]{cls.getName(), javaType}));
        }
        throw new IllegalArgumentException(String.format("Class %s not a super-type of %s", new Object[]{cls.getName(), javaType}));
    }

    public final MapLikeType constructMapLikeType(Class<?> cls, JavaType javaType, JavaType javaType2) {
        JavaType _fromClass = _fromClass(null, cls, TypeBindings.createIfNeeded((Class) cls, new JavaType[]{javaType, javaType2}));
        return _fromClass instanceof MapLikeType ? (MapLikeType) _fromClass : MapLikeType.upgradeFrom(_fromClass, javaType, javaType2);
    }

    public final MapLikeType constructMapLikeType(Class<?> cls, Class<?> cls2, Class<?> cls3) {
        return constructMapLikeType((Class) cls, _fromClass(null, cls2, EMPTY_BINDINGS), _fromClass(null, cls3, EMPTY_BINDINGS));
    }

    public final MapType constructMapType(Class<? extends Map> cls, JavaType javaType, JavaType javaType2) {
        return (MapType) _fromClass(null, cls, TypeBindings.create((Class) cls, new JavaType[]{javaType, javaType2}));
    }

    public final MapType constructMapType(Class<? extends Map> cls, Class<?> cls2, Class<?> cls3) {
        JavaType javaType;
        JavaType javaType2;
        if (cls == Properties.class) {
            javaType = CORE_TYPE_STRING;
            javaType2 = javaType;
        } else {
            javaType = _fromClass(null, cls2, EMPTY_BINDINGS);
            javaType2 = _fromClass(null, cls3, EMPTY_BINDINGS);
        }
        return constructMapType((Class) cls, javaType, javaType2);
    }

    public final JavaType constructParametricType(Class<?> cls, JavaType... javaTypeArr) {
        return _fromClass(null, cls, TypeBindings.create((Class) cls, javaTypeArr));
    }

    public final JavaType constructParametricType(Class<?> cls, Class<?>... clsArr) {
        int length = clsArr.length;
        JavaType[] javaTypeArr = new JavaType[length];
        for (int i = 0; i < length; i++) {
            javaTypeArr[i] = _fromClass(null, clsArr[i], null);
        }
        return constructParametricType((Class) cls, javaTypeArr);
    }

    public final JavaType constructParametrizedType(Class<?> cls, Class<?> cls2, JavaType... javaTypeArr) {
        return constructParametricType((Class) cls, javaTypeArr);
    }

    public final JavaType constructParametrizedType(Class<?> cls, Class<?> cls2, Class<?>... clsArr) {
        return constructParametricType((Class) cls, (Class[]) clsArr);
    }

    public final CollectionLikeType constructRawCollectionLikeType(Class<?> cls) {
        return constructCollectionLikeType((Class) cls, unknownType());
    }

    public final CollectionType constructRawCollectionType(Class<? extends Collection> cls) {
        return constructCollectionType((Class) cls, unknownType());
    }

    public final MapLikeType constructRawMapLikeType(Class<?> cls) {
        return constructMapLikeType((Class) cls, unknownType(), unknownType());
    }

    public final MapType constructRawMapType(Class<? extends Map> cls) {
        return constructMapType((Class) cls, unknownType(), unknownType());
    }

    public final JavaType constructReferenceType(Class<?> cls, JavaType javaType) {
        return ReferenceType.construct(cls, null, null, null, javaType);
    }

    @Deprecated
    public final JavaType constructSimpleType(Class<?> cls, Class<?> cls2, JavaType[] javaTypeArr) {
        return constructSimpleType(cls, javaTypeArr);
    }

    public final JavaType constructSimpleType(Class<?> cls, JavaType[] javaTypeArr) {
        return _fromClass(null, cls, TypeBindings.create((Class) cls, javaTypeArr));
    }

    public final JavaType constructSpecializedType(JavaType javaType, Class<?> cls) {
        Class<?> rawClass = javaType.getRawClass();
        if (rawClass == cls) {
            return javaType;
        }
        JavaType _fromClass;
        if (rawClass == Object.class) {
            _fromClass = _fromClass(null, cls, TypeBindings.emptyBindings());
        } else if (!rawClass.isAssignableFrom(cls)) {
            throw new IllegalArgumentException(String.format("Class %s not subtype of %s", new Object[]{cls.getName(), javaType}));
        } else if (javaType.getBindings().isEmpty()) {
            _fromClass = _fromClass(null, cls, TypeBindings.emptyBindings());
        } else {
            if (javaType.isContainerType()) {
                if (javaType.isMapLikeType()) {
                    if (cls == HashMap.class || cls == LinkedHashMap.class || cls == EnumMap.class || cls == TreeMap.class) {
                        _fromClass = _fromClass(null, cls, TypeBindings.create(cls, javaType.getKeyType(), javaType.getContentType()));
                    }
                } else if (javaType.isCollectionLikeType()) {
                    if (cls == ArrayList.class || cls == LinkedList.class || cls == HashSet.class || cls == TreeSet.class) {
                        _fromClass = _fromClass(null, cls, TypeBindings.create((Class) cls, javaType.getContentType()));
                    } else if (rawClass == EnumSet.class) {
                        return javaType;
                    }
                }
            }
            if (cls.getTypeParameters().length == 0) {
                _fromClass = _fromClass(null, cls, TypeBindings.emptyBindings());
            } else {
                if (javaType.isInterface()) {
                    _fromClass = javaType.refine(cls, TypeBindings.emptyBindings(), null, new JavaType[]{javaType});
                } else {
                    _fromClass = javaType.refine(cls, TypeBindings.emptyBindings(), javaType, NO_TYPES);
                }
                if (_fromClass == null) {
                    _fromClass = _fromClass(null, cls, TypeBindings.emptyBindings());
                }
            }
        }
        return _fromClass;
    }

    public final JavaType constructType(TypeReference<?> typeReference) {
        return _fromAny(null, typeReference.getType(), EMPTY_BINDINGS);
    }

    public final JavaType constructType(Type type) {
        return _fromAny(null, type, EMPTY_BINDINGS);
    }

    @Deprecated
    public final JavaType constructType(Type type, JavaType javaType) {
        return _fromAny(null, type, javaType == null ? TypeBindings.emptyBindings() : javaType.getBindings());
    }

    public final JavaType constructType(Type type, TypeBindings typeBindings) {
        return _fromAny(null, type, typeBindings);
    }

    @Deprecated
    public final JavaType constructType(Type type, Class<?> cls) {
        return _fromAny(null, type, cls == null ? TypeBindings.emptyBindings() : constructType((Type) cls).getBindings());
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final java.lang.Class<?> findClass(java.lang.String r4) throws java.lang.ClassNotFoundException {
        /*
        r3 = this;
        r0 = 46;
        r0 = r4.indexOf(r0);
        if (r0 >= 0) goto L_0x000f;
    L_0x0008:
        r0 = r3._findPrimitive(r4);
        if (r0 == 0) goto L_0x000f;
    L_0x000e:
        return r0;
    L_0x000f:
        r0 = 0;
        r1 = r3.getClassLoader();
        if (r1 != 0) goto L_0x001e;
    L_0x0016:
        r1 = java.lang.Thread.currentThread();
        r1 = r1.getContextClassLoader();
    L_0x001e:
        if (r1 == 0) goto L_0x002b;
    L_0x0020:
        r0 = 1;
        r0 = r3.classForName(r4, r0, r1);	 Catch:{ Exception -> 0x0026 }
        goto L_0x000e;
    L_0x0026:
        r0 = move-exception;
        r0 = com.fasterxml.jackson.databind.util.ClassUtil.getRootCause(r0);
    L_0x002b:
        r0 = r3.classForName(r4);	 Catch:{ Exception -> 0x0030 }
        goto L_0x000e;
    L_0x0030:
        r1 = move-exception;
        if (r0 != 0) goto L_0x0037;
    L_0x0033:
        r0 = com.fasterxml.jackson.databind.util.ClassUtil.getRootCause(r1);
    L_0x0037:
        r1 = r0 instanceof java.lang.RuntimeException;
        if (r1 == 0) goto L_0x003e;
    L_0x003b:
        r0 = (java.lang.RuntimeException) r0;
        throw r0;
    L_0x003e:
        r1 = new java.lang.ClassNotFoundException;
        r2 = r0.getMessage();
        r1.<init>(r2, r0);
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.fasterxml.jackson.databind.type.TypeFactory.findClass(java.lang.String):java.lang.Class<?>");
    }

    public final JavaType[] findTypeParameters(JavaType javaType, Class<?> cls) {
        JavaType findSuperType = javaType.findSuperType(cls);
        return findSuperType == null ? NO_TYPES : findSuperType.getBindings().typeParameterArray();
    }

    @Deprecated
    public final JavaType[] findTypeParameters(Class<?> cls, Class<?> cls2) {
        return findTypeParameters(constructType((Type) cls), (Class) cls2);
    }

    @Deprecated
    public final JavaType[] findTypeParameters(Class<?> cls, Class<?> cls2, TypeBindings typeBindings) {
        return findTypeParameters(constructType((Type) cls, typeBindings), (Class) cls2);
    }

    public final ClassLoader getClassLoader() {
        return this._classLoader;
    }

    public final JavaType moreSpecificType(JavaType javaType, JavaType javaType2) {
        if (javaType == null) {
            return javaType2;
        }
        if (javaType2 == null) {
            return javaType;
        }
        Class rawClass = javaType.getRawClass();
        Class rawClass2 = javaType2.getRawClass();
        return (rawClass == rawClass2 || !rawClass.isAssignableFrom(rawClass2)) ? javaType : javaType2;
    }

    public final JavaType uncheckedSimpleType(Class<?> cls) {
        return _constructSimple(cls, EMPTY_BINDINGS, null, null);
    }

    public final TypeFactory withClassLoader(ClassLoader classLoader) {
        return new TypeFactory(this._parser, this._modifiers, classLoader);
    }

    public final TypeFactory withModifier(TypeModifier typeModifier) {
        if (typeModifier == null) {
            return new TypeFactory(this._parser, this._modifiers, this._classLoader);
        }
        if (this._modifiers != null) {
            return new TypeFactory(this._parser, (TypeModifier[]) ArrayBuilders.insertInListNoDup(this._modifiers, typeModifier), this._classLoader);
        }
        return new TypeFactory(this._parser, new TypeModifier[]{typeModifier}, this._classLoader);
    }
}
