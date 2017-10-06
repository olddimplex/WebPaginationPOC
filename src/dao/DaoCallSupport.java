package dao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DaoCallSupport {

    private static final Logger LOGGER = LogManager.getLogger(DaoCallSupport.class);

	private final IStreamingDao dao;
	private final DAOParams daoParams;
	private final Logger logger;
	private IStreamingCallback callback;
	
	public DaoCallSupport(final IStreamingDao dao, final DAOParams daoParams, final IStreamingCallback callback, final Logger logger) {
		super();
		this.dao = dao;
		this.daoParams = daoParams;
		this.logger = logger;
		this.callback = callback;
	}
	
	public DaoCallSupport(final IStreamingDao dao, final DAOParams daoParams, final IStreamingCallback callback) {
		this(dao, daoParams, null, null);
	}

	public DaoCallSupport(final IStreamingDao dao, final DAOParams daoParams) {
		this(dao, daoParams, null, null);
	}

	/**
	 * @return the dao
	 */
	public final IStreamingDao getDao() {
		return this.dao;
	}

	/**
	 * @return the daoParams
	 */
	public final DAOParams getDaoParams() {
		return this.daoParams;
	}


	/**
	 * @return the callback
	 */
	public final IStreamingCallback getCallback() {
		return this.callback;
	}

	/**
	 * @return the {@link Logger} currently in use
	 */
	public Logger getLogger() {
		if(this.logger == null) {
			return LOGGER;
		} else {
			return this.logger;
		}
	}

	/**
	 * @param callback the callback to set
	 */
	public final void setCallback(final IStreamingCallback callback) {
		this.callback = callback;
	}

	/**
	 * For streaming database content directly to response output stream.<br/>
	 * Note (when overriding), that all exception handling must be done here.
	 * 
	 * @param request
	 * @param response
	 */
	public void stream(
			final HttpServletRequest request, 
			final HttpServletResponse response
			) {
		try {
			this.getDao().stream(this);
		} catch (final Exception e) {
			this.getLogger().error("Error streaming content", e);
		}
	}
}
