package action;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dao.DAOParams;
import dao.DaoCallSupport;
import dao.tz.TimezoneDao;
import domain.UserMessage;
import sitemap.ServletPath;
import sitemap.ViewPath;
import util.HttpUtil;
import util.IStringTransformer;
import util.ResponseWrapper;
import util.UserMessageUtils;

/**
 * Servlet implementation class SingleActionServlet
 */
@WebServlet(ServletPath.SINGLE)
public class SingleActionServlet extends AServlet {
	
	public static final String TIMEZONE_INFO_DATA_ATTRIBUTE_NAME = "timezoneInfoData";
	/** Pagination */
	public static final String SELECTOR_CLASS_TIMEZONE_INFO = "timezoneInfoPage";
	
	public static final String SHOW_SECONDARY_PAGINATION_CONTROL_PARAM_NAME = "show-secondary";
	
	private static final long serialVersionUID = -5837825589483822995L;
	
	private static final Logger LOGGER = LogManager.getLogger(SingleActionServlet.class);
	
	private final Integer PAGE_SIZE = Integer.valueOf(10);
	
	private final transient TimezoneDao timezoneDao;  
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SingleActionServlet() {
        super();
        this.timezoneDao = new TimezoneDao();
    }

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		this.getServletContext().setAttribute("timezoneInfoDataAttributeName", TIMEZONE_INFO_DATA_ATTRIBUTE_NAME);
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
			if (HttpUtil.acceptsJSON(request)) {
				super.respondWithJson(request, response);
			} else {
				this.setDataAttributes(request);
				request.getRequestDispatcher(ViewPath.SINGLE).forward(request, response);
			}
	}

	/**
	 * @see {@link AServlet#respondWithJson(HttpServletRequest, HttpServletResponse)}
	 */
	@Override
	protected void respondWithJsonML(
			final HttpServletRequest request,
			final HttpServletResponse response,
			final String fragmentClassName
			) throws Exception {
		this.setDataAttributes(request);
		this.includeAsJsonML(ViewPath.FRAGMENT_TIMEZONE_INFO_PAGE, request, new ResponseWrapper(response), response);
		// errors
		response.getOutputStream().print(',');
		this.includeAsJsonML(ViewPath.FRAGMENT_ERROR_LIST, request, new ResponseWrapper(response), response);
	}

	private void setDataAttributes(final HttpServletRequest request) {
		try {
			setDaoCallSupportAttributeForPage(request);
			setTotalPagesMapAttribute(request);
		} catch (final Exception e) {
			LOGGER.error("Failed to prepare for rendering data from backend", e);
			// the following message will be translated - may differ from local log message
			UserMessageUtils.addUserMessage(request, "Failed to prepare for rendering data from backend", UserMessage.Status.ERROR, IStringTransformer.ECHO);
		}
	}

	private void setDaoCallSupportAttributeForPage(final HttpServletRequest request) {
		final DAOParams callParams = new DAOParams();
		callParams.addParameter(
			TimezoneDao.PAGE_PARAMETER_NAME,
			HttpUtil.getParamAsInt(request, SELECTOR_CLASS_TIMEZONE_INFO, Integer.valueOf(1)));
		callParams.addParameter(TimezoneDao.PAGE_SIZE_PARAMETER_NAME, PAGE_SIZE);
		request.setAttribute(DAO_CALL_SUPPORT_ATTRIBUTE_NAME, new DaoCallSupport(this.timezoneDao, callParams));
	}

	private void setTotalPagesMapAttribute(final HttpServletRequest request) {
		final Map<String, Integer> totalDataPagesMap = new HashMap<>(1);
		totalDataPagesMap.put(
			SELECTOR_CLASS_TIMEZONE_INFO, 
			AServlet.getTotalPages(this.timezoneDao.getTimezoneInfoTotal(), PAGE_SIZE));
		AServlet.setTotalPagesMap(request, totalDataPagesMap);
	}

}
