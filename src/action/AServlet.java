package action;

import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;

import util.JsonUtil;
import util.ResponseWrapper;
import dao.DaoCallSupport;

public abstract class AServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6874573652767919424L;

	/** For use with AJAX calls. Holds the marker class of the HTML element to update. */
	public static final String SELECTOR_CLASS_PARAM_NAME = "classname";

	/** For use with AJAX calls. Holds the URL of the current page as seen by the browser. */
	public static final String LOCATION_PARAM_NAME = "location";

	/** Default page number to render */
	public static final String PAGE_NUMBER_DEFAULT = "1";

	/**
	 * Name of the request attribute holding the map of <selector class name> =>
	 * <total number of pages available>
	 */
	public static final String TOTAL_PAGES_MAP_ATTRIBUTE_NAME = "totalDataPagesMap";

	/**
	 * Name of the request attribute holding an instance of
	 * {@link DaoCallSupport}
	 */
	public static final String DAO_CALL_SUPPORT_ATTRIBUTE_NAME = "dynamicIncludeDaoCallSupport";

	@Override
	public void init(final ServletConfig config) throws ServletException {
		super.init(config);
		this.getServletContext().setAttribute("totalDataPagesMapAttributeName", TOTAL_PAGES_MAP_ATTRIBUTE_NAME);
	}

	/**
	 * Calculates the total pages as expected by the pagination solution.
	 * 
	 * @param totalRows
	 * @param pageSize
	 * @return
	 */
	public static final int getTotalPages(final int totalRows, final int pageSize) {
		int totalPages = totalRows / pageSize;
		if (totalRows % pageSize != 0) {
			totalPages++;
		}
		return totalPages;
	}

	/**
	 * Set the map of <selector class name> => <total pages> request attribute
	 * as expected by the pagination solution.
	 *
	 * @param request
	 * @param totalPagesMap
	 */
	public static final void setTotalPagesMap(final HttpServletRequest request, final Map<String, Integer> totalPagesMap) {
		request.setAttribute(TOTAL_PAGES_MAP_ATTRIBUTE_NAME, totalPagesMap);
	}

	/**
	 * For JSONML format, see: <a href="http://www.jsonml.org/">jsonml.org</a>
	 *
	 * @see {@link JsonUtil#toJsonML(java.io.InputStream, java.io.OutputStream)}
	 */
	protected final void includeAsJsonML(
			final String resourceUri, 
			final HttpServletRequest request, 
			final ResponseWrapper httpResponseWrapper,
			final HttpServletResponse response) {
		try {
			request.getRequestDispatcher(resourceUri).include(request, httpResponseWrapper);
			JsonUtil.toJsonML(httpResponseWrapper.getBytes(), response.getOutputStream());
		} catch (final Exception e) {
			this.getLogger().error("INTERNAL SERVER ERROR", e);
		}
	}

	protected abstract Logger getLogger();
}
