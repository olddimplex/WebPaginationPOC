<%@ page pageEncoding="UTF-8" %>
<%@ page import="sitemap.*" %>
<%@ page import="action.*" %>
<%@ page import="java.util.Date"  %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="cust" uri="/WEB-INF/taglibs/custom.tld" %>
<c:set var="selectorClassTimezoneInfoOne" scope="page" value="<%=MultipleActionServlet.SELECTOR_CLASS_TIMEZONE_INFO_1 %>"/>
<c:set var="selectorClassTimezoneInfoTwo" scope="page" value="<%=MultipleActionServlet.SELECTOR_CLASS_TIMEZONE_INFO_2 %>"/>
<c:set var="selectorClassError" scope="page" value='<%=
			MultipleActionServlet.SELECTOR_CLASS_TIMEZONE_INFO_1
	+ " " + MultipleActionServlet.SELECTOR_CLASS_TIMEZONE_INFO_2
%>'/>
<!DOCTYPE html>
<html lang="en">
  <head>
	<jsp:include page="<%=ViewPath.FRAGMENT_HEAD_HEADER %>"/>
  </head>
  <body>
	<jsp:include page="<%=ViewPath.FRAGMENT_NAVIGATION_MENU %>"/>
	<div class="container col-md-10 float-left">
		<table class="table table-sm">
		  <thead>
		    <tr>
		      <th>Abbr.</th>
		      <th>Name</th>
		      <th>UTC offset</th>
		    </tr>
		  </thead>
		  <tbody class="${selectorClassTimezoneInfoOne}">
		  	<jsp:include page="<%=ViewPath.FRAGMENT_TIMEZONE_INFO_1_PAGE %>" />
		  </tbody>
		</table>
		<jsp:include page="<%=ViewPath.FRAGMENT_PAGINATION %>">
			<jsp:param value="${selectorClassTimezoneInfoOne}" name="selectorClass"/>
		</jsp:include>
		<table class="table table-sm">
		  <thead>
		    <tr>
		      <th>Abbr.</th>
		      <th>Name</th>
		      <th>UTC offset</th>
		    </tr>
		  </thead>
		  <tbody class="${selectorClassTimezoneInfoTwo}">
		  	<jsp:include page="<%=ViewPath.FRAGMENT_TIMEZONE_INFO_2_PAGE %>" />
		  </tbody>
		</table>
		<jsp:include page="<%=ViewPath.FRAGMENT_PAGINATION %>">
			<jsp:param value="${selectorClassTimezoneInfoTwo}" name="selectorClass"/>
		</jsp:include>
	</div>
<jsp:include page="<%=ViewPath.FRAGMENT_BODY_FOOTER %>"/>
	<div class="${selectorClassError}">
<jsp:include page="<%=ViewPath.FRAGMENT_ERROR_LIST %>"/>
	</div>
  </body>
</html>