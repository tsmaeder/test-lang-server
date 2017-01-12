package org.jboss.tools.lsp.testlang;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jnr.unixsocket.UnixSocketAddress;
import jnr.unixsocket.UnixSocketChannel;

public class Utils {

	private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

	/**
	 * Obtains the environment variable or system property, with preference to
	 * the system property in the case both are defined.
	 * 
	 * @param key
	 * @return the value that was found
	 * @throws IllegalStateException
	 *             If the requested environment variable or system property was
	 *             not found
	 */
	public static String getEnvVarOrSysProp(final String key) throws IllegalStateException {
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

	public static UnixSocketChannel createChannel(String fileName) throws IOException {
		try {
			File file = new File(fileName);

			LOGGER.info("Connecting channel to socket at {}", file);
			final UnixSocketAddress address = new UnixSocketAddress(file);
			final UnixSocketChannel channel = UnixSocketChannel.open(address);
			LOGGER.info("Connected channel to socket at {}", file);
			return channel;
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}

	}

}