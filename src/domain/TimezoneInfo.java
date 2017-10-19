package domain;

import java.math.BigDecimal;

import util.JsonUtil;

public class TimezoneInfo {
	
	public static final TimezoneInfo EMPTY = new TimezoneInfo(null, null, null, null);

	// BigDecimal is a best practice to avoid scaling issues with large databases
	private final BigDecimal id;
	private final String abbreviation;
	private final String name;
	private final String offset;

	public TimezoneInfo(final BigDecimal id, final String abbreviation, final String name, final String offset) {
		super();
		this.id = id;
		this.abbreviation = abbreviation;
		this.name = name;
		this.offset = offset;
	}

	/**
	 * @return the id
	 */
	public BigDecimal getId() {
		return id;
	}

	/**
	 * @return the abbreviation
	 */
	public String getAbbreviation() {
		return abbreviation;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the offset
	 */
	public String getOffset() {
		return offset;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return JsonUtil.toJsonNoHtmlEscaping(this);
	}
}
