package com.research.util;

import java.lang.reflect.Method;

public class ReflectUtil {
	public static Object callMethod(Class<?> cls, String str, Object obj, Class<?>[] clsArr, Object[] objArr) throws Exception{
        Method findMethod = findMethod(cls, str, clsArr);
        findMethod.setAccessible(true);
        return findMethod.invoke(obj, objArr);
    }

    public static Method findMethod(Class<?> cls, String str, Class<?>... clsArr) throws Exception {
        do {
            try {
                return cls.getDeclaredMethod(str, clsArr);
            } catch (NoSuchMethodException e) {
                cls = cls.getSuperclass();
                if (cls == Object.class) {
                    break;
                } else if (cls == null) {
                }
            }
        } while (cls == null);
		return null;
    }

}
