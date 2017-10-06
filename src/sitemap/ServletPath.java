package sitemap;

import action.SingleActionServlet;

public class ServletPath {

	public static final String SINGLE = "/single";
	public static final String SINGLE_WITH_SECONDARY = "/single?" + SingleActionServlet.SHOW_SECONDARY_PAGINATION_CONTROL_PARAM_NAME + "=true";
	public static final String MULTIPLE = "/multiple";
	public static final String EDITABLE = "/editable";

}
