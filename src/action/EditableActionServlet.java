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

import sitemap.ServletPath;
import sitemap.ViewPath;
import util.HttpUtil;
import util.IStringTransformer;
import util.ResponseWrapper;
import util.UserMessageUtils;
import dao.DAOParams;
import dao.DaoCallSupport;
import dao.tz.TimezoneDao;
import domain.TimezoneInfo;
import domain.UserMessage;

/**
 * Servlet implementation class EditableActionServlet
 */
@WebServlet(ServletPath.EDITABLE)
public class EditableActionServlet extends AServlet {
	
	public static final String TIMEZONE_INFO_DATA_ATTRIBUTE_NAME = "timezoneInfoDataEditable";
	/** Pagination */
	public static final String SELECTOR_CLASS_TIMEZONE_INFO = "timezoneInfoPage";
	/** Modal form */
	public static final String SELECTOR_CLASS_TIMEZONE_INFO_EDIT = "timezoneInfoEdit";
	/** Update form submit */
	public static final String SELECTOR_CLASS_TIMEZONE_INFO_UPDATE = "timezoneInfoUpdate";
	/** Holds the id of a {@link TimezoneInfo} object. */
	public static final String TIMEZONE_ID_PARAM_NAME = "timezoneid"; // keep it lowercase
	/** Holds the abbreviation of a {@link TimezoneInfo} object. */
	public static final String TIMEZONE_ABBREVIATION_PARAM_NAME = "timezoneabbr";
	/** Holds the name of a {@link TimezoneInfo} object. */
	public static final String TIMEZONE_NAME_PARAM_NAME = "timezonename";
	/** Holds the offset of a {@link TimezoneInfo} object. */
	public static final String TIMEZONE_OFFSET_PARAM_NAME = "timezoneoffset";
	
	private static final long serialVersionUID = -5837825589483822995L;
	
	private static final Logger LOGGER = LogManager.getLogger(EditableActionServlet.class);
	
	private final Integer PAGE_SIZE = Integer.valueOf(10);
	
	private final transient TimezoneDao timezoneDao;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public EditableActionServlet() {
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
		this.getServletContext().setAttribute("timezoneInfoDataEditableAttributeName", TIMEZONE_INFO_DATA_ATTRIBUTE_NAME);
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (HttpUtil.acceptsJSON(request)) {
			super.respondWithJson(request, response);
		} else {
			this.prepareRequest(request, this::setDaoCallSupportAttributeForPage);
			this.prepareRequest(request, this::setTotalPagesMapAttribute);
			request.getRequestDispatcher(ViewPath.EDITABLE).forward(request, response);
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
		switch(fragmentClassName) {
			case SELECTOR_CLASS_TIMEZONE_INFO: 
				this.prepareRequest(request, this::setDaoCallSupportAttributeForPage);
				this.includeAsJsonML(ViewPath.FRAGMENT_TIMEZONE_INFO_EDITABLE_PAGE, request, new ResponseWrapper(response), response);
				response.getOutputStream().print(',');
				break;
			case SELECTOR_CLASS_TIMEZONE_INFO_EDIT: 
				this.prepareRequest(request, this::setTimezoneInfoDataAttribute);
				this.includeAsJsonML(ViewPath.FRAGMENT_TIMEZONE_INFO_FORM, request, new ResponseWrapper(response), response);
				response.getOutputStream().print(',');
				break;
			case SELECTOR_CLASS_TIMEZONE_INFO_UPDATE:
				this.executeUpdate(request, this::updateTimezoneInfo);
				break;
			default:
				break;
		}
		// errors
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

	private void executeUpdate(final HttpServletRequest request, final Consumer<HttpServletRequest> consumer) {
		try {
			consumer.accept(request);
		} catch (final Exception e) {
			LOGGER.error("Failed to execute an update", e);
			// the following message will be translated - may differ from local log message
			UserMessageUtils.addUserMessage(request, "Failed to execute an update", UserMessage.Status.ERROR, IStringTransformer.ECHO);
		}
	}

	private void setDaoCallSupportAttributeForPage(final HttpServletRequest request) {
		final DAOParams callParams = new DAOParams();
		callParams.addParameter(
			TimezoneDao.PAGE_PARAMETER_NAME,
			HttpUtil.getParamAsInt(request, SELECTOR_CLASS_TIMEZONE_INFO, Integer.valueOf(1)));
		callParams.addParameter(TimezoneDao.PAGE_SIZE_PARAMETER_NAME, PAGE_SIZE);
		
		request.setAttribute(
			DAO_CALL_SUPPORT_ATTRIBUTE_NAME, 
			new DaoCallSupport(this.timezoneDao, callParams));
	}

	private void setTotalPagesMapAttribute(final HttpServletRequest request) {
		final Map<String, Integer> totalDataPagesMap = new HashMap<>(1);
		totalDataPagesMap.put(
			SELECTOR_CLASS_TIMEZONE_INFO, 
			AServlet.getTotalPages(this.timezoneDao.getTimezoneInfoTotal(), PAGE_SIZE));
		AServlet.setTotalPagesMap(request, totalDataPagesMap);
	}

	private void setTimezoneInfoDataAttribute(final HttpServletRequest request) {
		final DAOParams callParams = new DAOParams();
		callParams.addParameter(
			TimezoneDao.ID_PARAMETER_NAME,
			HttpUtil.getParamAsInt(request, TIMEZONE_ID_PARAM_NAME, null));
		request.setAttribute(
			TIMEZONE_INFO_DATA_ATTRIBUTE_NAME, 
			this.timezoneDao.find(callParams));
	}

	private void updateTimezoneInfo(final HttpServletRequest request) {
		final TimezoneInfo tzInfoOld = 
			this.timezoneDao.findById(HttpUtil.getParamAsInt(request, TIMEZONE_ID_PARAM_NAME, null));
		if(tzInfoOld.getId() != null) {
			this.timezoneDao.update(new TimezoneInfo(
				tzInfoOld.getId(),
				tzInfoOld.getAbbreviation(),
				request.getParameter(TIMEZONE_NAME_PARAM_NAME),
				request.getParameter(TIMEZONE_OFFSET_PARAM_NAME)
				)
			);
		}
	}
}
