<%@ page pageEncoding="UTF-8"%>
<%@ page import="sitemap.*" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="cust" uri="/WEB-INF/taglibs/custom.tld"%>
<c:set var="singlePagingTargetPath" scope="page" value="<%= ServletPath.SINGLE %>" />
<c:set var="singlePagingTargetPathWithSecondaryControl" scope="page" value="<%= ServletPath.SINGLE_WITH_SECONDARY %>" />
<c:set var="multiplePagingTargetPaths" scope="page" value="<%= ServletPath.MULTIPLE %>" />
<c:set var="editablePagingTargetPath" scope="page" value="<%= ServletPath.EDITABLE %>" />
<c:set var="autocompletePath" scope="page" value="<%= ServletPath.AUTOCOMPLETE %>" />
<nav class="nav flex-column col-md-2 float-left">
	<a class="nav-link" href="<c:url value='${singlePagingTargetPath}'/>"><c:out value="Single control" /></a>
	<a class="nav-link" href="<c:url value='${singlePagingTargetPathWithSecondaryControl}'/>"><c:out value="Secondary control" /></a>
	<a class="nav-link" href="<c:url value='${multiplePagingTargetPaths}'/>"><c:out value="Multiple targets" /></a>
	<a class="nav-link" href="<c:url value='${editablePagingTargetPath}'/>"><c:out value="Editable target" /></a>
	<a class="nav-link" href="<c:url value='${autocompletePath}'/>"><c:out value="Autocomplete" /></a>
</nav>