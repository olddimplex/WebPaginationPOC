<%@ page pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"  %>
<c:set var="cls" scope="page" value="${fn:escapeXml(param.selectorClass)}"/>
<c:set var="totalPages" scope="page" value="${fn:escapeXml(requestScope[totalDataPagesMapAttributeName][cls])}"/>
<c:set var="startText" scope="page" value="Start"/>
<c:set var="endText" scope="page" value="End"/>
<input type="hidden" name="${cls}-total-data-pages" value="${totalPages}"/>
<%-- 
	The scripts below are intentionally made non-executable. They are solely used to provide textual content to JS code.
--%>
<script type="text/template" data-template="${cls}-start-button-item">
	<li class="start">
		<a href="?<c:out value="${cls}"/>=1" aria-label="<c:out value='${startText}'/>" class="addLoader"><c:out value="${startText}"/></a>
	</li>
	<li class="prev">
		<a href="?<c:out value="${cls}"/>={{page}}" aria-label="Previous" class="addLoader">&laquo;</a>
	</li>
</script>
<script type="text/template" data-template="${cls}-active-button-item">
	<li class="page active">
		<a href="?<c:out value="${cls}"/>={{page}}" class="addLoader">{{page}}</a>
	</li>
</script>
<script type="text/template" data-template="${cls}-regular-button-item">
	<li class="page">
		<a href="?<c:out value="${cls}"/>={{page}}" class="addLoader">{{page}}</a>
	</li>
</script>
<script type="text/template" data-template="${cls}-end-button-item">
	<li class="next">
		<a href="?<c:out value="${cls}"/>={{page}}" aria-label="Next" class="addLoader">&raquo;</a>
	</li>
	<li class="end">
		<a href="?<c:out value="${cls}"/>=${totalPages}" aria-label="<c:out value='${endText}'/>" class="addLoader"><c:out value="${endText}"/></a>
	</li>
</script>
<nav aria-label="<c:out value="Page navigation"/>" class="text-center">
    <ul class="${cls} pagination"></ul>
</nav>
