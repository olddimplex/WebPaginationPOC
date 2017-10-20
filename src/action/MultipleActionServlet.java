package action;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
import util.JsonUtil;
import util.ResponseWrapper;
import dao.DAOParams;
import dao.DaoCallSupport;
import dao.tz.TimezoneDao;

/**
 * Servlet implementation class MultipleActionServlet
 */
@WebServlet(ServletPath.MULTIPLE)
public class MultipleActionServlet extends AServlet {
	
	public static final String TIMEZONE_INFO_DATA_ATTRIBUTE_NAME = "timezoneInfoData";
	/** Pagination 1 */
	public static final String SELECTOR_CLASS_TIMEZONE_INFO_1 = "tzInfoOnePage";
	/** Pagination 2 */
	public static final String SELECTOR_CLASS_TIMEZONE_INFO_2 = "tzInfoTwoPage";

	/** Name of request attribute holding an instance of {@link DaoCallSupport} */
	public static final String TIMEZONE_INFO_1_DAO_CALL_SUPPORT_ATTRIBUTE_NAME = "tzInfoOneDaoCallSupport";

	/** Name of request attribute holding an instance of {@link DaoCallSupport} */
	public static final String TIMEZONE_INFO_2_DAO_CALL_SUPPORT_ATTRIBUTE_NAME = "tzInfoTwoDaoCallSupport";
	
	private static final long serialVersionUID = -5837825589483822995L;
	
	private static final Logger LOGGER = LogManager.getLogger(MultipleActionServlet.class);
	
	private final transient TimezoneDao timezoneDao;

	private final Integer PAGE_SIZE = Integer.valueOf(5);
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MultipleActionServlet() {
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
		this.getServletContext().setAttribute("timezoneInfoDataMultipleAttributeName", TIMEZONE_INFO_DATA_ATTRIBUTE_NAME);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doGet(request, response);
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (HttpUtil.acceptsJSON(request)) {
			this.setTotalPagesMapAttribute(request);
			this.respondWithJson(request, response);
		} else {
			try {
				this.setDaoCallSupportAttributeForTimeZoneInfoOne(request);
				this.setDaoCallSupportAttributeForTimeZoneInfoTwo(request);
				this.setTotalPagesMapAttribute(request);
			} catch (final Exception e) {
				this.getLogger().error("Failed to populate", e);
			} finally {
				request.getRequestDispatcher(ViewPath.MULTIPLE).forward(request, response);
			}
		}
	}

	/*
	 *
	 */
	private void respondWithJson(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		final String fragmentClassName = request.getParameter(SELECTOR_CLASS_PARAM_NAME);
		if (fragmentClassName != null) {
			try {
				response.setContentType("application/json");
				response.getOutputStream().print("{\"content\":[");
				switch (fragmentClassName) {
					case SELECTOR_CLASS_TIMEZONE_INFO_1:
						setDaoCallSupportAttributeForTimeZoneInfoOne(request);
						this.includeAsJsonML(ViewPath.FRAGMENT_TIMEZONE_INFO_1_PAGE, request, new ResponseWrapper(response), response);
						break;
					case SELECTOR_CLASS_TIMEZONE_INFO_2: 
						setDaoCallSupportAttributeForTimeZoneInfoTwo(request);
						this.includeAsJsonML(ViewPath.FRAGMENT_TIMEZONE_INFO_2_PAGE, request, new ResponseWrapper(response), response);
						break;
					default: 
						break;
				}
//				response.getOutputStream().print(',');
//				// errors
//				this.includeErrorListAsJsonML(request, response);
				response.getOutputStream().print(']');
				final Object totalDataPagesMap = 
					request.getAttribute(AServlet.TOTAL_PAGES_MAP_ATTRIBUTE_NAME);
				if(totalDataPagesMap != null) {
					response.getOutputStream().print(",\"meta\":");
					response.getOutputStream().print(JsonUtil.toJsonNoHtmlEscaping(totalDataPagesMap));
				}
				response.getOutputStream().print('}');
			} catch (final Exception e) {
				LOGGER.error("Failed to process an AJAX request", e);
			}
		}
	}

	public void setDaoCallSupportAttributeForTimeZoneInfoTwo(final HttpServletRequest request) {
		final DAOParams tzInfoTwoCallParams = new DAOParams();
		tzInfoTwoCallParams.addParameter(
			TimezoneDao.PAGE_PARAMETER_NAME,
			HttpUtil.getParamAsInt(request, SELECTOR_CLASS_TIMEZONE_INFO_2, Integer.valueOf(1)));
		tzInfoTwoCallParams.addParameter(
			TimezoneDao.PAGE_SIZE_PARAMETER_NAME, 
			PAGE_SIZE);
		request.setAttribute(
			TIMEZONE_INFO_2_DAO_CALL_SUPPORT_ATTRIBUTE_NAME, 
			new DaoCallSupport(this.timezoneDao, tzInfoTwoCallParams));
	}

	public void setDaoCallSupportAttributeForTimeZoneInfoOne(final HttpServletRequest request) {
		final DAOParams tzInfoOneCallParams = new DAOParams();
		tzInfoOneCallParams.addParameter(
			TimezoneDao.PAGE_PARAMETER_NAME,
			HttpUtil.getParamAsInt(request, SELECTOR_CLASS_TIMEZONE_INFO_1, Integer.valueOf(1)));
		tzInfoOneCallParams.addParameter(
			TimezoneDao.PAGE_SIZE_PARAMETER_NAME, 
			PAGE_SIZE);
		request.setAttribute(
			TIMEZONE_INFO_1_DAO_CALL_SUPPORT_ATTRIBUTE_NAME, 
			new DaoCallSupport(this.timezoneDao, tzInfoOneCallParams));
	}

	public void setTotalPagesMapAttribute(final HttpServletRequest request) {
		final Map<String, Integer> totalDataPagesMap = new HashMap<>(1);
		totalDataPagesMap.put(
			SELECTOR_CLASS_TIMEZONE_INFO_1, 
			AServlet.getTotalPages(this.timezoneDao.getTimezoneInfoTotal(), PAGE_SIZE));
		totalDataPagesMap.put(
			SELECTOR_CLASS_TIMEZONE_INFO_2, 
			AServlet.getTotalPages(this.timezoneDao.getTimezoneInfoTotal(), PAGE_SIZE));
		AServlet.setTotalPagesMap(request, totalDataPagesMap);
	}

}
