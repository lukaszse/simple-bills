package pl.com.seremak.simplebills.transactionmanagement.testUtils

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule

import java.text.SimpleDateFormat

class CommonTestUtils {

    static def DATE_PATTERN = "MMM dd, yyyy, hh:mm:ss a"
    static def dateFormat = new SimpleDateFormat(DATE_PATTERN)
    static def objectMapper = objectMapper()

    static def objectMapper() {
        JsonMapper.builder()
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .propertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
                .addModule(new JavaTimeModule())
                .build();
    }

    static def mapDatesAndNumbers(final Map<String, Object> record) {
        mapIntegersToLong(record)
        mapStringDateToDate(record)
    }

    static def mapDatesAndNumbers(final List<Map<String, Object>> elements) {
        elements.stream()
                .forEach({ record ->
                    mapDatesAndNumbers(record)
                })
    }

    static private def mapIntegersToLong(Map<String, Object> record) {
        record.entrySet().each {
            if (it.getValue() instanceof Integer) {
                record.put(it.getKey(), Long.valueOf((Integer) it.getValue()))
            }
        }
    }

    static private def mapStringDateToDate(Map<String, Object> record) {
        record.entrySet().each {
            if (it.getValue() instanceof String) {
                try {
                    def date = dateFormat.parse((String) it.getValue())
                    record.put(it.getKey(), date)
                } catch (Exception ex) {
                }
            }
        }
    }

    static def getStringLine(final String str, final int lineNumber) {
        str.lines().skip(lineNumber).findFirst().get().replaceAll("\t+\$", "")
    }

    static def getLineNumber(final String str) {
        str.lines().count()
    }
}
