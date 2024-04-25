package stirling.software.SPDF.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static stirling.software.SPDF.utils.TestingUtils.*;

public class RequestUriUtilsTest {
    private static Stream<Arguments> provideUris() {
        // Provides different scenarios of request uris
        return Stream.of(
            Arguments.of("/css/file.css", true),                    // CSS resource
            Arguments.of("/js/file.js", true),                      // JS resource
            Arguments.of("/images/file.png", true),                 // image resource
            Arguments.of("/public/file.jpeg", true),                // public resource
            Arguments.of("/pdfjs/file.js", true),                   // pdfjs resource
            Arguments.of("/anyDir/subDir/img.svg", true),           // svg resource
            Arguments.of("/api/v1/info/status/file", true),         // api status resource
            Arguments.of("/randomFile", false),                      // random non static resurce
            Arguments.of("/image/svg.png", false)                   // close match non-static resource
        );
    }
    @ParameterizedTest
    @MethodSource("provideUris")
    void testIsStaticResource(String requestUri, boolean expected) {
        assertEquals(expected, RequestUriUtils.isStaticResource(requestUri));
    }
}
