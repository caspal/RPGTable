package info.pascalkrause.rpgtable.utils;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.common.net.MediaType;

import io.vertx.core.buffer.Buffer;

public class MediaTypeDetectorTest {

    private final MediaTypeDetector testClass = new MediaTypeDetector(null);

    @Test
    public void detect_text_plain() {
        Buffer string = Buffer.buffer("Test", Charsets.UTF_8.toString());
        assertThat(testClass.detectBlocking(string)).isEqualTo(MediaType.PLAIN_TEXT_UTF_8.withoutParameters());
    }

    @Test
    public void detect_text_html() {
        Buffer html = Buffer.buffer("<html>This looks like a HTML document.</html>", Charsets.UTF_8.toString());
        assertThat(testClass.detectBlocking(html)).isEqualTo(MediaType.HTML_UTF_8.withoutParameters());
    }

    private static Buffer readFile(String ressourcePath) throws IOException {
        return Buffer.buffer(Resources.asByteSource(Resources.getResource(ressourcePath)).read());
    }

    @Test
    public void detect_image_gif() throws IOException {
        assertThat(testClass.detectBlocking(readFile("images/TestImage.gif"))).isEqualTo(MediaType.GIF);
    }

    @Test
    public void detect_image_jpeg() throws IOException {
        assertThat(testClass.detectBlocking(readFile("images/TestImage.jpeg"))).isEqualTo(MediaType.JPEG);
    }

    @Test
    public void detect_image_png() throws IOException {
        assertThat(testClass.detectBlocking(readFile("images/TestImage.png"))).isEqualTo(MediaType.PNG);
    }

    /**
     * For some reason Tika cannot detect SVG, although its supported [1]. Because of this we will not support SVG
     * images for now. [1] https://tika.apache.org/1.15/formats.html
     */
    @Test
    @Ignore
    public void detect_image_svg() throws IOException {
        assertThat(testClass.detectBlocking(readFile("images/svg.svg"))).isEqualTo(MediaType.SVG_UTF_8);
    }
}