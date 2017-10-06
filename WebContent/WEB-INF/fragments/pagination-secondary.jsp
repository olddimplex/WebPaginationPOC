<%@ page pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"  %>
<nav aria-label="<c:out value="Page navigation"/>" class="text-center">
    <ul class="${fn:escapeXml(param.selectorClass)} pagination" data-secondary="yes"></ul>
</nav>
