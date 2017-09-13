package org.jboss.tools.lsp.testlang.handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.jboss.tools.lsp.testlang.Utils;

public class ReplaceInWorkspaceHandler {

	public static WorkspaceEdit renameInWorkspace(File root, String original, String replacement) throws IOException {
		Map<String, List<TextEdit>> editMap = new HashMap<>();
		try {
			Files.walk(root.toPath(), FileVisitOption.FOLLOW_LINKS).filter(p -> p.toString().endsWith(".test"))
					.forEach(path -> {
						List<TextEdit> changes = new ArrayList<>();
						try (Reader r = new BufferedReader(new FileReader(path.toFile()))) {
							Utils.parse(r, (line, lineNumber) -> {
								int pos= line.indexOf(original);
								while (pos >= 0) {
									TextEdit edit= new TextEdit();
									edit.setRange(new Range(new Position(lineNumber, pos), new Position(lineNumber, pos+original.length())));
									edit.setNewText(replacement);
									changes.add(edit);
									pos= line.indexOf(original, pos+original.length());
								}
							});
							if (changes.size() > 0) {
								editMap.put(path.toUri().toString(), changes);
							}
						} catch (IOException e) {
							throw new UncheckedIOException(e);
						}
					});
			;
			return new WorkspaceEdit(editMap);
		} catch (UncheckedIOException e) {
			throw e.getCause();
		}
	}
}
