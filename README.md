This is a Java Web application built upon Java 7, J2EE 6 and plain Servlet/JSP on server side, respectively JQuery 3, Bootstrap 4 and some custom JavaScript in the browser.

It is about presenting relatively large tabular data sets in a Web browser. Both server and client side aspects are reviewed.

Best effort is made to implement functionalities as generic as not to be changed further, i.e. to close as many parts of code as possible. Just to name a few drivers, the following can be said:

- Streaming  
  On server side, the data is written to output stream at the point it is received, immediately or in small chunks, to avoid surges in memory allocation.

- Strict separation between Java and JavaScript code  
  JavaScript is never generated and contains no URL or HTML literals or other visible content.

- Consistent presentation code and logic  
  Any HTML page, fragment or presentation logic is implemented in a JSP.

- Option to refresh multiple fragments on page with a single AJAX request  
  The most common case is rendering data and error message(s).

- When modifying data, the respective forms are generated on server side  
  This guarantees the forms contain the most recent data.

Whenever a big number of records is to be presented in tabular form, it is usually split into chunks and the user is let to move back and forth. To save time and bandwidth, the browser window is only refreshed partially on switching between chunks and not entirely reloaded. When changing the current chunk, one or more regions on the window may need to be refreshed. There may be one or more regions containing data chunks, each with their own control(s) – they are expected to act independently of each other and from any other control on the window.

Following is an attempt to give a solution that keeps the code clean, scalable and reusable.

#### 1. Streaming database content *(avoiding large in-memory data sets)*

This project uses the Model-View-Controller paradigm to separate the internal *(RDBMS)* data from its presentation in the UI *(HTML)*. In its simplest form, this means the Action Servlet *(Controller)* updates and queries the backing store via DAO object, stores the data into an HttpServletRequest attribute and then forwards the request to JSP *(View)* for rendering. This appeared to cause surges in memory allocation on server side when rendering relatively large *(about 7000 records)* data sets.

Streaming means querying the backend happens directly in the View *(JSP)*, where the Controller only prepares a `DaoCallSupport` object, stores it into an HttpServletRequest attribute and then forwards the request to JSP *(View)* for rendering. This way, the View has the chance to output the data as it is received from backend and there is no need to accumulate it in memory.

To unify the above, all DAO objects potentially delivering big number of records, implement a common interface:
```
public interface IStreamingDao {
	void stream(final DaoCallSupport daoCallSupport) throws SQLException;
}
```

The `DaoCallSupport` in turn, is a nearly pure transport object with the following properties:
```
public class DaoCallSupport {
...
	private final IStreamingDao dao;
	private final DAOParams daoParams;
	private final Logger logger;
	private IStreamingCallback callback;
...

```
and a single utility method:
```
...
	public void stream(
			final HttpServletRequest request, 
			final HttpServletResponse response
			) {
		try {
			this.getDao().stream(this);
		} catch (final Exception e) {
			this.getLogger().error("Error streaming content", e);
		}
	}
}

```

Having the above in place lets a custom tag be introduced, thus unifying the way large number of records is rendered. Taking `single.jsp` as an example, the data is delivered by dynamically including `timezone-info-page.jsp`:
```
<cust:include 
	dao="<%=AServlet.DAO_CALL_SUPPORT_ATTRIBUTE_NAME %>" 
	dataObject="<%=SingleActionServlet.TIMEZONE_INFO_DATA_ATTRIBUTE_NAME %>" 
	view="<%=ViewPath.FRAGMENT_TIMEZONE_INFO_DATA %>"
/>
```
All Controller Servlets that handle public URLs are kept in the `action` package. The actual implementation of streaming can be found there.

Streaming is used across all pages in this application. In a heavily loaded site this would raise the productivity by minimizing the amount of memory allocated per request.

#### 2. Creating a paged region in browser window *(WEB-INF/single)*

