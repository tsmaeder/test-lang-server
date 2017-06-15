package org.jboss.tools.lsp.testlang;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

public class DocumentUpdateTest {
    private DocumentManager dm;
    @Before
    public void setUp() throws IOException, URISyntaxException {
        dm = Mockito.spy(new DocumentManager());
        dm.didOpen("foo", "first\nsecond\nthird");
    }
    
    @Test
    public void testRemoveLines() throws IOException, URISyntaxException {
        dm.didChange("foo", Arrays.asList(createChange(0, 4, 2, 3, "inserted Text")));
        Assert.assertEquals(Arrays.asList("firsinserted Textrd"), dm.getContent("foo"));
    }
    
    @Test
    public void testRemoveNewLine() throws IOException, URISyntaxException {
        dm.didChange("foo", Arrays.asList(createChange(0, 5, 1, 0, "")));
        Assert.assertEquals(Arrays.asList("firstsecond", "third"), dm.getContent("foo"));
    }
    
    @Test
    public void testSameLine() throws IOException, URISyntaxException {
        dm.didChange("foo", Arrays.asList(createChange(0, 1, 0, 3, "boink")));
        Assert.assertEquals(Arrays.asList("fboinkst", "second", "third"), dm.getContent("foo"));
    }
    
    @Test
    public void testOnlyInsert() throws IOException, URISyntaxException {
        dm.didChange("foo", Arrays.asList(createChange(0, 1, 0, 1, "boink")));
        Assert.assertEquals(Arrays.asList("fboinkirst", "second", "third"), dm.getContent("foo"));
    }
    
    @Test
    public void testReplaceAll() throws IOException, URISyntaxException {
        dm.didChange("foo", Arrays.asList(createChange(0, 0, 2, 5, "first\nsecond\nthird")));
        Assert.assertEquals(Arrays.asList("first", "second", "third"), dm.getContent("foo"));
    }
    
    @Test
    public void testInsertEmpty() throws IOException, URISyntaxException {
        dm.didChange("foo", Arrays.asList(createChange(0, 0, 2, 5, "")));
        dm.didChange("foo", Arrays.asList(createChange(0, 0, 0, 0, "1")));
        Assert.assertEquals(Arrays.asList("1"), dm.getContent("foo"));
    }


    static TextDocumentContentChangeEvent createChange(int startLine, int startChar, int endLine, int endChar, String insertedText) {
        return new TextDocumentContentChangeEvent(
                   new Range(
                             new Position(startLine, startChar), 
                             new Position(endLine, endChar)), 
                   0, 
                   insertedText);
    }

}
