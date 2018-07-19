<%@ page pageEncoding="UTF-8" %>
<%@ page import="sitemap.*" %>
<%@ page import="action.*" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="timezoneInfoData" scope="page" value="${requestScope[timezoneInfoDataEditableAttributeName]}"/>
<c:set var="timezoneIdParamName" scope="page" value="<%=AutocompleteActionServlet.TIMEZONE_ID_PARAM_NAME %>"/>
<c:set var="timezoneNameParamName" scope="page" value="<%=AutocompleteActionServlet.TIMEZONE_NAME_PARAM_NAME %>"/>
<c:set var="timezoneOffsetParamName" scope="page" value="<%=AutocompleteActionServlet.TIMEZONE_OFFSET_PARAM_NAME %>"/>
<%-- 
	Note the following must be a valid XML.
	Any text entity (like &nbsp;) must be declared unless it is a standard XML entity (&amp; | &lt; | &gt; | &quot; | &apos;),
	See also: JsonUtil.DOCUMENT_PREFIX
--%>
<c:if test="${not empty timezoneInfoData}">
	<form
		class="ajax-update" 
		method="post"
		data-post_event_name="submit"
		data-post_event_target="#searchForm"
		>
      <input type="hidden" name="${selectorClassParamName}" value="<%= AutocompleteActionServlet.SELECTOR_CLASS_TIMEZONE_INFO_UPDATE %>"/>
	  <div class="form-group row">
	    <label for="staticAbbr" class="col-sm-3 col-form-label">Abbreviation</label>
	    <div class="col-sm-9">
	      <label class="col-form-label"><c:out value='${timezoneInfoData.abbreviation}'/></label>
	    </div>
	  </div>
	  <div class="form-group row">
	    <label for="inputName" class="col-sm-3 col-form-label">Name</label>
	    <div class="col-sm-9">
	      <input type="text" class="form-control" id="${timezoneNameParamName}" name="${timezoneNameParamName}" placeholder="Timezone name" value="<c:out value='${timezoneInfoData.name}'/>"/>
	    </div>
	  </div>
	  <div class="form-group row">
	    <label for="inputOffset" class="col-sm-3 col-form-label">Offset</label>
	    <div class="col-sm-9">
	      <input type="text" class="form-control" id="${timezoneOffsetParamName}" name="${timezoneOffsetParamName}" placeholder="UTC offset" value="<c:out value='${timezoneInfoData.offset}'/>"/>
	    </div>
	  </div>
	  <input type="hidden" id="${timezoneIdParamName}" name="${timezoneIdParamName}" value="<c:out value='${timezoneInfoData.id}'/>"/>
	  <div class="modal-footer">
	    <button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
	    <button type="submit" class="btn btn-primary" data-toggle="modal" data-target="#editModal">Save</button>
	  </div>
	</form>
</c:if>