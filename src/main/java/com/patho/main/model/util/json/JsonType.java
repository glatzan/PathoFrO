package com.patho.main.model.util.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.internal.SessionImpl;
import org.hibernate.usertype.UserType;

import java.io.*;
import java.lang.reflect.ParameterizedType;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public abstract class JsonType<T> implements UserType, Serializable {

    private static final int[] SQL_TYPES = new int[]{Types.JAVA_OBJECT};
    private static Boolean jsonSupportCache = null;

    private static final ThreadLocal<ObjectMapper> OBJECT_MAPPER_CACHE = new ThreadLocal<>();

    @Override
    public int[] sqlTypes() {
        return SQL_TYPES;
    }

    @Override
    public Class<T> returnedClass() {
        return (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    @Override
    public Object nullSafeGet(final ResultSet rs, final String[] names, SharedSessionContractImplementor sharedSessionContractImplementor,
                              final Object owner) throws SQLException {
        final String cellContent = rs.getString(names[0]);
        if (cellContent == null) {
            return null;
        }
        try {
            return getObjectMapper().readValue(cellContent.getBytes(StandardCharsets.UTF_8), returnedClass());
        } catch (final Exception ex) {
            throw new HibernateException("Failed to convert value of ${names[0]}: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void nullSafeSet(final PreparedStatement ps, final Object value, final int idx,
                            SharedSessionContractImplementor sharedSessionContractImplementor) throws SQLException {
        boolean jsonSupported = isJsonSupported(sharedSessionContractImplementor);
        if (value == null) {
            ps.setNull(idx, jsonSupported ? Types.OTHER : Types.VARCHAR);
            return;
        }
        try {
            final StringWriter w = new StringWriter();
            getObjectMapper().writeValue(w, value);
            w.flush();
            if (jsonSupported) {
                ps.setObject(idx, w.toString(), Types.OTHER);
            } else {
                ps.setString(idx, w.toString());
            }
        } catch (final Exception ex) {
            throw new HibernateException("Failed to convert Object of index $idx to String: " + ex.getMessage(), ex);
        }
    }

    @Override
    public Object deepCopy(final Object value) {
        try {
            // use serialization to create a deep copy
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(value);
            oos.flush();
            oos.close();
            bos.close();

            ByteArrayInputStream bais = new ByteArrayInputStream(bos.toByteArray());
            return new ObjectInputStream(bais).readObject();
        } catch (ClassNotFoundException | IOException ex) {
            throw new HibernateException(ex);
        }
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Serializable disassemble(final Object value) {
        return (Serializable) this.deepCopy(value);
    }

    @Override
    public Object assemble(final Serializable cached, final Object owner) {
        return this.deepCopy(cached);
    }

    @Override
    public Object replace(final Object original, final Object target, final Object owner) {
        return this.deepCopy(original);
    }

    @Override
    public boolean equals(final Object obj1, final Object obj2) {
        if (obj1 == null) {
            return obj2 == null;
        }
        return obj1.equals(obj2);
    }

    @Override
    public int hashCode(final Object obj) {
        return obj.hashCode();
    }

    private static ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = OBJECT_MAPPER_CACHE.get();
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
            objectMapper.registerModules(
                    new Jdk8Module(),
                    new JavaTimeModule(),
                    offsetDateTimeModule());
            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

            OBJECT_MAPPER_CACHE.set(objectMapper);
        }
        return objectMapper;
    }

    private static synchronized boolean isJsonSupported(SharedSessionContractImplementor sharedSessionContractImplementor) {
        if (jsonSupportCache == null) {
            jsonSupportCache = String
                    .valueOf(((SessionImpl) sharedSessionContractImplementor).getSessionFactory().getProperties().get("hibernate.dialect"))
                    .startsWith("org.hibernate.dialect.PostgreSQL");
        }
        return jsonSupportCache.booleanValue();
    }

    private static Module offsetDateTimeModule() {
        return new SimpleModule().addDeserializer(OffsetDateTime.class, new JsonDeserializer<OffsetDateTime>() {

            @Override
            public OffsetDateTime deserialize(JsonParser jsonParser,
                                              DeserializationContext deserializationContext) throws IOException {
                return OffsetDateTime.parse(jsonParser.getValueAsString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            }
        });
    }
}