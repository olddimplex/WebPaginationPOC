<%@ page pageEncoding="UTF-8" %>
<%@ page import="sitemap.*" %>
<%@ page import="action.*" %>
<%@ taglib prefix="cust" uri="/WEB-INF/taglibs/custom.tld" %>
<cust:include 
	dao="<%=AServlet.DAO_CALL_SUPPORT_ATTRIBUTE_NAME %>" 
	dataObject="<%=EditableActionServlet.TIMEZONE_INFO_DATA_ATTRIBUTE_NAME %>" 
	view="<%=ViewPath.FRAGMENT_TIMEZONE_INFO_DATA_EDITABLE %>"
/>
