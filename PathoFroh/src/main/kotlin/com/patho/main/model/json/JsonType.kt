package com.patho.main.model.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.hibernate.HibernateException
import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.internal.SessionImpl
import org.hibernate.usertype.UserType
import java.io.*
import java.lang.reflect.ParameterizedType
import java.nio.charset.StandardCharsets
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Types
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

abstract class JsonType<T> : UserType, Serializable {

    override fun sqlTypes(): IntArray {
        return SQL_TYPES
    }

    override fun returnedClass(): Class<T> {
        return (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<T>
    }

    @Throws(SQLException::class)
    override fun nullSafeGet(rs: ResultSet, names: Array<String>, sharedSessionContractImplementor: SharedSessionContractImplementor,
                             owner: Any): Any? {
        val cellContent = rs.getString(names[0]) ?: return null
        try {
            return objectMapper.readValue(cellContent.toByteArray(StandardCharsets.UTF_8), returnedClass())
        } catch (ex: Exception) {
            throw HibernateException("Failed to convert value of ${names[0]}: " + ex.message, ex)
        }

    }

    @Throws(SQLException::class)
    override fun nullSafeSet(ps: PreparedStatement, value: Any?, idx: Int,
                             sharedSessionContractImplementor: SharedSessionContractImplementor) {
        val jsonSupported = isJsonSupported(sharedSessionContractImplementor)
        if (value == null) {
            ps.setNull(idx, if (jsonSupported) Types.OTHER else Types.VARCHAR)
            return
        }
        try {
            val w = StringWriter()
            objectMapper.writeValue(w, value)
            w.flush()
            if (jsonSupported) {
                ps.setObject(idx, w.toString(), Types.OTHER)
            } else {
                ps.setString(idx, w.toString())
            }
        } catch (ex: Exception) {
            throw HibernateException("Failed to convert Object of index \$idx to String: " + ex.message, ex)
        }

    }

    override fun deepCopy(value: Any?): Any? {

        if(value == null) return null

        try {
            // use serialization to create a deep copy
            val bos = ByteArrayOutputStream()
            val oos = ObjectOutputStream(bos)
            oos.writeObject(value)
            oos.flush()
            oos.close()
            bos.close()

            val bais = ByteArrayInputStream(bos.toByteArray())
            return ObjectInputStream(bais).readObject()
        } catch (ex: ClassNotFoundException) {
            throw HibernateException(ex)
        } catch (ex: IOException) {
            throw HibernateException(ex)
        }

    }

    override fun isMutable(): Boolean {
        return true
    }

    override fun disassemble(value: Any): Serializable? {
        return this.deepCopy(value) as Serializable
    }

    override fun assemble(cached: Serializable, owner: Any): Any? {
        return this.deepCopy(cached)
    }

    override fun replace(original: Any, target: Any, owner: Any): Any? {
        return this.deepCopy(original)
    }

    override fun equals(obj1: Any?, obj2: Any?): Boolean {
        return if (obj1 == null) {
            obj2 == null
        } else obj1 == obj2
    }

    override fun hashCode(obj: Any): Int {
        return obj.hashCode()
    }

    companion object {

        private val SQL_TYPES = intArrayOf(Types.JAVA_OBJECT)
        private var jsonSupportCache: Boolean? = null

        private val OBJECT_MAPPER_CACHE = ThreadLocal<ObjectMapper>()

        private val objectMapper: ObjectMapper
            get() {
                var objectMapper: ObjectMapper? = OBJECT_MAPPER_CACHE.get()
                if (objectMapper == null) {
                    objectMapper = ObjectMapper()
                    objectMapper.registerModules(
                            Jdk8Module(),
                            JavaTimeModule(),
                            offsetDateTimeModule())
                    objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                    objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)

                    OBJECT_MAPPER_CACHE.set(objectMapper)
                }
                return objectMapper
            }

        @Synchronized
        private fun isJsonSupported(sharedSessionContractImplementor: SharedSessionContractImplementor): Boolean {
           println("$jsonSupportCache ----------------- ${(sharedSessionContractImplementor as SessionImpl).sessionFactory.properties}")
            if (jsonSupportCache == null) {
                jsonSupportCache = (sharedSessionContractImplementor as SessionImpl).sessionFactory.properties["hibernate.dialect"].toString()
                        .startsWith("org.hibernate.dialect.PostgreSQL")
            }
            println("$jsonSupportCache")

            return true
        }

        private fun offsetDateTimeModule(): Module {
            return SimpleModule().addDeserializer(OffsetDateTime::class.java, object : JsonDeserializer<OffsetDateTime>() {

                @Throws(IOException::class)
                override fun deserialize(jsonParser: JsonParser,
                                         deserializationContext: DeserializationContext): OffsetDateTime {
                    return OffsetDateTime.parse(jsonParser.valueAsString, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                }
            })
        }
    }
}