package warden;

import org.objectweb.asm.ClassReader;

import java.lang.reflect.InvocationTargetException;
import java.net.URLClassLoader;
import java.security.SecureClassLoader;

public class ByteClassLoader {

    private static class Loader extends SecureClassLoader {

        public Loader(ClassLoader parent) {
            super(parent);
        }

    }

    public static Class<?> loadClass(ClassLoader parent, byte[] b) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        ClassReader reader = new ClassReader(b);
        String className = reader.getClassName().replaceAll("/", ".");
        //override classDefine (as it is protected) and define the class.
        Class<?> clazz = null;
        //TODO Better way to allow reloads
        //WARNING DO NOT USE THIS CODE IN A NON-TEST ENVIRONMENT
        //Always create a new classloader (ONLY FOR USE IN TESTING)
        ClassLoader loader = new Loader(parent);
        Class cls = Class.forName("java.lang.ClassLoader");
        java.lang.reflect.Method method =
                cls.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);

        // protected method invocaton
        method.setAccessible(true);
        try {
            Object[] args = new Object[]{className, b, 0, b.length};
            clazz = (Class) method.invoke(loader, args);
        } finally {
            method.setAccessible(false);
        }
        //Initialize
        Class.forName(className, true, loader);
        return clazz;
    }


}