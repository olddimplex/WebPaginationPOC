package action;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;

import dao.DaoCallSupport;
import util.JsonUtil;
import util.ResponseWrapper;

public abstract class AServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6874573652767919424L;

	/** For use with AJAX calls. Holds the marker class of the HTML element to update. */
	public static final String SELECTOR_CLASS_PARAM_NAME = "classname";

	/** For use with AJAX calls only. Holds the target URL of the call */
	public static final String TARGET_URL_PARAMETER_NAME = "target_url";

	/** For use with AJAX calls. Holds the URL of the current page as seen by the browser. */
	public static final String LOCATION_PARAM_NAME = "location";
	/** */
	public static final String QUERY_PARAMETER_NAME = "query";

	/** Default page number to render */
	public static final String PAGE_NUMBER_DEFAULT = "1";

	/**
	 * Name of the request attribute holding the map of <selector class name> =>
	 * <total number of pages available>
	 */
	public static final String TOTAL_PAGES_MAP_ATTRIBUTE_NAME = "totalDataPagesMap";

	/**
	 * Name of the session attribute holding validation errors to be presented to user
	 */
	public static final String ERROR_COLLECTION_ATTRIBUTE_NAME = "errorCollection";

	/**
	 * Name of the session attribute holding messages to be presented to user.
	 * Note this is not intended to hold validation errors.
	 */
	public static final String MESSAGE_COLLECTION_ATTRIBUTE_NAME = "messageCollection";
	/**
	 * Name of the request attribute holding an instance of
	 * {@link DaoCallSupport}
	 */
	public static final String DAO_CALL_SUPPORT_ATTRIBUTE_NAME = "dynamicIncludeDaoCallSupport";
	/**
	 * Name of the request attribute holding the times data view has been rendered
	 */
	public static final String VIEW_COUNT_ATTRIBUTE_NAME = "viewCount";

	@Override
	public void init(final ServletConfig config) throws ServletException {
		super.init(config);
		this.getServletContext().setAttribute("totalDataPagesMapAttributeName", TOTAL_PAGES_MAP_ATTRIBUTE_NAME);
		this.getServletContext().setAttribute("messageCollectionAttributeName", MESSAGE_COLLECTION_ATTRIBUTE_NAME);
		this.getServletContext().setAttribute("viewCountAttributeName", VIEW_COUNT_ATTRIBUTE_NAME);

		this.getServletContext().setAttribute("selectorClassParamName", SELECTOR_CLASS_PARAM_NAME);
		this.getServletContext().setAttribute("targetUrlParamName", TARGET_URL_PARAMETER_NAME);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		this.doGet(request, response);
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

	/**
	 * Prints a common JSON structure to response stream.<br/>
	 * Relies on {@link #respondWithJsonML(HttpServletRequest, HttpServletResponse)}
	 * for the actual content part.
	 */
	protected void respondWithJson(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		final String fragmentClassName = request.getParameter(SELECTOR_CLASS_PARAM_NAME);
		if (fragmentClassName != null) {
			response.setContentType("application/json");
			try {
				response.getOutputStream().print("{\"content\":[");
				this.respondWithJsonML(request, response, fragmentClassName);
				response.getOutputStream().print(']');
				final Object totalDataPagesMap = 
					request.getAttribute(TOTAL_PAGES_MAP_ATTRIBUTE_NAME);
				if(totalDataPagesMap != null) {
					response.getOutputStream().print(",\"meta\":");
					response.getOutputStream().print(JsonUtil.toJsonNoHtmlEscaping(totalDataPagesMap));
				}
				response.getOutputStream().print('}');
			} catch (final Exception e) {
				this.getLogger().error("Failed to process an AJAX request", e);
			}
		}
	}

	/**
	 * Noop implementation, designed for overriding.
	 * 
	 * @param request
	 * @param response
	 */
	protected void respondWithJsonML(
			final HttpServletRequest request, 
			final HttpServletResponse response,
			final String fragmentClassName
			) throws Exception {
	}

	protected abstract Logger getLogger();
}
