package com.technocratos;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {
    private static final String CONFIG_DB = "application.properties";
    private Properties props;

    public PropertiesLoader() {
        props = new Properties();
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream(CONFIG_DB)) {

            if (input == null) {
                throw new RuntimeException("Файл не найден: " + CONFIG_DB);
            }
            props.load(input);
            System.out.println(props.getProperty("database.url"));
        } catch (IOException e) {
            throw new RuntimeException("Ошибка загрузки файла конфигурации", e);
        }
    }

    public String getProperty(String key) {
        return props.getProperty(key);
    }
}

