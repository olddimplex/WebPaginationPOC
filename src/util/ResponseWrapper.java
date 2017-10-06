package util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import filter.RequestEnrichmentFilter;

/**
 * The objects of this class are NOT thread-safe.
 */
public class ResponseWrapper extends HttpServletResponseWrapper {
	
	private ByteArrayOutputStream baos;
    private ServletOutputStream outputStream;
    private PrintWriter printWriter;

	public ResponseWrapper(final HttpServletResponse response) {
		super(response);
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponseWrapper#getOutputStream()
	 */
	@Override
	public final ServletOutputStream getOutputStream() throws IOException {
        if (this.printWriter != null) {
            throw new IllegalStateException("#getWriter() is already called.");
        }
		return this.getOutputStreamCopier();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponseWrapper#getWriter()
	 */
	@Override
	public final PrintWriter getWriter() throws IOException {
        if (this.outputStream != null) {
            throw new IllegalStateException("#getOutputStream() is already called.");
        }
		return this.getWriterCopier();
	}

	private ServletOutputStream getOutputStreamCopier() {
		if(this.outputStream == null) {
			this.baos = new ByteArrayOutputStream();
			this.outputStream = new ServletOutputStream() {
			    @Override
			    public void write(final int b) throws IOException {
			    	ResponseWrapper.this.baos.write(b);
			    }

				@Override
				public boolean isReady() {
					// TODO Auto-generated method stub
					return false;
				}

				@Override
				public void setWriteListener(WriteListener arg0) {
					// TODO Auto-generated method stub
				}
			};
		}
		return this.outputStream;
	}

	private PrintWriter getWriterCopier() throws UnsupportedEncodingException {
		if(this.printWriter == null) {
			this.baos = new ByteArrayOutputStream();
			this.printWriter = new PrintWriter(new OutputStreamWriter(this.baos, RequestEnrichmentFilter.CHARACTER_ENCODING));
		}
		return this.printWriter;
	}

	/**
	 * @return
	 */
	public final byte[] getBytes() {
		if(this.baos != null) {
			this.flush();
			return this.baos.toByteArray();
		}
		return null;
	}

	/**
	 * @return
	 */
	public final String getBuffer() {
		if(this.baos != null) {
			this.flush();
			return this.baos.toString();
		}
		return null;
	}

	/**
	 * 
	 */
	public final void flush() {
		if(this.printWriter != null) {
			this.printWriter.flush();
		}
	}
}
