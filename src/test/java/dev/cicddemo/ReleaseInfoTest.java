package dev.cicddemo;

import org.junit.jupiter.api.Test;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.*;

class ReleaseInfoTest {
    @Test void shortensCommitWithoutChangingIdentity() {
        var info = new ReleaseInfo("Demo", "Hello", "1.0", "abcdef0123456789", "42");
        assertEquals("abcdef012345", info.shortCommit());
        assertEquals("abcdef0123456789", info.commit());
    }
    @Test void acceptsShortLocalIdentifier() {
        assertEquals("local", new ReleaseInfo("Demo", "Hello", "1.0", "local", "local").shortCommit());
    }
    @Test void rejectsMissingReleaseMetadata() {
        assertThrows(IllegalArgumentException.class, () -> ReleaseInfo.from(new Properties()));
        assertThrows(IllegalArgumentException.class, () -> new ReleaseInfo("Demo", " ", "1.0", "abc", "1"));
    }
    @Test void loadsPackagedReleaseMetadata() throws Exception {
        var info = ReleaseInfo.load();
        assertEquals("Release Observatory", info.name());
        assertFalse(info.version().contains("${"));
        assertFalse(info.commit().contains("${"));
    }
    @Test void escapesUntrustedContentInHomepage() {
        var info = new ReleaseInfo("Demo", "<script>alert('x')</script> & \"hello\"", "1.0", "abc", "42");
        String page = ReleaseServlet.render(info);
        assertFalse(page.contains("<script>"));
        assertTrue(page.contains("&lt;script&gt;"));
        assertTrue(page.contains("&amp; &quot;hello&quot;"));
        assertTrue(page.contains("42"));
    }
}
