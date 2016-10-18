package org.jboss.tools.lsp.transport.junixsocket;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;

import org.jboss.tools.lsp.transport.AbstractPipedConnection;
import org.jboss.tools.lsp.transport.Connection;
import org.jboss.tools.lsp.transport.TransportMessage;
import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link Connection} implementation using the {@code junix-socket} library.
 */
public class NamedPipeConnection extends AbstractPipedConnection {

	/** The usual Logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(NamedPipeConnection.class);

	private static String OS = System.getProperty("os.name").toLowerCase();

	private final String readFileName;
	private final String writeFileName;

	// used for windows
	private RandomAccessFile writeFile;
	private RandomAccessFile readFile;
	// used on POSIX
	private AFUNIXSocket writeSocket;
	private AFUNIXSocket readSocket;

	/**
	 * Constructor.
	 * @param readFileName the file to use to read incoming data
	 * @param writeFileName the file to use to write outgoing data.
	 */
	public NamedPipeConnection(final String readFileName, final String writeFileName) {
		super("server");
		this.readFileName = readFileName;
		this.writeFileName = writeFileName;
	}

	@Override
	public void send(TransportMessage message) {
		if (message != null)
			outboundQueue.add(message);
	}

	@Override
	public void close() {
		try {
			if (writeFile != null)
				writeFile.close();
			if (readFile != null)
				readFile.close();
			if (readSocket != null)
				readSocket.close();
			if (writeSocket != null)
				writeSocket.close();
		} catch (IOException e) {
			// TODO: handle exception
		}
	}

	@Override
	public InputStream connectReadChannel() throws IOException {
		final File rFile = new File(readFileName);
		if (isWindows()) {
			readFile = new RandomAccessFile(rFile, "rwd");
			return Channels.newInputStream(readFile.getChannel());
		}
		LOGGER.info("Connecting read channel to socket at {}", rFile);
		readSocket = AFUNIXSocket.newInstance();
		readSocket.connect(new AFUNIXSocketAddress(rFile));
		return readSocket.getInputStream();
	}

	@Override
	public OutputStream connectWriteChannel() throws IOException {
		final File wFile = new File(writeFileName);

		if (isWindows()) {
			writeFile = new RandomAccessFile(wFile, "rwd");
			return Channels.newOutputStream(writeFile.getChannel());
		}
		LOGGER.info("Connecting write channel to socket at {}", wFile);
		writeSocket = AFUNIXSocket.newInstance();
		writeSocket.connect(new AFUNIXSocketAddress(wFile));
		return writeSocket.getOutputStream();
	}

	private static boolean isWindows() {
		return (OS.indexOf("win") >= 0);
	}

}
