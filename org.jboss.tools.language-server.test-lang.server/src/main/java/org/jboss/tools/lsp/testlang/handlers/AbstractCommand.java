package org.jboss.tools.lsp.testlang.handlers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractCommand {
	private Pattern pattern;

	public AbstractCommand(String matchString) {
		this.pattern= Pattern.compile(matchString);
	}
	
	public boolean maybeExecute(String line) {
		Matcher matcher = pattern.matcher(line);
		if (matcher.matches()) {
			String[] groups = new String[matcher.groupCount()];
			for (int i= 0; i < groups.length; i++) {
				groups[i]= matcher.group(i+1);
			}
			execute(groups);
			return true;
		}
		return false;
		
	}
	
	protected abstract void execute(String[] groups);
}
