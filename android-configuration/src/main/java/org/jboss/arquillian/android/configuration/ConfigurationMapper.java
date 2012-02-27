package org.jboss.arquillian.android.configuration;

import java.io.File;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;

public class ConfigurationMapper {

    /**
     * Maps Android configuration using Arquillian Descriptor file
     *
     * @param descriptor Arquillian Descriptor
     * @param configuration Configuration object
     * @param properties A map of name-value pairs
     * @return Configured configuration
     */
    public static <T> T fromArquillianDescriptor(ArquillianDescriptor descriptor, T configuration,
            Map<String, String> properties) {
        Validate.notNull(descriptor, "Descriptor must not be null");
        Validate.notNull(configuration, "Configuration must not be null");

        List<Field> fields = SecurityActions.getAccessableFields(configuration.getClass());
        for (Field f : fields) {
            if (properties.containsKey(f.getName())) {
                try {
                    f.set(configuration, convert(box(f.getType()), properties.get(f.getName())));
                } catch (Exception e) {
                    throw new RuntimeException("Could not map Android configuration from Arquillian Descriptor", e);
                }
            }
        }
        return configuration;

    }

    /**
     * A helper boxing method. Returns boxed class for a primitive class
     *
     * @param primitive A primitive class
     * @return Boxed class if class was primitive, unchanged class in other cases
     */
    private static Class<?> box(Class<?> primitive) {
        if (!primitive.isPrimitive()) {
            return primitive;
        }

        if (int.class.equals(primitive)) {
            return Integer.class;
        } else if (long.class.equals(primitive)) {
            return Long.class;
        } else if (float.class.equals(primitive)) {
            return Float.class;
        } else if (double.class.equals(primitive)) {
            return Double.class;
        } else if (short.class.equals(primitive)) {
            return Short.class;
        } else if (boolean.class.equals(primitive)) {
            return Boolean.class;
        } else if (char.class.equals(primitive)) {
            return Character.class;
        } else if (byte.class.equals(primitive)) {
            return Byte.class;
        }

        throw new IllegalArgumentException("Unknown primitive type " + primitive);
    }

    /**
     * A helper converting method.
     *
     * Converts string to a class of given type
     *
     * @param <T> Type of returned value
     * @param clazz Type of desired value
     * @param value String value to be converted
     * @return Value converted to a appropriate type
     */
    private static <T> T convert(Class<T> clazz, String value) {
        if (String.class.equals(clazz)) {
            return clazz.cast(value);
        } else if (Integer.class.equals(clazz)) {
            return clazz.cast(Integer.valueOf(value));
        } else if (Double.class.equals(clazz)) {
            return clazz.cast(Double.valueOf(value));
        } else if (Long.class.equals(clazz)) {
            return clazz.cast(Long.valueOf(value));
        } else if (Boolean.class.equals(clazz)) {
            return clazz.cast(Boolean.valueOf(value));
        } else if (URL.class.equals(clazz)) {
            try {
                return clazz.cast(new URI(value).toURL());
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("Unable to convert value " + value + " to URL", e);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Unable to convert value " + value + " to URL", e);
            }
        } else if (URI.class.equals(clazz)) {
            try {
                return clazz.cast(new URI(value));
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Unable to convert value " + value + " to URL", e);
            }
        } else if (File.class.equals(clazz)) {
            try {
                return clazz.cast(new File(value));
            } catch (NullPointerException e) {
                throw new IllegalArgumentException("Unable to convert value " + value + " to File", e);
            }

        }

        throw new IllegalArgumentException("Unable to convert value " + value + "to a class: " + clazz.getName());
    }
}
