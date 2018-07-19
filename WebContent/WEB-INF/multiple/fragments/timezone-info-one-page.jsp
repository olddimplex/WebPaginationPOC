<%@ page pageEncoding="UTF-8" %>
<%@ page import="sitemap.*" %>
<%@ page import="action.*" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="cust" uri="/WEB-INF/taglibs/custom.tld" %>
<% request.removeAttribute(AServlet.VIEW_COUNT_ATTRIBUTE_NAME); %>
<cust:include 
	dao="<%=MultipleActionServlet.TIMEZONE_INFO_1_DAO_CALL_SUPPORT_ATTRIBUTE_NAME %>"
	dataObject="${timezoneInfoDataMultipleAttributeName}" 
	view="<%=ViewPath.FRAGMENT_TIMEZONE_INFO_DATA_MULTIPLE %>"
	viewCount="${viewCountAttributeName}"
/>
<c:if test="${empty requestScope[viewCountAttributeName]}">
	<tr><td colspan="3" class="text-center">No data</td></tr>
</c:if>
