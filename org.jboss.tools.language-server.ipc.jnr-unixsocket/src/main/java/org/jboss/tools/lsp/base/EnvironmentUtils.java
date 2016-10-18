package org.jboss.tools.lsp.base;

import java.text.MessageFormat;

public class EnvironmentUtils {

   /**
    * Obtains the environment variable or system property, with preference to the system
    * property in the case both are defined.
    * @param key
    * @return the value that was found
    * @throws IllegalStateException If the requested environment variable or system property was not found
    */
   public static String getEnvVarOrSysProp(final String key)
           throws IllegalStateException {
      final String property = System.getProperty(key);
      if (property != null) {
         return property;
      }
      final String env = System.getenv(key);
      if (env != null) {
         return env;
      }
      throw new IllegalStateException(MessageFormat.format("Could not find required env var or sys prop {0}", key));
   }

}