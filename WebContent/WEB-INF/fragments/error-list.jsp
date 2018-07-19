<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:forEach var="item" items="${requestScope[messageCollectionAttributeName]}">
	<c:choose><c:when test="${item.status == 'ERROR'}">
		<c:set var="alertType" scope="page" value="danger"/>
	</c:when><c:when test="${item.status == 'WARNING'}">
		<c:set var="alertType" scope="page" value="warning"/>
	</c:when><c:when test="${item.status == 'INFO'}">
		<c:set var="alertType" scope="page" value="info"/>
	</c:when><c:when test="${item.status == 'SUCCESS'}">
		<c:set var="alertType" scope="page" value="success"/>
	</c:when><c:otherwise>
		<c:set var="alertType" scope="page" value="info"/>
	</c:otherwise></c:choose>
	<input type="hidden" name="user_message" 
		value="<c:out value='${item.message}'/>"
		data-header="<c:out value='${item.status.label}'/>" 
		data-alert="${alertType}" 
		/>
</c:forEach>
