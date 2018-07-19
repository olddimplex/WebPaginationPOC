<%@ page pageEncoding="UTF-8" %>
<%@ page import="sitemap.*" %>
<%@ page import="action.*" %>
<%@ page import="java.util.Date" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="cust" uri="/WEB-INF/taglibs/custom.tld" %>
<c:set var="resultListSelectorClassName" scope="page" value="<%=AutocompleteActionServlet.SELECTOR_CLASS_TIMEZONE_INFO %>"/>
<c:set var="selectorClassError" scope="page" value='<%=
			AutocompleteActionServlet.SELECTOR_CLASS_TIMEZONE_INFO
	+ " " + AutocompleteActionServlet.SELECTOR_CLASS_TIMEZONE_INFO_EDIT
	+ " " + AutocompleteActionServlet.SELECTOR_CLASS_TIMEZONE_ABBREVIATION_SUGGESTIONS
%>'/>
<!DOCTYPE html>
<html lang="en">
  <head>
    <!-- Required meta tags -->
<jsp:include page="<%=ViewPath.FRAGMENT_HEAD_HEADER %>"/>
	<link rel="stylesheet" href="<c:url value='/font-awesome-4.7.0/css/font-awesome.min.css'/>">
	<style>
		.typeahead-loading {
			background:url(<c:url value="/img/loading.gif"/>) no-repeat right center/contain content-box;
		}
	</style>
  </head>
  <body>
<jsp:include page="<%=ViewPath.FRAGMENT_NAVIGATION_MENU %>"/>
	<div class="container col-md-10 float-left">
		<form class="form-inline my-3 ajax-update" id="searchForm">
		  <label class="mr-sm-2" for="inlineFormAbbr">Abbreviation:</label>
		  <div class="input-group mb-2 mr-sm-2 mb-sm-0 col-6 ${resultListSelectorClassName}">
<%-- Holy wisdom!!! Appears you cannot set a text input's value (as seen by user) if something has been typed in it - you must recreate the element --%>
<jsp:include page="<%=ViewPath.FRAGMENT_TIMEZONE_SEARCH_AUTOCOMPLETE_INPUT %>"></jsp:include>
		  </div>
		  <input type="hidden" name="classname" value="${resultListSelectorClassName}"/>
		  <button type="submit" class="btn btn-primary">Search</button>
		</form>
		<table class="table table-sm">
		  <thead>
		    <tr>
		      <th>Abbr.</th>
		      <th>Name</th>
		      <th>UTC offset</th>
		      <th/>
		    </tr>
		  </thead>
		  <tbody class="${resultListSelectorClassName}">
		  	<jsp:include page="<%=ViewPath.FRAGMENT_TIMEZONE_INFO_EDITABLE_PAGE %>"/>
		  </tbody>
		</table>
		<jsp:include page="<%=ViewPath.FRAGMENT_PAGINATION %>">
			<jsp:param value="${resultListSelectorClassName}" name="selectorClass"/>
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
	      <div class="modal-body <%=AutocompleteActionServlet.SELECTOR_CLASS_TIMEZONE_INFO_EDIT %>"></div>
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
<jsp:include page="<%=ViewPath.FRAGMENT_BODY_FOOTER %>"/>
	<div class="${selectorClassError}">
<jsp:include page="<%=ViewPath.FRAGMENT_ERROR_LIST %>"/>
	</div>
  </body>
</html>