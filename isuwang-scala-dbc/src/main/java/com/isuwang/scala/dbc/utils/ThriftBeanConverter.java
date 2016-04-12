package com.isuwang.scala.dbc.utils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.thrift.TEnum;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ThriftBeanConverter
 *
 * @author craneding
 * @date 2015年4月2日 下午4:36:15
 * @description Copyright (c) 2015, isuwang.com All Rights Reserved.
 */
public class ThriftBeanConverter {

    public static interface IBean {
        String getString();

        Long getLong();

        Integer getInteger();

        Byte getByte();

        Short getShort();

        Double getDouble();

        Float getFloat();

        Boolean getBoolean();

        BigDecimal getBigDecimal();

        Date getDate();

        Object getObject();

        String getCamel();
    }

    private static Map<String, Map<String, PropertyDescriptor>> cache = new ConcurrentHashMap<>();


    public static <T> T copy(Map<String, IBean> map, Class<T> clazz) {
        if (map == null || map.isEmpty())
            return null;

        try {
            T t = clazz.newInstance();

            Field[] fields = clazz.getDeclaredFields();
            Map<String, Field> fieldsMap = new HashMap<>();
            for (Field field : fields) {
                fieldsMap.put(field.getName(), field);
            }

            Method[] methods = clazz.getMethods();
            Map<String, Method> methodsMap = new HashMap<>();
            for (Method method : methods) {
                methodsMap.put(method.getName(), method);
            }

            Set<String> keys = map.keySet();

            for (String key : keys) {
                IBean iBean = map.get(key);

                Field field = fieldsMap.get(key);
                if (field == null) {
                    // try Camel

                    field = fieldsMap.get(iBean.getCamel());
                }

                if (field == null)
                    continue;

                Method readMethod = methodsMap.get("get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1));
                Method writeMethod = methodsMap.get("set" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1));

                if (readMethod == null || writeMethod == null)
                    continue;

                Object value = iBean.getObject();

                if (value != null) {
                    value = toValue(value.getClass(), value, field.getType());

                    writeMethod.invoke(t, value);
                }
            }

            return t;
        } catch (Exception e) {
            throw new ThriftBeanConverterException(e);
        }
    }

    /**
     * 值复制
     *
     * @param src
     * @param dest
     * @param setDefaultValForNull 是否为null值属性设置默认值（null=>0,null=>""）
     * @return
     * @throws ThriftBeanConverterException
     */
    public static <T> T copy(Object src, T dest, boolean setDefaultValForNull) throws ThriftBeanConverterException {
        if (src == null)
            return null;

        try {
            Class<? extends Object> destClass = dest.getClass();
            Map<String, PropertyDescriptor> srcDescriptors = getCachePropertyDescriptors(src.getClass());
            Map<String, PropertyDescriptor> destDescriptors = getCachePropertyDescriptors(destClass);

            Set<String> keys = destDescriptors.keySet();
            for (String key : keys) {
                PropertyDescriptor srcDescriptor = srcDescriptors.get(key);

                if (srcDescriptor == null)
                    continue;

                PropertyDescriptor destDescriptor = destDescriptors.get(key);

                Object value = srcDescriptor.getReadMethod().invoke(src);

                Class<?> propertyType = destDescriptor.getPropertyType();

                Method writeMethod = destDescriptor.getWriteMethod();
                if (writeMethod == null) {
                    String name = destDescriptor.getName();
                    try {
                        writeMethod = destClass.getMethod("set" + name.substring(0, 1).toUpperCase() + name.substring(1), destDescriptor.getPropertyType());

                        destDescriptor.setWriteMethod(writeMethod);
                    } catch (Exception e) {
                    }
                }
                if (writeMethod != null) {
                    //类型匹配
                    boolean matched = propertyType == srcDescriptor.getPropertyType();
                    if (!matched) {
                        if (value != null || setDefaultValForNull) {
                            value = toValue(srcDescriptor.getPropertyType(), value, propertyType);
                        }
                    }
                    //设置默认值
                    if (value == null && setDefaultValForNull) {
                        if (destDescriptor.getPropertyType() == Long.class || destDescriptor.getPropertyType() == Integer.class || destDescriptor.getPropertyType() == Short.class || destDescriptor.getPropertyType() == Double.class || destDescriptor.getPropertyType() == Float.class) {
                            value = 0;
                        } else if (destDescriptor.getPropertyType() == String.class) {
                            value = "";
                        } else if (destDescriptor.getPropertyType() == BigDecimal.class) {
                            value = new BigDecimal("0");
                        }
                    }

                    if (value != null) {
                        writeMethod.invoke(dest, value);
                    }
                }
            }

            return dest;
        } catch (Exception e) {
            throw new ThriftBeanConverterException(e);
        }
    }