Each region is marked by a specific class name, that is also known to server side. The region may consist of one or more HTML elements, marked by the same class name.

For example, there is one such region in `single.jsp`:
```
<tbody class="${selectorClassName}">
	<jsp:include page="<%=ViewPath.FRAGMENT_TIMEZONE_INFO_PAGE %>"/>
</tbody>
```
Note the HTML code for the paged region must represent a separate JSP fragment, since it will be rendered alone too.

The above region is targeted by a pagination control section that is included dynamically, taking the marker class name as a request parameter:
```
<jsp:include page="<%=ViewPath.FRAGMENT_PAGINATION %>">
	<jsp:param value="${selectorClassName}" name="selectorClass"/>
</jsp:include>
```
The pagination control itself, is rendered by JavaScript code, based on the total number of pages for the respective region *(identified by class name as shown above)*. Providing such information from server to client side is subject to a few rules and discussed right below.

#### 3.  Sharing information with client-side JavaScript

- Values

Values are provided in the form of hidden \<input\> tags.

Example *(total number of pages for the region marked as ${cls})*: 
```
<input type="hidden" name="${cls}-total-data-pages" value="${totalPages}"/>
```

- HTML templates for use with JavaScript

HTML templates are provided in the form of non-executable scripts: \<script type=”text/template”\>. All I18n work or URL handling is thus performed on server side.

Example *(start button of a pagination control for the region marked as ${cls})* :
```
<script type="text/template" data-template="${cls}-start-button-item">
	<li class="start">
		<a href="?<c:out value="${cls}"/>=1" aria-label="<c:out value='${startText}'/>" class="addLoader"><c:out value="${startText}"/></a>
	</li>
	<li class="prev">
		<a href="?<c:out value="${cls}"/>={{page}}" aria-label="Previous" class="addLoader">&laquo;</a>
	</li>
</script>
```

- CSS

Whenever a CSS rule contains a server URL (absolute), it is kept in a JSP, so as to maintain the context path in a consistent manner and possibly use Java constants (for refactoring purposes).

Example *(WEB-INF/auto/autocomplete.jsp)*:
```
...	
	<style>
		.typeahead-loaging {
			background:url(<c:url value="/img/loading.gif"/>) no-repeat right center/contain content-box;
		}
	</style>
...
```
#### 4.  Partial window rendering via AJAX calls

- Pagination

Pagination control looks like:

![](media/1b28978407f64a2ac6afcfdb2558c068.png)

It is maintained as a JSP fragment (WEB-INF/fragments/pagination.jsp), included where appropriate and then populated, hidden or shown by JavaScript code based on the total number of pages available, delivered in the following form:
```
<input type="hidden" name="${cls}-total-data-pages" value="${totalPages}"/>
```
, where `${cls}` is the class name that identifies the target region on browser window. 

On server side, `${totalPages}` is populated via request attribute comprising a *Map<String,Integer>*, since there may be multiple paged regions within the same browser window. The following protected method is used to store the map in the request:

`action.AServlet#setTotalPagesMap(HttpServletRequest, Map<String, Integer>)`

If the total number of pages is less than 2 *(incl. the value is not set)*, the control is not shown.

- Serving page requests

When the pagination control is in place and the user clicks a button on it, an AJAX GET request is triggered to page’s URL, which brings the requested page number as a URL parameter. To distinguish between regular HTTP GET requests that render the whole browser window and AJAX requests that are to render parts of it, the Servlet checks the Accept HTTP header. If it indicates the client accepts JSON format, the Servlet returns data in the form of a JSON array. Otherwise, the Servlet responds with entire windows’s HTML. This way, the JavaScript code making an AJAX call does not need to care about the actual URL and is therefore the same for all pages *(i.e. it is generic)*.

