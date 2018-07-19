<%@ page pageEncoding="UTF-8" %>
<%@ page import="action.*" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="suggestionListSelectorClassName" scope="page" value="<%=AutocompleteActionServlet.SELECTOR_CLASS_TIMEZONE_ABBREVIATION_SUGGESTIONS %>"/>
<c:set var="timezoneAbbreviationParamName" scope="page" value="<%=AutocompleteActionServlet.TIMEZONE_ABBREVIATION_PARAM_NAME %>"/>
		    <div class="input-group-addon"><i class="fa fa-search" aria-hidden="true"></i></div>
		    <input 
		    	type="text" 
		    	class="form-control" 
		    	id="inlineFormAbbr"
		    	name="${timezoneAbbreviationParamName}"
		    	value="${sessionScope[timezoneAbbreviationParamName]}"
<%--		    placeholder="Abbreviation" --%>
		    	autocomplete="off"
		    	data-provide="typeahead"
		    	data-${selectorClassParamName}="${suggestionListSelectorClassName}"
		    	data-highlighter-template="listitem"
		    	data-updater-template="valueitem"
		    	/>
<%-- Typeahead templates are expected within the same element as the referring input element (above) --%>
		    <script type="text/template" data-template="listitem">
				<div class="typeahead">
					<div class="col-md-12 no-padding" style="border-bottom:1px dashed #ccc">
						<p><small class="pull-left"><strong>{{abbreviation}}</strong></small><small class="pull-right">{{name}}</small></p>
					</div>
					<div class="clearfix"></div>
				</div>
			</script>
		    <script type="text/template" data-template="valueitem">{{abbreviation}}</script>
