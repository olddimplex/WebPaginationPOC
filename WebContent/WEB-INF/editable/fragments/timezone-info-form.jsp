<%@ page pageEncoding="UTF-8" %>
<%@ page import="sitemap.*" %>
<%@ page import="action.*" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="timezoneInfoData" scope="page" value="${requestScope[timezoneInfoDataEditableAttributeName]}"/>
<c:set var="timezoneIdParamName" scope="page" value="<%=EditableActionServlet.TIMEZONE_ID_PARAM_NAME %>"/>
<c:set var="timezoneNameParamName" scope="page" value="<%=EditableActionServlet.TIMEZONE_NAME_PARAM_NAME %>"/>
<c:set var="timezoneOffsetParamName" scope="page" value="<%=EditableActionServlet.TIMEZONE_OFFSET_PARAM_NAME %>"/>
<c:set var="locationParamName" scope="page" value="<%=AServlet.LOCATION_PARAM_NAME %>"/>
<%-- 
	Note the following must be a valid XML.
	Any text entity (like &nbsp;) must be declared unless it is a standard XML entity (&amp; | &lt; | &gt; | &quot; | &apos;),
	See also: JsonUtil.DOCUMENT_PREFIX
--%>
<form action="<c:url value='<%=ServletPath.EDITABLE %>'/>" method="POST">
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
<%-- 
	The browser will be redirected to the following after form submit.
	This covers the case where request is somehow proxied, 
	so the server is unable to reconstruct the initial URL as seen by the client. 
--%>
  <input type="hidden" id="${locationParamName}" name="${locationParamName}" value="<c:out value='${param[locationParamName]}'/>"/>
  <div class="modal-footer">
    <button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
    <button type="submit" class="btn btn-primary">Save</button>
  </div>
</form>