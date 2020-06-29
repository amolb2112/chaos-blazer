package com.cb.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Utils to load the yaml configuration
 *
 */
public class YamlUtils {
    private YamlUtils() { }

    public static <T> T load(String filename, Class<T> clazz) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        return mapper.readValue(YamlUtils.class.getClassLoader().getResource(filename), clazz);
    }

    public static <T> T load(InputStream inputStream, Class<T> clazz) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        return mapper.readValue(inputStream, clazz);
    }
}
