<%@ page pageEncoding="UTF-8" %>
<%@ page import="sitemap.*" %>
<%@ page import="action.*" %>
<%@ page import="java.util.Date"  %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="cust" uri="/WEB-INF/taglibs/custom.tld" %>
<c:set var="selectorClassName" scope="page" value="<%=EditableActionServlet.SELECTOR_CLASS_TIMEZONE_INFO %>"/>
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
		      <th/>
		    </tr>
		  </thead>
		  <tbody class="${selectorClassName}">
		  	<jsp:include page="<%=ViewPath.FRAGMENT_TIMEZONE_INFO_EDITABLE_PAGE %>"/>
		  </tbody>
		</table>
		<jsp:include page="<%=ViewPath.FRAGMENT_PAGINATION %>">
			<jsp:param value="${selectorClassName}" name="selectorClass"/>
		</jsp:include>
	</div>
<%-- Modals --%>
	<div class="modal fade" id="editModal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel" aria-hidden="true">
	  <div class="modal-dialog" role="document">
	    <div class="modal-content">
	      <div class="modal-header">
	        <h5 class="modal-title" id="exampleModalLabel">Modal title</h5>
	        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
	          <span aria-hidden="true">&times;</span>
	        </button>
	      </div>
	      <div class="modal-body <%=EditableActionServlet.SELECTOR_CLASS_TIMEZONE_INFO_EDIT %>"></div>
	    </div>
	  </div>
	</div>
<%-- 
	The script below is intentionally made non-executable. It is solely used to provide textual content to JS code.
	The"style" attribute is used because of the URL handling (prepending the context path).
	The width and height are aligned with background image dimensions.
--%>
	<script type="text/template" data-template="loading-indicator">
		<div style="background:url(<c:url value='/img/loading.gif' />) no-repeat center center; width:100%; height:32px;">&nbsp;</div>
	</script>
    <!-- jQuery first, then Tether, then Bootstrap JS. -->
    <script src="<c:url value='/js/jquery-3.1.1.min.js' />"></script>
    <script src="<c:url value='/js/tether.min.js' />"></script>
    <script src="<c:url value='/js/bootstrap.min.js' />"></script>
    <script src="<c:url value='/js/script-data.js' />?t=<%=new Date().getTime() %>"></script>
  </body>
</html>