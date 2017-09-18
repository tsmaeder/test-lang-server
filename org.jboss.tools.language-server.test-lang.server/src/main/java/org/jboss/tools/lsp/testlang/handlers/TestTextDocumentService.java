/**
 * ***************************************************************************** Copyright (c) 2017
 * Red Hat. All rights reserved. This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Red Hat - Initial Contribution
 * *****************************************************************************
 */
package org.jboss.tools.lsp.testlang.handlers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentHighlightKind;
import org.eclipse.lsp4j.DocumentOnTypeFormattingParams;
import org.eclipse.lsp4j.DocumentRangeFormattingParams;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureInformation;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.jboss.tools.lsp.testlang.DocumentManager;
import org.jboss.tools.lsp.testlang.TestLanguageServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of the TextDocumentService
 *
 * @author Thomas Mäder
 */
public class TestTextDocumentService implements TextDocumentService {

  private final AbstractCommand[] commands =
      new AbstractCommand[] {
        new AbstractCommand("Show Message", "window/showMessageNotification:(?<type>\\w+):(?<message>.+)") {

          @Override
          public void execute(String[] groups, TextDocumentIdentifier document) {
            final MessageType type = MessageType.valueOf(groups[0]);
            final String message = groups[1].trim();
            LOGGER.info(
                "Found line starting with 'window/showMessageNotification' keyword:\n{} (type={})",
                message,
                type.toString().toLowerCase());
            testLanguageServer.sendShowMessageNotification(type, message);
          }
        },
        new AbstractCommand("Show Message Request",
            "window/showMessageRequest:(?<type>\\w+):(?<command>\\w+):(?<message>.+)") {

          @Override
          public void execute(String[] groups, TextDocumentIdentifier document) {
            final MessageType type = MessageType.valueOf(groups[0]);
            final String message = groups[2].trim();
            LOGGER.info(
                "Found line starting with 'window/showMessageRequest' keyword:\n{} (type={})",
                message,
                type.toString().toLowerCase());
            testLanguageServer.sendShowMessageRequest(type, message, groups[1]);
          }
        },
        new AbstractCommand("Mark bad words", "textDocument/badWord:(?<type>\\w+):(?<word>.+):(?<message>.+)") {

          @Override
          public void execute(String[] groups, TextDocumentIdentifier document) {
            final DiagnosticSeverity severity = DiagnosticSeverity.valueOf(groups[0]);
            final String message = groups[2].trim();
            LOGGER.info(
                "Found line starting with 'textDocument/badWord' keyword:\n{} (type={})",
                message,
                severity.toString().toLowerCase());

            List<Diagnostic> diagnostics = new ArrayList<>();
            try {
              List<String> content =
                  testLanguageServer.getDocumentManager().getContent(document.getUri());
              int l = 0;
              for (String line : content) {
                int c = 0;
                while (c < line.length()) {
                  int start = line.indexOf(groups[1], c);
                  if (start >= 0) {
                    diagnostics.add(
                        createDiagnostic(severity, l, start, groups[1].length(), message));
                    c = start + groups[1].length();
                  } else {
                    break;
                  }
                }
                l++;
              }
            } catch (IOException | URISyntaxException e) {
              diagnostics.add(
                  createDiagnostic(
                      DiagnosticSeverity.Error, 0, 0, 0, "Could not compute diagnostics"));
            }

            testLanguageServer.publishDiagnostics(document.getUri(), diagnostics);
          }

          private Diagnostic createDiagnostic(
              DiagnosticSeverity severity, int line, int start, int length, String message) {
            Diagnostic diagnostic = new Diagnostic();
            diagnostic.setCode("badWord");
            diagnostic.setSeverity(severity);
            diagnostic.setRange(
                new Range(new Position(line, start), new Position(line, start + length)));
            diagnostic.setMessage(message);
            return diagnostic;
          }
        },
        new AbstractCommand("Create Snippet", "textDocument/snippet:(?<name>\\w+):(?<message>.+)") {

          @Override
          protected void execute(String[] groups, TextDocumentIdentifier document) {
            String name= groups[0];
            String snippetText= groups[1];
            CompletionItem snippet = new CompletionItem(name);
            snippet.setInsertText(snippetText);
            snippet.setInsertTextFormat(InsertTextFormat.Snippet);
            snippets.add(snippet);
          }
          
        }
      };

  protected static final Pattern showMessagePattern =
      Pattern.compile("window/showMessageNotification:(?<type>\\w+):(?<message>.+)");
  /** The usual Logger. */
  public static final Logger LOGGER = LoggerFactory.getLogger(TestTextDocumentService.class);

  private final TestLanguageServer testLanguageServer;
  
  private List<CompletionItem> snippets= new ArrayList<>();

  public TestTextDocumentService(TestLanguageServer testLanguageServer) {
    this.testLanguageServer = testLanguageServer;
  }

