<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="timezoneInfoData" value="${requestScope[timezoneInfoDataMultipleAttributeName]}"/>
<c:forEach var="timezoneInfo" items="${timezoneInfoData}">
		    <tr>
		      <td><c:out value="${timezoneInfo.abbreviation}" /></td>
		      <td><c:out value="${timezoneInfo.name}" /></td>
		      <td><c:out value="${timezoneInfo.offset}" /></td>
		    </tr>
</c:forEach>