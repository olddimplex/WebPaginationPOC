<%@ page pageEncoding="UTF-8" %>
<%@ page import="sitemap.*" %>
<%@ page import="action.*" %>
<%@ taglib prefix="cust" uri="/WEB-INF/taglibs/custom.tld" %>
<cust:include 
	dao="<%=MultipleActionServlet.TIMEZONE_INFO_2_DAO_CALL_SUPPORT_ATTRIBUTE_NAME %>" 
	dataObject="${timezoneInfoDataMultipleAttributeName}" 
	view="<%=ViewPath.FRAGMENT_TIMEZONE_INFO_DATA_MULTIPLE %>"
/>
