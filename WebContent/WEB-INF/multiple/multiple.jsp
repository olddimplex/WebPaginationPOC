<%@ page pageEncoding="UTF-8" %>
<%@ page import="sitemap.*" %>
<%@ page import="action.*" %>
<%@ page import="java.util.Date"  %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="cust" uri="/WEB-INF/taglibs/custom.tld" %>
<c:set var="selectorClassTimezoneInfoOne" scope="page" value="<%=MultipleActionServlet.SELECTOR_CLASS_TIMEZONE_INFO_1 %>"/>
<c:set var="selectorClassTimezoneInfoTwo" scope="page" value="<%=MultipleActionServlet.SELECTOR_CLASS_TIMEZONE_INFO_2 %>"/>
<!DOCTYPE html>
<html lang="en">
  <head>
    <!-- Required meta tags -->
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">

    <!-- Bootstrap CSS -->
    <link rel="stylesheet" href="<c:url value='/css/bootstrap.min.css'/>">
	<link rel="stylesheet" href="<c:url value='/css/style-min.css'/>?t=<%=new Date().getTime() %>">
  </head>
  <body>
  	<div id="loader" class="loadcloak"></div>
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
    <!-- jQuery first, then Tether, then Bootstrap JS. -->
    <script src="<c:url value='/js/jquery-3.1.1.min.js'/>"></script>
    <script src="<c:url value='/js/tether.min.js'/>"></script>
    <script src="<c:url value='/js/bootstrap.min.js'/>"></script>
    <script src="<c:url value='/js/script-data.js'/>?t=<%=new Date().getTime() %>"></script>
  </body>
</html>