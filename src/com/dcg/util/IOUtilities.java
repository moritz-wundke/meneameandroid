package com.dcg.util;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.os.Environment;

/**
 * Based on Romain Guys IOUtilities from the shelves app
 * (http://code.google.com/p/shelves/)
 * 
 * @author Moritz Wundke (b.thax.dcg@gmail.com)
 */
public class IOUtilities {
	private static final String LOG_TAG = "IOUtilities";

	public static final int IO_BUFFER_SIZE = 4 * 1024;

	public static File getExternalFile(String file) {
		return new File(Environment.getExternalStorageDirectory(), file);
	}

	/**
	 * Copy the content of the input stream into the output stream, using a
	 * temporary byte array buffer whose size is defined by
	 * {@link #IO_BUFFER_SIZE}.
	 * 
	 * @param in
	 *            The input stream to copy from.
	 * @param out
	 *            The output stream to copy to.
	 * 
	 * @throws java.io.IOException
	 *             If any error occurs during the copy.
	 */
	public static void copy(InputStream in, OutputStream out)
			throws IOException {
		byte[] b = new byte[IO_BUFFER_SIZE];
		int read;
		while ((read = in.read(b)) != -1) {
			out.write(b, 0, read);
		}
	}

	/**
	 * Closes the specified stream.
	 * 
	 * @param stream
	 *            The stream to close.
	 */
	public static void closeStream(Closeable stream) {
		if (stream != null) {
			try {
				stream.close();
			} catch (IOException e) {
				android.util.Log.e(LOG_TAG, "Could not close stream", e);
			}
		}
	}
}
