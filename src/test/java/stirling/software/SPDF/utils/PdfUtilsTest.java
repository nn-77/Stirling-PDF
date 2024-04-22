package stirling.software.SPDF.utils;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.ImageType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.params.shadow.com.univocity.parsers.annotations.Nested;

import javax.imageio.ImageIO;

import static org.junit.jupiter.api.Assertions.*;


import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class PdfUtilsTest {
    private static boolean imageEquals(BufferedImage a, BufferedImage b) {
        // The images must be the same size.
        if (a.getWidth() != b.getWidth() || a.getHeight() != b.getHeight()) {
            return false;
        }

        int width  = a.getWidth();
        int height = a.getHeight();

        // Loop over every pixel.
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Compare the pixels for equality.
                if (a.getRGB(x, y) != b.getRGB(x, y)) {
                    return false;
                }
            }
        }

        return true;
    }

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

    private static PDDocument txtAndImg;
    private static PDDocument imgOnly;
    private static PDDocument txtOnly;
    private static PDDocument multiPage;
    private static PDDocument empty;
    private static final String conversionPath = "testFiles/converted/";
    @BeforeEach
    public void setup() {
        try {
            txtAndImg = Loader.loadPDF(new File("testFiles/txtAndImg.pdf"));
            imgOnly = Loader.loadPDF(new File("testFiles/imgOnly.pdf"));
            txtOnly = Loader.loadPDF(new File("testFiles/txtOnly.pdf"));
            multiPage = Loader.loadPDF(new File("testFiles/multiPage.pdf"));
            empty = Loader.loadPDF(new File("testFiles/empty.pdf"));
        } catch (IOException ex) {
            fail("Could not complete test setup");
        }
    }

    @ParameterizedTest
    @MethodSource("getTextAndRectangle")
    void testTextToPageSizeSuccess(String size, PDRectangle rect) {
        assertEquals(rect, PdfUtils.textToPageSize(size));
    }

    @Test
    void testTextToPageSizeThrows() {
        assertThrows(IllegalArgumentException.class, () -> PdfUtils.textToPageSize("A7"));
    }

    @Nested
    class Image {
        @Test
            // Need to improve branch coverage
            // Can't get fonts to show up in resources
        void testGetAllImages() {
            PDDocument doc = new PDDocument();
            doc.addPage(new PDPage());

            try {
                PDImageXObject img = PDImageXObject.createFromFile("testFiles/blue.png", doc);

                PDPage page = txtAndImg.getPage(0);
                List<RenderedImage> imgList = PdfUtils.getAllImages(page.getResources());
                BufferedImage buf1 = img.getImage();
                BufferedImage buf2 = (BufferedImage) PdfUtils.getAllImages(page.getResources()).get(0);

                assertEquals(1, imgList.size());
                assertTrue(imageEquals(buf1, buf2));
            } catch (IOException ex) {
                fail(ex.toString());
            }
        }

        @Test
            // Fault? NPE if no resources
        void testHasImagesFalse() {
            try {
                assertFalse(PdfUtils.hasImages(txtOnly, "all"));
            } catch (IOException ex) {
                fail(ex.toString());
            }
        }

        @Test
            // Fault? NPE if no resources
        void testHasImagesTrue() {
            try {
                assertTrue(PdfUtils.hasImages(imgOnly, "all"));
            } catch (IOException ex) {
                fail(ex.toString());
            }
        }

        @Test
            // Found fault when inputting "0" as page to check, trace back to generalUtils line 220
            // hasImage seems very inefficient by calling getPage multiple times (which calls getPages under the hood)
        void testHasImagesOnPageFalse() {
            try {
                assertFalse(PdfUtils.hasImagesOnPage(txtOnly.getPage(0)));
            } catch (IOException ex) {
                fail(ex.toString());
            }
        }

        @Test
        void testHasImagesOnPageTrue() {
            try {
                assertTrue(PdfUtils.hasImagesOnPage(imgOnly.getPage(0)));
            } catch (IOException ex) {
                fail(ex.toString());
            }
        }
    }
    @Nested
    class Text {
        @Test
        void testHasTextTrue() {
            try {
                assertTrue(PdfUtils.hasText(txtOnly, "all", "text"));
            } catch (IOException ex) {
                fail(ex.toString());
            }
        }

        @Test
        void testHasTextFalse() {
            try {
                assertFalse(PdfUtils.hasText(txtOnly, "all", "not present"));
            } catch (IOException ex) {
                fail(ex.toString());
            }
        }

        @Test
        void testHasTextOnPageTrue() {
            try {
                assertTrue(PdfUtils.hasTextOnPage(txtOnly.getPage(0), "ex"));
            } catch (IOException ex) {
                fail(ex.toString());
            }
        }

        @Test
        void testHasTextOnPageFalse() {
            try {
                assertFalse(PdfUtils.hasTextOnPage(txtOnly.getPage(0), "texts"));
            } catch (IOException ex) {
                fail(ex.toString());
            }
        }
        @Test
        void testContainsTextFileNullPageTrue() {
            try {
                assertTrue(PdfUtils.containsTextInFile(txtOnly, "text", null));
            } catch (IOException ex) {
                fail(ex.toString());
            }
        }

        @Test
        void testContainsTextFileAllPageTrue() {
            try {
                assertTrue(PdfUtils.containsTextInFile(txtOnly, "text", "all"));
            } catch (IOException ex) {
                fail(ex.toString());
            }
        }

        @Test
            // bug about index 0 page
        void testContainsTextFileSinglePageTrue() {
            try {
                assertTrue(PdfUtils.containsTextInFile(multiPage, "text", "4"));
            } catch (IOException ex) {
                fail(ex.toString());
            }
        }

        @Test
            // bug about index 0 page
        void testContainsTextFileRangeFalse() {
            try {
                assertFalse(PdfUtils.containsTextInFile(multiPage, "text", "1-2, 3"));
            } catch (IOException ex) {
                fail(ex.toString());
            }
        }

        @Test
            // bug about index 0 page
        void testContainsTextFileFalse() {
            try {
                assertFalse(PdfUtils.containsTextInFile(txtOnly, "not present", "0"));
            } catch (IOException ex) {
                fail(ex.toString());
            }
        }
    }
    @Nested
    class PageCount {
        @Test
        public void testGreaterTrue() {
            try {
                assertTrue(PdfUtils.pageCount(multiPage, 2, "greater"));
            } catch (IOException ex) {
                fail(ex.toString());
            }
        }

        @Test
        public void testGreaterFalse() {
            try {
                assertFalse(PdfUtils.pageCount(multiPage, 4, "GREATER"));
            } catch (IOException ex) {
                fail(ex.toString());
            }
        }

        @Test
        public void testLessFalse() {
            try {
                assertFalse(PdfUtils.pageCount(multiPage, 4, "LESS"));
            } catch (IOException ex) {
                fail(ex.toString());
            }
        }

        @Test
        public void testLessTrue() {
            try {
                assertTrue(PdfUtils.pageCount(multiPage, 5, "less"));
            } catch (IOException ex) {
                fail(ex.toString());
            }
        }

        @Test
        public void testEqualTrue() {
            try {
                assertTrue(PdfUtils.pageCount(multiPage, 4, "eQual"));
            } catch (IOException ex) {
                fail(ex.toString());
            }
        }

        @Test
        public void testEqualFalse() {
            try {
                assertFalse(PdfUtils.pageCount(multiPage, 3, "equal"));
            } catch (IOException ex) {
                fail(ex.toString());
            }
        }

        @Test
        public void testInvalid() {
            assertThrows(IllegalArgumentException.class, () -> PdfUtils.pageCount(imgOnly, 8,"lessOrEqual"));
        }
    }

    @Nested
    class PageSize {
        @Test
        public void testCorrectSize() {
            try {
                assertTrue(PdfUtils.pageSize(txtAndImg, "612x792"));
            } catch (IOException ex) {
                fail(ex.toString());
            }
        }

        @Test
        public void testIncorrectSize() {
            try {
                assertFalse(PdfUtils.pageSize(txtAndImg, "792x612"));
            } catch (IOException ex) {
                fail(ex.toString());
            }
        }

        @Test
        public void testNearSize() {
            try {
                assertFalse(PdfUtils.pageSize(txtAndImg, "612x612"));
            } catch (IOException ex) {
                fail(ex.toString());
            }
        }
    }

    @Nested
    class ConvertToImage {
        @Test
        public void testInvalidPDF() {
            assertThrows(IOException.class, () -> {
                byte[] bytes = FileUtils.readFileToByteArray(new File("testFiles/blue.png"));
                byte[] res = PdfUtils.convertFromPdf(bytes, "png", ImageType.RGB, true, 300, "img");

                saveImage(res, conversionPath + "shouldNotExist", "png");
            });
        }

        @Test
        // Multi-paged but single image
        public void testConvertPNGSingle() {
            try {
                byte[] bytes = FileUtils.readFileToByteArray(new File("testFiles/multiPage.pdf"));
                byte[] res = PdfUtils.convertFromPdf(bytes, "png", ImageType.RGB, true, 300, "img");

                saveImage(res, conversionPath + "testPNG", "png");
                assertTrue(isValidImage(new File(conversionPath+"testPNG.png")));
            } catch (Exception ex) {
                fail (ex.toString());
            }
        }

        @Test
        public void testConvertTIFFSingle() {
            try {
                byte[] bytes = FileUtils.readFileToByteArray(new File("testFiles/imgOnly.pdf"));
                byte[] tiffImageBytes = PdfUtils.convertFromPdf(bytes, "tiff", ImageType.RGB, true, 300, "img");

                saveImage(tiffImageBytes, conversionPath+"testTIFF", "tiff");
                assertTrue(isValidImage(new File(conversionPath+"testTIFF.tiff")));
            } catch (Exception ex) {
                fail (ex.toString());
            }
        }

        @Test
        // multi-paged but single image
        public void testConvertTIFSingle() {
            try {
                byte[] bytes = FileUtils.readFileToByteArray(new File("testFiles/multiPage.pdf"));
                byte[] tiffImageBytes = PdfUtils.convertFromPdf(bytes, "tif", ImageType.RGB, true, 300, "img");

                saveImage(tiffImageBytes, conversionPath+"testTIF", "tif");
                assertTrue(isValidImage(new File(conversionPath+"testTIF.tif")));
            } catch (Exception ex) {
                fail (ex.toString());
            }
        }

        @Test
        public void testConvertJPGMulti() {
            try {
                byte[] bytes = FileUtils.readFileToByteArray(new File("testFiles/multiPage.pdf"));
                byte[] zipBytes = PdfUtils.convertFromPdf(bytes, "jpg", ImageType.RGB, false, 300, "img");

                extractImagesFromZip(zipBytes, conversionPath+"zipJPG/", "jpg");
                // assert num images == num pages
                assertEquals(multiPage.getNumberOfPages(), new File(conversionPath+"zipJPG").listFiles().length);
            } catch (Exception ex) {
                fail (ex.toString());
            }
        }

        public static boolean isValidImage(File f) {
            boolean isValid = true;
            try {
                ImageIO.read(f).flush();
            } catch (Exception e) {
                isValid = false;
            }
            return isValid;
        }

        public static void saveImage(byte[] imageBytes, String outputPath, String ext) {
            try {
                ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
                BufferedImage image = ImageIO.read(bis);
                bis.close();
                File imgFile = new File(outputPath + "." + ext);
                ImageIO.write(image, ext, imgFile);
                System.out.println("Output image saved to: " + imgFile.getAbsolutePath());
            } catch (IOException ex) {
                fail(ex.toString());
            }
        }

        public static void extractImagesFromZip(byte[] zipBytes, String dirPath, String ext) throws IOException {
            try (ByteArrayInputStream bis = new ByteArrayInputStream(zipBytes);
                 ZipInputStream zis = new ZipInputStream(bis)) {

                int i = 0;
                while (zis.getNextEntry() != null) {
                    // Read the image bytes
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = zis.read(buffer)) != -1) {
                        bos.write(buffer, 0, bytesRead);
                    }

                    // Convert image bytes to BufferedImage
                    ByteArrayInputStream imageInputStream = new ByteArrayInputStream(bos.toByteArray());
                    BufferedImage image = ImageIO.read(imageInputStream);
                    imageInputStream.close();

                    // Save the BufferedImage
                    String outputFileName = dirPath+"page_" + (++i) +"."+ext;
                    FileOutputStream fos = new FileOutputStream(outputFileName);
                    ImageIO.write(image, ext, fos);
                    fos.close();

                    System.out.println("Image extracted and saved: " + outputFileName);
                }
            }
        }
    }




//    @Test
//    // May need this code later
//    void test() {
//        PDDocument doc = new PDDocument();
//        PDPage page = new PDPage();
//        doc.addPage(page);
//
//        try {
//            PDImageXObject img = PDImageXObject.createFromFile("testFiles/blue.png", doc);
//
//            // Start a new content stream which will "hold" the to be created content
//            PDPageContentStream contents = new PDPageContentStream(doc, page);
//            contents.drawImage(img, 0, 0);
//            contents.close();
//            assertTrue(PdfUtils.hasImages(doc, "all"));
//        } catch (IOException ex) {
//            fail(ex.toString());
//        }
//    }
}
