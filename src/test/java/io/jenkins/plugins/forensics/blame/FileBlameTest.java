package io.jenkins.plugins.forensics.blame;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.util.SerializableTest;

import static io.jenkins.plugins.forensics.assertions.Assertions.*;

/**
 * Tests the class {@link FileBlame}.
 *
 * @author Ullrich Hafner
 */
class FileBlameTest extends SerializableTest<FileBlame> {
    private static final String SERIALIZATION_NAME = "fileBlame.ser";

    private static final String COMMIT = "commit";
    private static final String NAME = "name";
    private static final String EMAIL = "email";
    private static final int TIME = 12_345;

    @Test
    void shouldCreateInstance() {
        FileBlame request = new FileBlame("file");

        assertThat(request).hasNoLines();
        assertThat(request.getFileName()).isEqualTo("file");
        assertThat(request).isEqualTo(new FileBlame("file"));

        addDetails(request, 15);
        verifyDetails(request, 15);

        FileBlame other = new FileBlame("file");
        addDetails(other, 15);

        assertThat(request).isEqualTo(other);
    }

    private void addDetails(final FileBlame request, final int lineNumber) {
        request.setCommit(lineNumber, COMMIT);
        request.setName(lineNumber, NAME);
        request.setEmail(lineNumber, EMAIL);
        request.setTime(lineNumber, TIME);
    }

    private void verifyDetails(final FileBlame request, final int line) {
        assertThat(request.getCommit(line)).isEqualTo(COMMIT);
        assertThat(request.getName(line)).isEqualTo(NAME);
        assertThat(request.getEmail(line)).isEqualTo(EMAIL);
        assertThat(request.getTime(line)).isEqualTo(TIME);
    }

    @Test
    void shouldMergeRequest() {
        FileBlame request = createSerializable();

        FileBlame sameLine = new FileBlame("file");
        request.merge(sameLine);
        assertThat(request).hasLines(1);
        verifyDetails(request, 1);

        FileBlame otherLine = new FileBlame("file");
        addDetails(otherLine, 2);

        request.merge(otherLine);
        assertThat(request.iterator()).toIterable().containsExactly(1, 2);
        assertThat(request).hasLines(1, 2);
        verifyDetails(request, 1);
        verifyDetails(request, 2);

        assertThatThrownBy(() -> request.merge(new FileBlame("wrong")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("wrong").hasMessageContaining("file");
    }

    @Test
    void shouldReturnMeaningfulDefaults() {
        FileBlame request = new FileBlame("file");

        assertThat(request.getCommit(2)).isEqualTo(FileBlame.EMPTY);
        assertThat(request.getEmail(2)).isEqualTo(FileBlame.EMPTY);
        assertThat(request.getName(2)).isEqualTo(FileBlame.EMPTY);
        assertThat(request.getTime(2)).isEqualTo(FileBlame.EMPTY_INTEGER);
    }

    @Test
    void shouldNormalizeFileName() {
        FileBlame request = new FileBlame("C:\\path\\to\\file");

        assertThat(request).hasNoLines();
        assertThat(request.getFileName()).isEqualTo("C:/path/to/file");
    }

    @Override
    protected FileBlame createSerializable() {
        FileBlame request = new FileBlame("file");

        addDetails(request, 1);
        assertThat(request).hasLines(1);

        return request;
    }

    /**
     * Verifies that saved serialized format (from a previous release) still can be resolved with the current
     * implementation of {@link FileBlame}.
     */
    @Test
    void shouldReadOldSerialization() {
        byte[] restored = readAllBytes(SERIALIZATION_NAME);

        assertThatSerializableCanBeRestoredFrom(restored);
    }

    /**
     * Serializes an issues to a file. Use this method in case the issue properties have been changed and the
     * readResolve method has been adapted accordingly so that the old serialization still can be read.
     *
     * @param args
     *         not used
     *
     * @throws IOException
     *         if the file could not be written
     */
    public static void main(final String... args) throws IOException {
        new FileBlameTest().createSerializationFile();
    }

    @Override
    protected Class<?> getTestResourceClass() {
        return FileBlameTest.class;
    }
}
