package pl.com.seremak.simplebills.transactionmanagement.testUtils

import com.fasterxml.jackson.core.type.TypeReference

import java.util.stream.Collectors

class JsonImporter {

    private static final String TEST_RESOURCE_FILE_PATH_PATTERN = "src/test/resources/%s"

    static String getStringFromFile(final String resourceFilePath) {
        String filePath = getFileResourcePath(resourceFilePath)
        final BufferedReader br = new BufferedReader(new FileReader(filePath))
        return br.lines()
                .collect(Collectors.joining("\n"))
    }

    static String getFileResourcePath(String resourceFilePath) {
        final String filePath = String.format(TEST_RESOURCE_FILE_PATH_PATTERN, resourceFilePath)
        filePath
    }

    static <T> T getDataFromFile(final String filePath, final TypeReference<T> valueTypeRef) {
        def jsonString = getStringFromFile(filePath)
        def elements = CommonTestUtils.objectMapper.readValue(jsonString, valueTypeRef)
        elements as T
    }

    static <T> T getDataFromFile(final String filePath, final Class<T> clazz) {
        def jsonString = getStringFromFile(filePath)
        def elements = CommonTestUtils.objectMapper.readValue(jsonString, clazz)
        elements as T
    }
}
