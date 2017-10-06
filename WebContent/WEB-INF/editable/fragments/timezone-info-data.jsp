<%@ page pageEncoding="UTF-8" %>
<%@ page import="action.*" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="timezoneInfoData" scope="page" value="${requestScope[timezoneInfoDataEditableAttributeName]}"/>
<c:forEach var="timezoneInfo" items="${timezoneInfoData}">
<tr>
	<td><c:out value="${timezoneInfo.abbreviation}" /></td>
	<td><c:out value="${timezoneInfo.name}" /></td>
	<td><c:out value="${timezoneInfo.offset}" /></td>
	<td>
<%-- Keep the data- attribute names in lowercase --%>
		<input 
			type="button" data-toggle="modal" data-target="#editModal" value="Edit"
			class="btn btn-primary ajax-update"
			data-timezoneid="${timezoneInfo.id}"
			data-classname="<%=EditableActionServlet.SELECTOR_CLASS_TIMEZONE_INFO_EDIT %>"
			/>
	</td>
</tr>
</c:forEach>