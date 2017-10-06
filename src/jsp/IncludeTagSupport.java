package jsp;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dao.DaoCallSupport;
import dao.IStreamingCallback;

/**
 * Custom JSP tag to stream database content directly to response output stream.
 */
public final class IncludeTagSupport extends SimpleTagSupport {

    private static final Logger LOGGER = LogManager.getLogger(IncludeTagSupport.class);
    
    private String dao;
    private String dataObjectKey;
    private String view;
    private String viewCount;

    /**
	 * @param daoKey the daoKey to set
	 */
	public void setDao(final String daoKey) {
		this.dao = daoKey;
	}

	/**
	 * @param dataObjectKey the dataObjectKey to set
	 */
	public void setDataObject(final String dataObjectKey) {
		this.dataObjectKey = dataObjectKey;
	}

	/**
	 * @param viewPath the viewPath to set
	 */
	public void setView(final String viewPath) {
		this.view = viewPath;
	}

	/**
	 * @param viewCount request attribute name to hold the number of view invocations 
	 */
	public void setViewCount(String viewCount) {
		this.viewCount = viewCount;
	}

	/**
     * @see javax.servlet.jsp.tagext.SimpleTagSupport#doTag()
     */
	@Override
    public void doTag() throws JspException, IOException {
    	getJspContext().getOut().flush();
    	final DaoCallSupport daoCallSupport = 
    		(DaoCallSupport)this.getRequest().getAttribute(this.dao);
    	final RequestDispatcher requestDispatcher = 
    		this.getRequest().getRequestDispatcher(this.view);
    	final int[] counters = new int[] {0};
    	if((daoCallSupport != null) && (daoCallSupport.getDao() != null)) {
    		daoCallSupport.setCallback(new IStreamingCallback() {
				private final IncludeTagSupport parent = IncludeTagSupport.this;
				@Override
				public void call(final Object dataObject) throws SQLException {
					final Object oldDataObject = parent.getRequest().getAttribute(parent.dataObjectKey);
					if(dataObject != null) {
						parent.getRequest().setAttribute(parent.dataObjectKey, dataObject);
					}
					try {
						counters[0]++;
						requestDispatcher.include(parent.getRequest(), parent.getResponse());
					} catch (Exception e) {
						throw new SQLException("Wrapped to match the interface", e);
					} finally {
						parent.getRequest().setAttribute(parent.dataObjectKey, oldDataObject);
					}
				}
			});
			daoCallSupport.stream(this.getRequest(), this.getResponse());
    	} else {
			try {
				counters[0]++;
				requestDispatcher.include(this.getRequest(), this.getResponse());
			} catch (ServletException e) {
				throw new JspException("Wrapped to match the interface", e);
			}
    	}
    	if(this.viewCount != null && this.viewCount.length() > 0 && counters[0] > 0) {
    		getRequest().setAttribute(this.viewCount, Integer.valueOf(counters[0]));
    	}
    }

	private HttpServletResponse getResponse() {
		return (HttpServletResponse) this.getPageContext().getResponse();
	}

	private HttpServletRequest getRequest() {
		return (HttpServletRequest)this.getPageContext().getRequest();
	}

	private PageContext getPageContext() {
		return (PageContext) getJspContext();
	}
}
