(function(){
	
	function getCommonParams() {
        var params = {};
    	window.location.search.replace("?","").split("&").map(function(arg) {
    		if(arg.length > 1) {
	    		var val = arg.split("=");
	    		params[val[0]] = val[1];
    		}
    	});
    	return params;
	}
	
	function addLoader() {
        if ($("#loader").length < 1) {
        	$("body").prepend('<div id="loader" class="loadcloak"></div>');
        }
	}

	/**
	 * Initializes any DOM element just populated, also called on $(document).ready()
	 */
	function init(el) {  
		// Possibly render parts of page on click
        el.find('.ajax-update:not(form)').on('click', function(e) {
    		var context = $(e.currentTarget.dataset.target);
        	var className = e.currentTarget.dataset["classname"];
        	// The following HTML will be replaced by the actual content
        	context.find("." + className).empty().html($('script[data-template="loading-indicator"]').html());
        	var params = getCommonParams();
        	params["location"] = window.location.toString(); // possibly redirect here to refresh page
        	addLoader();
        	getContent($.extend(params, e.currentTarget.dataset), function(data) {
        		onAjaxUpdate(data, className);
        	});
        });
		// Possibly render parts of page on click
        el.find('form.ajax-update').on('submit', function(e) {
        	e.preventDefault();
        	var fieldData = getCommonParams();
        	$(e.currentTarget).find("input, textarea, select").each(function(ndx, el) {
        		// radio and checkbox input types not supported !!!
        		fieldData[el.name] = $(el).val();
        	});
        	var className = fieldData["classname"];
            addLoader();
        	getContent(fieldData, function(data) {
        		onAjaxUpdate(data, className);
        		// Maintain history
        		addPopstateListener();
        		
        		// You can't restore form data in the general case 
        		// since there may be multiple forms on page,
        		// therefore not supported here
            	var state = stateObj[className];
            	state.totalPages = getTotalPages(data, className);
            	state.data = getDataToRender(data);

                var newUrl = window.location.href.split("?")[0] + "?" + getRequestBody(fieldData);
                history.pushState(stateObj, null, newUrl);
        	});
        });
		// Possibly render parts of page on load/refresh
        el.find("input[name='ajax-update']").each(function() {
    		var className = $(this).val();
        	getContent({"classname": className}, function(data) {
            	onAjaxUpdate(data, className);
        	});
    	});
		// Possibly render parts of page on load/refresh
        el.find('input[type="text"][data-provide="typeahead"]').each(function() {
        	var self = $(this);
        	var params = $.extend(getCommonParams(), self.data());
        	var highlighterTemplateName = self.data("highlighter-template");
        	var updaterTemplateName = self.data("updater-template");
            /**
             * Typeahead
             */
    		var highlighterTemplate = self.parent().find('script[data-template="' + highlighterTemplateName + '"]').html().split(/\{\{(.+?)\}\}/g);
    		var updaterTemplate = self.parent().find('script[data-template="' + updaterTemplateName + '"]').html().split(/\{\{(.+?)\}\}/g);
            self.typeahead({
            	source: function(query, process) {
            		self.toggleClass("typeahead-loaging");
            		getContent($.extend(params, {"query": query}),
	                    function(data) { // called on success
	                    	process(getDataToRender(data)[0]);
	                    },
	                    function(data) { // called on error
	                    	console.log(data);
	                    },
	                    function() { // called always
	                    	self.toggleClass("typeahead-loaging");
	                    });	
            	},
                minLength: 1,
                items: 'all',
                autoSelect: false,
                delay : 1000,
                fitToElement: true,
                highlighter: function (item) {
                    return applyTemplate(highlighterTemplate, JSON.parse(item));
                },
                updater: function (item) {
                	return applyTemplate(updaterTemplate, item);
                },
                displayText: function(item) {
                    if (typeof (item) != "object") {
                        return item;
                    }
                    return JSON.stringify(item);
                }
            });
        });
	}

// On ajax update functions
    /**
     * Handles JSONML format, see: http://www.jsonml.org/
     */
	function onAjaxUpdate(data, className) {
		if(className) {
			var dataArray = getDataToRender(data);
			if(dataArray) {
				var nodesToUpdate = getNodesToUpdate(className);
				$.each(nodesToUpdate, function(ndx, el) {
					var $el = $(el);
					$el.empty();
					generateAjaxContent(el, dataArray[ndx], 0);
					init($el);
				});
			}
			var totalPages = getTotalPages(data, className);
			if(!isNaN(totalPages)) {
				$(".pagination." + className).each(function(ndx, paginationContext) {
					if(totalPages > 1) {
						handlePaginationContext($(paginationContext), className, totalPages, false);
					} else {
						$(paginationContext).empty();
					}
				});
			}
		}
	}

	function getDataToRender(data) {
		var dataArray;
		if(data.constructor === Array) {
			dataArray = data;
		} else
		if(typeof data === 'object') {
			$.each(data, function(key, val) {
				if(val.constructor === Array) {
					dataArray = val;
					return false;
				}
			});
		}
		return dataArray;
	}
	
	function getTotalPages(data, className) {
		var totalPages;
		if(typeof data === 'object') {
			$.each(data, function(key, val) {
				if(typeof val === 'object') {
					totalPages = val[className];
					if(totalPages) { 
						return false;
					}
				}
			});
		}
		return totalPages;
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
        	return (template.length < 2 || i % 2) ? params[tok] : tok;
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
     * @param paramsObj = {
	    		"totalPages": totalPages,
	    		"showRange": showRange,
	    		"page": currentPage,
	    		"templates": templates {"start":[], "active":[], "regular":[], "end":[]}
	    		}
	   @param paginationContext = JQuery object
     * @return {Bool|String} content = false | html string
     */
    function buildPagination(paramsObj, paginationContext)  {
    	var content,
    		totalPages = paramsObj.totalPages,
    		currentPage = paramsObj.page,
    		showRange = paramsObj.showRange,
    	    templates = paramsObj.templates;
        if (totalPages <= 0  || !paramsObj.showRange) {
            return false;
        } else if (totalPages < 2) {
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
            if (!$(this).hasClass('disabled')) {
                 addLoader();
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

    function handlePaginationContext(paginationContext, className, totalPages, addEventHandler) {

        var templates = {};
        ["start","active","regular","end"].forEach(function(templateName){
        	$('script[data-template="' + className + '-' + templateName + '-button-item"]').filter(":first").each(function(ndx, el) {
        		templates[templateName] = $(el).html().split(/\{\{(.+?)\}\}/g);
        	});
        });
        var currentPage = 1,
            isSecondary = paginationContext.attr("data-secondary");

        var pageParamValue = parseInt(getCommonParams()[className]);
        if (pageParamValue && !isNaN(pageParamValue) && pageParamValue > 0) {
            currentPage = Math.min(pageParamValue, totalPages);
        }

        if(addEventHandler) {
	        // on click rebuild pagination
	        paginationContext.on("click", "li.page,.prev,.next,.start,.end", function(event) {
	
	            // don't reload page on click
	            event.preventDefault();
	            
	            var pagesAvailable = stateObj[className].totalPages;
	
	            if($(this).is('li.page')){
	                currentPage = parseInt($(this).text());
	            }
	            if($(this).is('.prev')){
	                currentPage--;
	                if (currentPage < 1) { currentPage = 1; };
	            }
	            if($(this).is('.next')){
	                currentPage++;
	                if (currentPage > pagesAvailable) { currentPage = pagesAvailable; };
	            }
	            if($(this).is('.start')){
	               currentPage = 1;
	            }
	            if($(this).is('.end')){
	                currentPage = pagesAvailable;
	            }
	            
	            var currentParams = getCommonParams();
	            currentParams[className] = currentPage;
	
	            getContent(
	            	$.extend({}, currentParams, {
		            	"classname": className
		            }),
		            function(data) { // AJAX update success callback
	                	onAjaxUpdate(data, className);
	                	var state = stateObj[className];
	                	state.page = currentPage;
	                	state.data = data;
		                $("." + className + ".pagination").each(function(ndx, el) {
		                	buildPagination(state, $(el));
		                });
		                //Change page number in url and push it to history
		                var newUrl = window.location.href.split("?")[0] + "?" + getRequestBody(currentParams);
		                history.pushState(stateObj, null, newUrl);
		            }
	            );
	        });
        }

        addPopstateListener();
        
		// maintain it globally since we call this function on AJAX updates too
    	stateObj[className] = {
    		"showRange": 3, 
    		"page": currentPage,
    		"totalPages": totalPages,
    		"templates": templates
    	};
        
        buildPagination(stateObj[className], paginationContext);

    }

    function addPopstateListener() {
        if(!window.popstateListener) {
        	// block adding multiple instances
        	window.popstateListener = popstateListener;
	        // History events handled
	        window.addEventListener('popstate', window.popstateListener);
        }
    }
    
    function popstateListener(e) {
    	var popObject = e.state;
    	$.each(popObject, function(className, state) {
            $("." + className + ".pagination").each(function(ndx, el) {
            	buildPagination(state, $(el));
            });
            if(state.data) {
            	onAjaxUpdate(state.data, className);
            } else
            if(state.dataHtml) {
        		getNodesToUpdate(className).each(function(ndx, el) {
        			$(el).html(state.dataHtml[ndx]);
        		});
            }
    	});
    }
    
    var stateObj = {};

    $(document).ready(function($) {
    	$(".pagination").each(function(ndx, paginationContext) {
    		paginationContext.className.split(/\s+/).forEach(function(className) {
    			var totalPages = parseInt($("input[name='" + className + "-total-data-pages']").filter(":first").val(),10) || 1;
    			if(!isNaN(totalPages) && totalPages > 1) {
    				// Note the following updates the global stateObj
    				handlePaginationContext($(paginationContext), className, totalPages, true);
    			}
    		});
    	});
    	var paginationControlCount = 0; 
    	$.each(stateObj, function(className, state) {
    		paginationControlCount++;
    		state["dataHtml"] = [];
    		getNodesToUpdate(className).each(function(ndx, el) {
    			state.dataHtml.push(el.innerHTML);
    		});
    	});
    	if(paginationControlCount > 0) {
	        history.replaceState(stateObj, null, window.location.toString());
    	}
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