package org.jboss.tools.lsp.testlang;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Test;

/**
 * Testing the {@link DocumentManager}.
 */
public class DocumentManagerTest {
	
	@Test
	public void shouldReadFile() throws IOException, URISyntaxException {
		// given
		final String fileLocation = "file://" + Thread.currentThread().getContextClassLoader().getResource("foo.test").getFile();
		// when
		final List<String> content = new DocumentManager().getContent(fileLocation);
		// then
		assertThat(content).contains("ERROR");
	}

}
