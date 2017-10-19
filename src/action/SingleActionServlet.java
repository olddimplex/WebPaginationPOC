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

import sitemap.ServletPath;
import sitemap.ViewPath;
import util.HttpUtil;
import util.ResponseWrapper;
import dao.DAOParams;
import dao.DaoCallSupport;
import dao.tz.TimezoneDao;

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
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		final Integer pageSize = Integer.valueOf(10);
		try {
			final DAOParams callParams = new DAOParams();
			callParams.addParameter(
				TimezoneDao.PAGE_PARAMETER_NAME,
				HttpUtil.getParamAsInt(request, SELECTOR_CLASS_TIMEZONE_INFO, Integer.valueOf(1)));
			callParams.addParameter(TimezoneDao.PAGE_SIZE_PARAMETER_NAME, pageSize);

			final Map<String, Integer> totalDataPagesMap = new HashMap<>(1);
			totalDataPagesMap.put(
				SELECTOR_CLASS_TIMEZONE_INFO, 
				AServlet.getTotalPages(this.timezoneDao.getTimezoneInfoTotal(), pageSize));
			AServlet.setTotalPagesMap(request, totalDataPagesMap);

			request.setAttribute(DAO_CALL_SUPPORT_ATTRIBUTE_NAME, new DaoCallSupport(this.timezoneDao, callParams));

			if (HttpUtil.acceptsJSON(request)) {
				this.respondWithJson(request, response);
			} else {
				request.getRequestDispatcher(ViewPath.SINGLE).forward(request, response);
			}
		} catch (final Exception e) {
			this.getLogger().error("Failed to populate", e);
		}
	}

	/*
	 *
	 */
	private void respondWithJson(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		response.getOutputStream().print('[');
		// data
		this.includeAsJsonML(ViewPath.FRAGMENT_TIMEZONE_INFO_PAGE, request, new ResponseWrapper(response), response);
//		response.getOutputStream().print(',');
//		// errors
//		this.includeErrorListAsJsonML(request, response);
		response.getOutputStream().print(']');
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doGet(request, response);
	}

}
