(function(){

	/**
	 * Initializes any DOM element just populated, also called on $(document).ready()
	 */
	function init(el) {  
		// Possibly render parts of page on click
        el.find('.ajax-update').on('click', function(e) {
            var params = {"location": window.location};
    		var context = $(e.currentTarget.dataset.target);
        	window.location.search.replace("?","").split("&").map(function(val) {
        		val = val.split("=");
        		params[val[0]]  = val[1];
        	});
        	var className = e.currentTarget.dataset["classname"];
        	// The following HTML will be replaced by the actual content
        	context.find("." + className).empty().html($('script[data-template="loading-indicator"]').html());
        	getContent($.extend(e.currentTarget.dataset, params), function(data) {
        		onAjaxUpdate(data, className);
//        		onCountryChange(context, params);
        	});
        });
		// Possibly render parts of page on load/refresh
        el.find("input[name='ajax-update']").each(function() {
    		var className = $(this).val();
        	getContent({"classname": className}, function(data) {
            	onAjaxUpdate(data, className);
        	});
    	});
	}

// On ajax update functions
    /**
     * Handles JSONML format, see: http://www.jsonml.org/
     */
	function onAjaxUpdate(data, className) {
		if(className && data) {
			var nodesToUpdate = getNodesToUpdate(className);
			for(var i = 0; i < nodesToUpdate.length && i < data.length; i++) {
				$(nodesToUpdate[i]).empty();
				generateAjaxContent(nodesToUpdate[i], data[i], 0);
				init($(nodesToUpdate[i]));
			}
		}
	}
	
	function getNodesToUpdate(className) {
		return $("." + className + ":not(.pagination)");
	}

	function generateAjaxContent(parent, data, level) {
		var el = parent;
		if(level > 0) {
			el = document.createElement(data[0]);
			parent.appendChild(el);
		}
		for(var i = 1; i < data.length; i++) {
			if(data[i].constructor === Array) {
				generateAjaxContent(el, data[i], 1 + level);
			} else
			if(typeof data[i] === 'object') {
				setAjaxAttributes(el, data[i]);
			} else
			if(typeof data[i] === 'string') {
				el.appendChild(document.createTextNode(data[i]));
			}
		}
	}

	function setAjaxAttributes(el, obj) {
		for(name in obj) {
			el.setAttribute(name, obj[name]);
		}
	}

/*------------------------------------------------------------------
[  Very basic "template engine"  ]
*/

	function applyTemplate(template, params) {
    	return template.map(function(tok, i) {
        	return (i % 2) ? params[tok] : tok;
        }).join('');
	}

/*------------------------------------------------------------------
[  Pagination Starts Here  ]
*/
    /**
     * Renders pagination links as unordered list elements
     * @param {Number} starting
     * @param {Number} ending
     * @param {Number} active
     * @param {Number} totalPages
     * @param {Object} templates {"start":[], "active":[], "regular":[], "end":[]}
     * @return {String} content
     */
	function addPages(starting, ending, active, totalPages, templates) {

        var content = " ";

        if (active != 1 ) {
        	content = applyTemplate(templates["start"], {"page":(active - 1)});
        }
        for (var i = starting; i <= ending; i++) {
        	content += applyTemplate(templates[(i == active) ? "active" : "regular"], {"page": i});
        }
        if (active != totalPages) {
        	content += applyTemplate(templates["end"], {"page": (active + 1)});
        }

        return content;
    }

    /**
     * Builds Pagination according to params values
     * @param stateObject = {
	    		"totalPages": totalPages,
	    		"showRange": showRange,
	    		"page": currentPage,
	    		"context": paginationContext,
	    		"templates": templates {"start":[], "active":[], "regular":[], "end":[]}
	    		}
	   @param paginationContext = JQuery object
     * @return {Bool|String} content = false | html string
     */
    function buildPagination(stateObject, paginationContext)  {
    	var content,
    		totalPages = stateObject.totalPages,
    		currentPage = stateObject.page,
    		showRange = stateObject.showRange,
    	    templates = stateObject.templates;
        if (totalPages <= 0  || !stateObject.showRange) {
            return false;
        } else if (totalPages == 1) {
            content = "";
        } else {
            if (totalPages < showRange * 2) {
                content = addPages(1, totalPages, currentPage, totalPages, templates);
            }
            else if (currentPage < showRange * 2) {
                content = addPages(1, showRange * 2, currentPage, totalPages, templates);
            }
            else if (currentPage > totalPages - showRange * 2) {
                content = addPages(totalPages - showRange * 2, totalPages, currentPage, totalPages, templates);
            }
            else {
                content = addPages(currentPage - showRange, currentPage + showRange, currentPage, totalPages, templates);
            }
        }

        paginationContext.html(content);
        paginationContext.on("click", ".addLoader", function(event) {
            if (!$(this).hasClass('disabled') && $("#loader").length < 1) {
                 $("body").prepend('<div id="loader"></div>');
            }
        });

    }

    function getRequestBody(oParams)   {
        var aParams = new Array(),
            sParam;
        $.each(oParams,function(j,valR){
            sParam = "";
            sParam = encodeURIComponent(j);
                sParam += "=";
                sParam += encodeURIComponent(valR);
                aParams.push(sParam);
        });

        return aParams.join("&");
    }

    function getContent(params, callbackDone, callbackFail, callbackAlways){
        jQuery.ajax({
            beforeSend: function (xhr){ 
                xhr.setRequestHeader("Content-Type","application/json");
                xhr.setRequestHeader("Accept","application/json");
            },
            url: window.location.href.split("?")[0],
            data: params,
            type: 'GET',
            contentType: "application/json"
        })
        .done(function(data) {
        	if(callbackDone) {
        		callbackDone(data);
        	}
        })
        .fail(function(data) {
        	if(callbackFail) {
        		callbackFail(data);
        	}
        })
        .always(function() {
            // Remove loader
        	$("#loader").remove();
        	if(callbackAlways) {
        		callbackAlways();
        	}
        });
    }

    function handlePaginationContext(paginationContext, className, totalPages) {

        var templates = {};
        ["start","active","regular","end"].forEach(function(templateName){
        	$('script[data-template="' + className + '-' + templateName + '-button-item"]').filter(":first").each(function(ndx, el) {
        		templates[templateName] = $(el).html().split(/\{\{(.+?)\}\}/g);
        	});
        });
        var currentPage = 1,
            showRange = 3,
            isSecondary = paginationContext.attr("data-secondary");

        var params      = window.location.search.replace("?",""),
            newParams   = {},
            tempParams  = {};

        params = params.split("&").map(function(val) {
            val                 = val.split("=");
            tempParams          = {};
            tempParams[val[0]]  = val[1];

            $.extend(true, newParams, tempParams);
        });

        var pageParamValue = parseInt(newParams[className]);
        if (pageParamValue && !isNaN(pageParamValue) && pageParamValue > 0) {
            currentPage = Math.min(pageParamValue, totalPages);
        }

        // on click rebuild pagination
        paginationContext.on("click", "li.page,.prev,.next,.start,.end", function(event) {

            // don't reload page on click
            event.preventDefault();

            if($(this).is('li.page')){
                currentPage = parseInt($(this).text());
            }
            if($(this).is('.prev')){
                currentPage--;
                if (currentPage < 1) { currentPage = 1; };
            }
            if($(this).is('.next')){
                currentPage++;
                if (currentPage > totalPages) { currentPage = totalPages; };
            }
            if($(this).is('.start')){
               currentPage = 1;
            }
            if($(this).is('.end')){
                currentPage = totalPages;
            }

            newParams[className] = currentPage;

            getContent(
            	$.extend({}, newParams, {
	            	"page": currentPage,
	            	"classname": className
	            }),
	            function(data) { // AJAX update success callback
                	onAjaxUpdate(data, className);
	                var stateObject = {
	            		"totalPages": totalPages,
	            		"showRange": showRange,
	            		"page": currentPage,
	            		"selectorClass": className,
	            		"templates": templates,
	            		"data": data
	            	}
	                newUrl = window.location.href.split("?")[0] + "?" + getRequestBody(newParams);
	                // Change page number in url and push it to history
	                history.pushState(stateObject, null, newUrl);
	                $("." + className + ".pagination").each(function(ndx, el) {
	                	buildPagination(stateObject, $(this));
	                });
	            }
            );
        });

        if(!isSecondary) {
	        // History events handled
	        window.addEventListener('popstate', function(e) {
	        	var stateObject = e.state;
	        	if(stateObject && stateObject.selectorClass) {
	                $("." + stateObject.selectorClass + ".pagination").each(function(ndx, el) {
	                	buildPagination(stateObject, $(this));
	                });
	                if(stateObject.data) {
	                	onAjaxUpdate(stateObject.data, stateObject.selectorClass);
	                } else
	                if(stateObject.dataHtml) {
	                	getNodesToUpdate(className).each(function(ndx, el) {
	                		$(el).html(stateObject.dataHtml[ndx]);
	                	});
	                }
	        	}
	        });
        }

        // Init pagination links
        var stateObject = {
    		"totalPages": totalPages,
    		"showRange": showRange,
    		"page": currentPage,
    		"selectorClass": className,
    		"templates": templates,
    		"dataHtml": []
    	}
        
        getNodesToUpdate(className).each(function(ndx, el) {
        	stateObject["dataHtml"].push(el.innerHTML);
        });

        if(!isSecondary) {
            newUrl = window.location.href.split("?")[0] + "?" + getRequestBody(newParams);
            // Change page number in url and push it to history
            history.pushState(stateObject, null, newUrl);
        }
        
        buildPagination(stateObject, paginationContext);

    }

    $(document).ready(function($) {
    	$(".pagination").each(function(ndx, paginationContext) {
    		paginationContext.className.split(/\s+/).forEach(function(className) {
    			var totalPages = parseInt($("input[name='" + className + "-total-data-pages']").filter(":first").val(),10) || 1;
    			if(!isNaN(totalPages) && totalPages > 1) {
    				handlePaginationContext($(paginationContext), className, totalPages);
    			}
    		});
    	});

    	// Render arbitrary data on page
    	init($(this));
    });

/*------------------------------------------------------------------
[  Detect Browser an OS to show correct key combination to trigger accesskey  ]
*/
    var browser = navigator.userAgent.toLowerCase();

    // If Win or Linux
    if ( navigator.appVersion.indexOf("Win")!=-1 || navigator.appVersion.indexOf("Linux")!=-1 ) {

        // If Firefox and Chrome
        if (!(window.mozInnerScreenX == null) || (browser.indexOf('chrome') > -1)) {
            $('.shortcut > i').text('Alt + Shift');
        }

    }

    // If MAC OS
    if(navigator.platform.toUpperCase().indexOf('MAC')>=0) {
        $('.shortcut > i').text('Ctrl + Alt');
        // Check if Chrome's shortcuts are OK
    }

/*------------------------------------------------------------------
[  Loader & Cloak loader  ]
*/
    $(document).ready(function($) {
        // Add loader to the innermost element to make IE happy
        $(document).on("click", ".addLoader:not(.disabled)", function(event) {
            if ($("#loader").length < 1) {
                 $("body").prepend('<div id="loader" class="loadcloak"></div>');
            }
        });

        // Add Cloak Loader
        $(".loadcloak:visible").addClass('hidden');
    });

/*------------------------------------------------------------------
[  Checkbox trigger state   ]
*/
    $(document).on('click','.triggerState', function () {
        var forElement = $(this).attr('for');
        $("#"+forElement).trigger('click');
    });

    window.onbeforeunload = function () {
        $(".loadcloak:visible").removeClass('hidden');
    }

})();