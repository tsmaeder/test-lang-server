package org.jboss.tools.lsp.testlang.handlers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractCommand {
  public static final Logger LOGGER = LoggerFactory.getLogger(AbstractCommand.class);
  private String name;
  private Pattern pattern;

  public AbstractCommand(String name, String matchString) {
    this.name= name;
    this.pattern = Pattern.compile(matchString);
  }

  public boolean maybeExecute(TextDocumentIdentifier document, String line) {
    Matcher matcher = pattern.matcher(line);
    if (matcher.matches()) {
      LOGGER.info("matched command '"+name+"'");
      String[] groups = new String[matcher.groupCount()];
      for (int i = 0; i < groups.length; i++) {
        groups[i] = matcher.group(i + 1);
      }
      execute(groups, document);
      return true;
    }
    return false;
  }

  protected abstract void execute(String[] groups, TextDocumentIdentifier document);
}
