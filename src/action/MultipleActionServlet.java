package action;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

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
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (HttpUtil.acceptsJSON(request)) {
			this.prepareRequest(request, this::setTotalPagesMapAttribute);
			super.respondWithJson(request, response);
		} else {
			this.prepareRequest(request, this::setDaoCallSupportAttributeForTimeZoneInfoOne);
			this.prepareRequest(request, this::setDaoCallSupportAttributeForTimeZoneInfoTwo);
			this.prepareRequest(request, this::setTotalPagesMapAttribute);
			request.getRequestDispatcher(ViewPath.MULTIPLE).forward(request, response);
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
		switch (fragmentClassName) {
			case SELECTOR_CLASS_TIMEZONE_INFO_1:
				this.prepareRequest(request, this::setDaoCallSupportAttributeForTimeZoneInfoOne);
				this.includeAsJsonML(ViewPath.FRAGMENT_TIMEZONE_INFO_1_PAGE, request, new ResponseWrapper(response), response);
				break;
			case SELECTOR_CLASS_TIMEZONE_INFO_2: 
				this.prepareRequest(request, this::setDaoCallSupportAttributeForTimeZoneInfoTwo);
				this.includeAsJsonML(ViewPath.FRAGMENT_TIMEZONE_INFO_2_PAGE, request, new ResponseWrapper(response), response);
				break;
			default: 
				break;
		}
		// errors
		response.getOutputStream().print(',');
		this.includeAsJsonML(ViewPath.FRAGMENT_ERROR_LIST, request, new ResponseWrapper(response), response);
	}

	private void prepareRequest(final HttpServletRequest request, final Consumer<HttpServletRequest> consumer) {
		try {
			consumer.accept(request);
		} catch (final Exception e) {
			LOGGER.error("Failed to prepare for rendering data from backend", e);
			// the following message will be translated - may differ from local log message
			UserMessageUtils.addUserMessage(request, "Failed to prepare for rendering data from backend", UserMessage.Status.ERROR, IStringTransformer.ECHO);
		}
	}

	private void setDaoCallSupportAttributeForTimeZoneInfoTwo(final HttpServletRequest request) {
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

	private void setDaoCallSupportAttributeForTimeZoneInfoOne(final HttpServletRequest request) {
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

	private void setTotalPagesMapAttribute(final HttpServletRequest request) {
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
