package util;

import net.sf.saxon.s9api.ExtensionFunction;
import net.sf.saxon.s9api.ItemType;
import net.sf.saxon.s9api.OccurrenceIndicator;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.SequenceType;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmValue;

public class JsonUtilExtensionFunction implements ExtensionFunction {

	private static final QName qName = new QName("http://experian.com/json-util", "toJsonNoHtmlEscaping");

	@Override
	public XdmValue call(final XdmValue[] arg0) throws SaxonApiException {
		final Object argument = ((XdmAtomicValue) arg0[0]).getStringValue();
		return new XdmAtomicValue(JsonUtil.toJsonNoHtmlEscaping(argument));
	}

	@Override
	public SequenceType[] getArgumentTypes() {
		return new SequenceType[] { SequenceType.makeSequenceType(ItemType.STRING, OccurrenceIndicator.ONE) };
	}

	@Override
	public QName getName() {
		return qName;
	}

	@Override
	public SequenceType getResultType() {
		return SequenceType.makeSequenceType(ItemType.STRING, OccurrenceIndicator.ONE);
	}

}
