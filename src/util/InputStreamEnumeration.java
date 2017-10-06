package util;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Quick and dirty implementation
 */
public class InputStreamEnumeration implements Enumeration<InputStream> {
	
	private final Iterator<InputStream> inputStreamIterator;

	public InputStreamEnumeration(final InputStream...inputStreams) {
		final List<InputStream> inputStreamList = new LinkedList<InputStream>();
		for(final InputStream is : inputStreams) {
			inputStreamList.add(is);
		}
		this.inputStreamIterator = inputStreamList.iterator();
	}

	@Override
	public boolean hasMoreElements() {
		return this.inputStreamIterator != null && this.inputStreamIterator.hasNext();
	}

	@Override
	public InputStream nextElement() {
		if(this.hasMoreElements()) {
			return this.inputStreamIterator.next();
		}
		throw new NoSuchElementException();
	}


}