The JavaScript code that updates browser window partially is universal (generic) too. It expects data in the form of a JSON array. Each entry in the JSON array corresponds to a DOM element marked with a certain class. Each JSON array entry is mapped to a DOM element, where the match is made in order of appearance in the array/DOM tree. The JavaScript code clears the contents of respective DOM element first and then populates it by transforming the corresponding JSON array entry into HTML, where the expected format is [JSONML](http://www.jsonml.org/), which supports HTML elements, attributes and text. This approach provides for updating several parts of the DOM tree in a single iteration, including possible presentation of errors during the AJAX call.

On server side, all browser window regions subject to partial rendering are maintained as separate JSP fragments. They are included in the parent page and enclosed in HTML elements, each marked with a respective class. The name of enclosing HTML element is of no importance – could be a `<div>`, `<tbody>` or anything else. The Servlet uses a universal XSLT transformation to convert any such fragment into [JSONML](http://www.jsonml.org/) when responding to partial rendering AJAX requests. The XSLT itself uses [Gson](https://github.com/google/gson) to render the JSON field values. This project uses one and the same library when generating JSON. While this makes the code universal across pages, some limitations apply as listed below:

- The page fragments subject to partial rendering must represent a well-formed XML. However, it is **not** limited to a single element;

- Any HTML entitiy in the fragment *(like `&nbsp;`)* must be declared in the XSLT, see `utils.JsonUtil.DOCUMENT_PREFIX`;

- Referencing external libraries in XSLT is not standardized, so the way to bind [Gson](https://github.com/google/gson) may change with the JVM implementation/version or the particular XSLT engine used. This project uses Saxon, but Xalan variant is provided too (Xalan is currently embedded in JVM).

Apart from pagination, partial window rendering may be triggered:

- by clicking on a link *(WEB-INF/editable/fragments/timezone-info-data.jsp)*;
```
...
<input 
	type="button" data-toggle="modal" data-target="#editModal" value="Edit"
	class="btn btn-primary ajax-update"
	data-timezoneid="${timezoneInfo.id}"
	data-classname="<%=EditableActionServlet.SELECTOR_CLASS_TIMEZONE_INFO_EDIT %>"
	/>
...
```
The above makes a GET request to populate and show a modal form. The `ajax-update` class triggers the call, while `data-classname` is the marker class to inform both JavaScript and server code what is the target region. It is important to note, that the request includes all parameters available in browser window's URL, as well as all `data-xxx` attributes possibly included in the link *(like timezoneid above)*. Therefore, any parameters needed for the request must be included as `data-xxx` attributes.

- by submitting a form *(WEB-INF/auto/autocomplete.jsp)*;
```
...
<form class="form-inline my-3 ajax-update">
...
	<input type="hidden" name="classname" value="${resultListSelectorClassName}"/>
...
</form>
```
Again, it is the `ajax-update` class that makes submitting the form an AJAX call. Note the hidden \<input\> holding the `classname` parameter - it serves the same purpose as in the previous example. The request includes all parameters available in browser window's URL too. 

- by including a hidden \<input\> tag *(see the [I18N Example](https://github.com/olddimplex/I18nExample))*.

Due to the use of streaming, some parts of the window may need an update after the entire page is loaded. An example could be the data set appears empty and a message needs to be shown in the beginning of page (already streamed at the time this condition is detected). In such cases a hidden \<input\> may be added with the special name `ajax-update` and a value holding the target region's marking class:
```
<input type="hidden" name="ajax-update" value="${selectorClassName}"/>
```
In doing so, it must be ensured the Servlet responsible for the page's URL understands this request and returns the respective [JSONML](http://www.jsonml.org/) to populate the target region. To complete the hypothetic case, the Servlet may leave a message object in user's Session and render *(and clean)* it when called as shown here.

Generally said, the marker class name is used on server side to determine the use case for the particular URL, i.e. which part of the window is to be refreshed.

#### 5.  Managing URL literals *(Server side, AJAX calls)*

No URL literals in JavaScript code. All AJAX calls use the current browser window URL, adding headers to distinguish between them on server side.