  @Override
  public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(
      TextDocumentPositionParams position) {
    List<CompletionItem> items = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      items.add(createCompletionItem("TestItem " + i));
    }

    items.addAll(snippets);

    return CompletableFuture.completedFuture(Either.forRight(new CompletionList(false, items)));
  }

  private CompletionItem createCompletionItem(String itemText) {
    CompletionItem i = new CompletionItem();
    i.setDetail(itemText + " Detail");
    i.setInsertText(itemText);
    i.setLabel("Label for " + itemText);
    i.setCommand(new Command("TestCommand" + itemText, "TestCommand", Arrays.asList(itemText)));
    i.setData("true");
    return i;
  }

  @Override
  public CompletableFuture<CompletionItem> resolveCompletionItem(CompletionItem unresolved) {
    if ("true".equals(unresolved.getData())) {
      return CompletableFuture.completedFuture(
          createCompletionItem(unresolved.getInsertText() + " resolved"));
    } else {
      return CompletableFuture.completedFuture(unresolved);
    }
  }

  @Override
  public CompletableFuture<Hover> hover(TextDocumentPositionParams position) {
    Range range =
        new Range(
            position.getPosition(),
            new Position(
                position.getPosition().getLine(), position.getPosition().getCharacter() + 1));
    return CompletableFuture.completedFuture(
        new Hover(
            Arrays.asList(Either.forLeft("First element"), Either.forLeft("Second element")),
            range));
  }

  @Override
  public CompletableFuture<SignatureHelp> signatureHelp(TextDocumentPositionParams position) {
    SignatureHelp result = new SignatureHelp();
    List<SignatureInformation> signatures = new ArrayList<>();
    result.setSignatures(signatures);
    signatures.add(new SignatureInformation("First sig", "some doc", Collections.emptyList()));
    signatures.add(
        new SignatureInformation("Second sig", "some more doc", Collections.emptyList()));
    signatures.add(new SignatureInformation("Third sig", "some doc", Collections.emptyList()));
    result.setActiveSignature(1);
    return CompletableFuture.completedFuture(result);
  }

  @Override
  public CompletableFuture<List<? extends Location>> definition(TextDocumentPositionParams params) {
    try {
      DocumentManager dm = testLanguageServer.getDocumentManager();
      String word = dm.getWordAtPosition(params.getTextDocument(), params.getPosition());
      CompletableFuture<List<? extends Location>> result =
          new CompletableFuture<List<? extends Location>>();
      if (word != null && word.length() > 0) {
        dm.findInDocument(
            params.getTextDocument(),
            word,
            (doc, range, text) -> {
              result.complete(Collections.singletonList(new Location(doc.getUri(), range)));
              return true;
            });
      }
      if (!result.isDone()) {
        result.complete(Collections.emptyList());
      }
      return result;
    } catch (IOException | URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  interface OccurrenceHandler<T> {
    T handle(TextDocumentIdentifier doc, Range range, String text);
  }

  private <T> CompletableFuture<List<? extends T>> doWithReferences(
      TextDocumentIdentifier document, Position pos, OccurrenceHandler<T> handler) {
    return CompletableFuture.supplyAsync(
        new Supplier<List<? extends T>>() {

          @Override
          public List<T> get() {
            List<T> result = new ArrayList<>();
            try {
              String selectedWord =
                  testLanguageServer.getDocumentManager().getWordAtPosition(document, pos);
              if (selectedWord != null && selectedWord.length() > 0) {
                testLanguageServer
                    .getDocumentManager()
                    .findInDocument(
                        document,
                        selectedWord,
                        (doc, range, text) -> {
                          T res = handler.handle(doc, range, text);
                          if (res != null) {
                            result.add(res);
                          }
                          return true;
                        });
              }
            } catch (IOException | URISyntaxException e) {
              throw new RuntimeException(e);
            }

            return result;
          }
        });
  }

  @Override
  public CompletableFuture<List<? extends Location>> references(ReferenceParams params) {
    return doWithReferences(
        params.getTextDocument(),
        params.getPosition(),
        (doc, range, text) -> {
          return new Location(doc.getUri(), range);
        });
  }

  @Override
  public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(
      TextDocumentPositionParams params) {
    return doWithReferences(
        params.getTextDocument(),
        params.getPosition(),
        (doc, range, text) -> {
          return new DocumentHighlight(range, DocumentHighlightKind.Text);
        });
  }

  @Override
  public CompletableFuture<List<? extends SymbolInformation>> documentSymbol(
      DocumentSymbolParams params) {
    List<SymbolInformation> result = new ArrayList<>();
    Set<String> foundWords = new HashSet<>();
    try {
      String uri = params.getTextDocument().getUri();
      List<String> lines = testLanguageServer.getDocumentManager().getContent(uri);
      for (int l = 0; l < lines.size(); l++) {
        String line = lines.get(l);
        int pos = 0;
        while (pos < line.length()) {
          while (pos < line.length() && Character.isWhitespace(line.charAt(pos))) {
            pos++;
          }
          StringBuilder b = new StringBuilder();
          int startPos = pos;
          while (pos < line.length() && !Character.isWhitespace(line.charAt(pos))) {
            b.append(line.charAt(pos));
            pos++;
          }
          String word = b.toString();
          if (word.length() > 0 && !foundWords.contains(word)) {
            foundWords.add(word);
            SymbolInformation s = new SymbolInformation();
            s.setName(word + " (testls)");
            s.setKind(SymbolKind.String);
            s.setLocation(
                new Location(uri, new Range(new Position(l, startPos), new Position(l, pos))));
            result.add(s);
          }
        }
      }
      return CompletableFuture.completedFuture(result);
    } catch (IOException | URISyntaxException e) {
      throw new RuntimeException("Error on didOpen", e);
    }
  }

  @Override
  public CompletableFuture<List<? extends Command>> codeAction(CodeActionParams params) {
    LOGGER.info("Handling code action request: " + params);
    List<Command> commands = new ArrayList<>();
    boolean hasDiagnostic = params.getContext().getDiagnostics().isEmpty();
    commands.add(
        new Command(
            "A quick fix (" + hasDiagnostic + ")",
            "lsp.applyTextEdit",
            Arrays.asList(
                new TextEdit(new Range(new Position(0, 0), new Position(0, 0)), "foobar"))));

    for (Diagnostic diagnostic : params.getContext().getDiagnostics()) {
      if ("badWord".equals(diagnostic.getCode())) {
        String selectedWord;
        try {
          selectedWord =
              testLanguageServer
                  .getDocumentManager()
                  .getWordAtPosition(params.getTextDocument(), diagnostic.getRange().getStart());
          if (selectedWord != null && selectedWord.length() > 0) {
            WorkspaceEdit edit =
                ReplaceInWorkspaceHandler.renameInWorkspace(
                    testLanguageServer.getRoot(), selectedWord, "foobar");
            commands.add(
                new Command(
                    "Replace with foobar",
                    "lsp.applyWorkspaceEdit",
                    Collections.singletonList(edit)));
          }
        } catch (IOException | URISyntaxException e) {
          throw new RuntimeException(e);
        }
      }
    }

    return CompletableFuture.completedFuture(commands);
  }

  @Override
  public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params) {
    return CompletableFuture.completedFuture(Collections.emptyList());
  }

  @Override
  public CompletableFuture<CodeLens> resolveCodeLens(CodeLens unresolved) {
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletableFuture<List<? extends TextEdit>> formatting(DocumentFormattingParams params) {
    return CompletableFuture.completedFuture(Collections.emptyList());
  }

  @Override
  public CompletableFuture<List<? extends TextEdit>> rangeFormatting(
      DocumentRangeFormattingParams params) {
    return CompletableFuture.completedFuture(Collections.emptyList());
  }

  @Override
  public CompletableFuture<List<? extends TextEdit>> onTypeFormatting(
      DocumentOnTypeFormattingParams params) {
    return CompletableFuture.completedFuture(Collections.emptyList());
  }

  @Override
  public CompletableFuture<WorkspaceEdit> rename(RenameParams params) {
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public void didOpen(DidOpenTextDocumentParams params) {
    try {
      testLanguageServer
          .getDocumentManager()
          .didOpen(params.getTextDocument().getUri(), params.getTextDocument().getText());
    } catch (IOException | URISyntaxException e) {
      throw new RuntimeException("Error on didOpen", e);
    }
    executeCommands(new TextDocumentIdentifier(params.getTextDocument().getUri()));
  }

  @Override
  public void didChange(DidChangeTextDocumentParams params) {
    testLanguageServer
        .getDocumentManager()
        .didChange(params.getTextDocument().getUri(), params.getContentChanges());
  }

  @Override
  public void didClose(DidCloseTextDocumentParams params) {
    testLanguageServer.getDocumentManager().didClose(params.getTextDocument().getUri());
  }

  @Override
  public void didSave(DidSaveTextDocumentParams params) {
    LOGGER.info("Handling document saved");
    executeCommands(params.getTextDocument());
  }

  private void executeCommands(TextDocumentIdentifier textdocument) {
    snippets.clear();
    final DocumentManager documentManager = testLanguageServer.getDocumentManager();
    String documentUri = textdocument.getUri();
    try {
      final List<String> lines = documentManager.getContent(documentUri);
      LOGGER.info("Document saved: \n{}", lines.stream().collect(Collectors.joining("\n")));
      for (String line : lines) {
        for (AbstractCommand command : commands) {
          if (command.maybeExecute(textdocument, line)) {
            break;
          }
        }
      }
    } catch (IOException | URISyntaxException e) {
      LOGGER.error("Failed to read document content at " + documentUri, e);
    }
  }
}