    public static <T> T copy(Object src, T dest) throws ThriftBeanConverterException {
        return copy(src, dest, false);
    }

    public static <T> List<T> copy(List<?> srcs, Class<T> destClass, boolean setDefaultValForNull) {
        if (srcs == null)
            return new ArrayList<T>();

        List<T> dests = new ArrayList<T>();
        for (Object src : srcs) {
            dests.add(copy(src, destClass, setDefaultValForNull));
        }

        return dests;
    }

    public static <T> List<T> copy(List<?> srcs, Class<T> destClass) {
        return copy(srcs, destClass, false);
    }

    public static <T> T copy(Object src, Class<T> destClass, boolean setDefaultValForNull) throws ThriftBeanConverterException {
        if (src == null)
            return null;

        try {
            T dest = destClass.newInstance();
            copy(src, dest, setDefaultValForNull);
            return dest;
        } catch (Exception e) {
            throw new ThriftBeanConverterException(e);
        }
    }

    public static <T> T copy(Object src, Class<T> destClass) throws ThriftBeanConverterException {
        return copy(src, destClass, false);
    }

    /**
     * 把对象值为0的包装类型属性转为null
     *
     * @param bean
     * @param excludeFields 排除不处理的字段
     * @throws ThriftBeanConverterException
     */
    public static void zeroWrapPropertiesToNull(Object bean, String... excludeFields) throws ThriftBeanConverterException {
        try {
            Map<String, PropertyDescriptor> srcDescriptors = getCachePropertyDescriptors(bean.getClass());
            Set<String> keys = srcDescriptors.keySet();

            List<String> excludeFieldsList = null;
            if (excludeFields != null && excludeFields.length > 0 && StringUtils.isNotBlank(excludeFields[0])) {
                excludeFieldsList = Arrays.asList(excludeFields);
            }

            for (String key : keys) {
                PropertyDescriptor srcDescriptor = srcDescriptors.get(key);
                if (srcDescriptor == null) continue;
                if (excludeFieldsList != null && excludeFieldsList.contains(key)) continue;
                Object value = srcDescriptor.getReadMethod().invoke(bean);

                boolean isWrapType = srcDescriptor.getPropertyType() == Long.class || srcDescriptor.getPropertyType() == Integer.class || srcDescriptor.getPropertyType() == Short.class || srcDescriptor.getPropertyType() == Double.class || srcDescriptor.getPropertyType() == Float.class;
                if (isWrapType && value != null && Integer.parseInt(value.toString()) == 0) {
                    value = null;
                    Method writeMethod = srcDescriptor.getWriteMethod();
                    if (writeMethod != null) writeMethod.invoke(bean, value);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ThriftBeanConverterException(e);
        }
    }

    private static Object toValue(Class<?> srcPropertyType, Object value, Class<?> propertyType) {
        if (value instanceof TEnum) {
            if (propertyType == String.class) {
                value = ((Enum<?>) value).name();
            } else {
                value = ((TEnum) value).getValue();
            }
        }

        if (propertyType == BigDecimal.class) {
            value = (value == null) ? new BigDecimal("0") : new BigDecimal(value.toString());
        } else if (propertyType == byte.class || propertyType == Byte.class) {
            value = (value == null) ? Byte.valueOf("0") : Byte.valueOf(value.toString());
        } else if (propertyType == short.class || propertyType == Short.class) {
            value = (value == null) ? Short.valueOf("0") : Short.valueOf(value.toString());
        } else if (propertyType == int.class || propertyType == Integer.class) {
            if (srcPropertyType == Date.class) {
                value = (value == null) ? null : (int) ((Date) value).getTime();
            } else if (srcPropertyType == java.sql.Date.class) {
                value = (value == null) ? null : ((java.sql.Date) value).getTime();
            } else if (srcPropertyType == java.sql.Timestamp.class) {
                value = (value == null) ? null : ((java.sql.Timestamp) value).getTime();
            } else if (srcPropertyType == boolean.class || srcPropertyType == Boolean.class) {
                value = Boolean.parseBoolean(value.toString()) ? 1 : 0;
            } else if (srcPropertyType == Optional.class) {
                value = ((Optional) value).isPresent() ? ((Optional) value).get() : 0;
            } else {
                value = (value == null) ? Integer.valueOf("0") : Integer.valueOf(value.toString());
            }
        } else if (propertyType == long.class || propertyType == Long.class) {
            if (srcPropertyType == Date.class) {
                value = (value == null) ? null : ((Date) value).getTime();
            } else if (srcPropertyType == java.sql.Date.class) {
                value = (value == null) ? null : ((java.sql.Date) value).getTime();
            } else if (srcPropertyType == java.sql.Timestamp.class) {
                value = (value == null) ? null : ((java.sql.Timestamp) value).getTime();
            } else {
                value = (value == null) ? Long.valueOf("0") : Long.valueOf(value.toString());
            }
        } else if (propertyType == double.class || propertyType == Double.class) {
            value = (value == null) ? Double.valueOf("0") : Double.valueOf(value.toString());
        } else if (propertyType == Date.class) {
            if (value != null && (srcPropertyType == Long.class || srcPropertyType == Integer.class || srcPropertyType == long.class || srcPropertyType == int.class)) {
                Long val = Long.valueOf(value.toString());

                if (val.longValue() != 0)
                    value = new Date(val);
                else
                    value = null;
            } else if (srcPropertyType == Optional.class) {
                value = ((Optional) value).isPresent() ? ((Optional) value).get() : null;
            }
        } else if (propertyType == String.class && srcPropertyType != String.class) {
            if (srcPropertyType == Optional.class) {
                value = ((Optional) value).isPresent() ? ((Optional) value).get() : null;
            } else if (value != null) {
                value = value.toString();
            }
        } else if (propertyType == boolean.class || propertyType == Boolean.class) {
            if (value.toString().matches("[0|1]")) {
                value = "1".equals(value.toString());
            }
        } else if (propertyType.isEnum() && value != null) {
            Object[] enumConstants = propertyType.getEnumConstants();
            for (Object enumConstant : enumConstants) {
                if (value instanceof String) {
                    if (((Enum<?>) enumConstant).name().equals(value.toString())) {
                        value = enumConstant;

                        break;
                    }
                } else if (NumberUtils.isNumber(value.toString())) {
                    if (enumConstant instanceof TEnum) {
                        if (((TEnum) enumConstant).getValue() == NumberUtils.toInt(value.toString())) {
                            value = enumConstant;

                            break;
                        }
                    } else {
                        if (((Enum<?>) enumConstant).ordinal() == NumberUtils.toInt(value.toString())) {
                            value = enumConstant;

                            break;
                        }
                    }
                }
            }
        } else if (propertyType == List.class) {
            if (srcPropertyType == Optional.class)
                value = ((Optional) value).isPresent() ? ((Optional) value).get() : null;
        }
        return value;
    }

    public static Map<String, Object> beanToMap(Object bean) {
        Map<String, Object> returnMap = new HashMap<String, Object>();
        try {
            Map<String, PropertyDescriptor> descriptors = getCachePropertyDescriptors(bean.getClass());
            for (PropertyDescriptor descriptor : descriptors.values()) {
                String propertyName = descriptor.getName();
                Method readMethod = descriptor.getReadMethod();
                Object result = readMethod.invoke(bean, new Object[0]);
                if (result != null) {
                    returnMap.put(propertyName, result);
                }
            }
        } catch (Exception e) {
            throw new ThriftBeanConverterException(e);
        }


        return returnMap;

    }

    private static synchronized Map<String, PropertyDescriptor> getCachePropertyDescriptors(Class<?> clazz) throws IntrospectionException {
        String canonicalName = clazz.getCanonicalName();

        Map<String, PropertyDescriptor> map = cache.get(canonicalName);

        if (map == null) {
            map = new ConcurrentHashMap<>();

            BeanInfo srcBeanInfo = Introspector.getBeanInfo(clazz);

            PropertyDescriptor[] descriptors = srcBeanInfo.getPropertyDescriptors();
            for (PropertyDescriptor descriptor : descriptors) {
                Method readMethod = descriptor.getReadMethod();
                Method writeMethod = descriptor.getWriteMethod();

                String name = descriptor.getName();

                if (readMethod == null)
                    try {
                        readMethod = clazz.getMethod("get" + name.substring(0, 1).toUpperCase() + name.substring(1));

                        descriptor.setReadMethod(readMethod);
                    } catch (NoSuchMethodException | SecurityException e) {
                    }

                if (writeMethod == null)
                    try {
                        writeMethod = clazz.getMethod("set" + name.substring(0, 1).toUpperCase() + name.substring(1), descriptor.getPropertyType());

                        descriptor.setWriteMethod(writeMethod);
                    } catch (NoSuchMethodException | SecurityException e) {
                    }

                if (readMethod != null && writeMethod != null) {
                    map.put(descriptor.getName(), descriptor);
                }
            }

            cache.put(canonicalName, map);
        }

        return map;
    }


    public static class ThriftBeanConverterException extends RuntimeException {
        private static final long serialVersionUID = 152873897614690397L;

        public ThriftBeanConverterException(Throwable cause) {
            super(cause);
        }
    }

}
