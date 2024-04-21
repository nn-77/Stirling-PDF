package stirling.software.SPDF.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.util.stream.Stream;

public class PdfUtilsTest {

    private static Stream<Arguments> getTextAndRectangle() {
        return Stream.of(
                Arguments.of("A0", PDRectangle.A0),
                Arguments.of("A1", PDRectangle.A1),
                Arguments.of("A2", PDRectangle.A2),
                Arguments.of("A3", PDRectangle.A3),
                Arguments.of("A4", PDRectangle.A4),
                Arguments.of("A5", PDRectangle.A5),
                Arguments.of("A6", PDRectangle.A6),
                Arguments.of("LETTER", PDRectangle.LETTER),
                Arguments.of("LEGAL", PDRectangle.LEGAL)
        );
    }

    @ParameterizedTest
    @MethodSource("getTextAndRectangle")
    void testTextToPageSizeSucess(String size, PDRectangle rect) {
        assertEquals(rect, PdfUtils.textToPageSize(size));
    }

    @Test
    void testTextToPageSizeThrows() {
        assertThrows(IllegalArgumentException.class, () -> PdfUtils.textToPageSize("A7"));
    }
}
