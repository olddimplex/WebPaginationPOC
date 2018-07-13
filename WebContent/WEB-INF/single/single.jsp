<%@ page pageEncoding="UTF-8" %>
<%@ page import="sitemap.*" %>
<%@ page import="action.*" %>
<%@ page import="java.util.Date"  %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="cust" uri="/WEB-INF/taglibs/custom.tld" %>
<c:set var="selectorClassName" scope="page" value="<%=SingleActionServlet.SELECTOR_CLASS_TIMEZONE_INFO %>"/>
<c:set var="showSecondaryPaginationControlParamName" scope="page" value="<%=SingleActionServlet.SHOW_SECONDARY_PAGINATION_CONTROL_PARAM_NAME %>"/>
<!DOCTYPE html>
<html lang="en">
  <head>
	<jsp:include page="<%=ViewPath.FRAGMENT_HEAD_HEADER %>"/>
  </head>
  <body>
  	<!-- div id="loader" class="loadcloak"></div -->
	<jsp:include page="<%=ViewPath.FRAGMENT_NAVIGATION_MENU %>"/>
	<div class="container col-md-10 float-left">
<c:if test="${not empty param[showSecondaryPaginationControlParamName]}">
		<jsp:include page="<%=ViewPath.FRAGMENT_PAGINATION %>">
			<jsp:param value="${selectorClassName}" name="selectorClass"/>
		</jsp:include>
</c:if>
		<table class="table table-sm">
		  <thead>
		    <tr>
		      <th>Abbr.</th>
		      <th>Name</th>
		      <th>UTC offset</th>
		    </tr>
		  </thead>
		  <tbody class="${selectorClassName}">
		  	<jsp:include page="<%=ViewPath.FRAGMENT_TIMEZONE_INFO_PAGE %>"/>
		  </tbody>
		</table>
<c:choose><c:when test="${not empty param[showSecondaryPaginationControlParamName]}">
		<jsp:include page="<%=ViewPath.FRAGMENT_PAGINATION_SECONDARY %>">
			<jsp:param value="${selectorClassName}" name="selectorClass"/>
		</jsp:include>
</c:when><c:otherwise>
		<jsp:include page="<%=ViewPath.FRAGMENT_PAGINATION %>">
			<jsp:param value="${selectorClassName}" name="selectorClass"/>
		</jsp:include>
</c:otherwise></c:choose>
	</div>
	<jsp:include page="<%=ViewPath.FRAGMENT_BODY_FOOTER %>"/>
  </body>
</html>