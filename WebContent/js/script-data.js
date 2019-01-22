(function () {

    function getCommonParams() {
        return window.location.search
            .replace("?", "")
            .split("&")
            .filter(function (x) {
                return x.length > 1;
            })
            .reduce(function (res, arg) {
                var val = arg.split("=");
                res[val[0]] = val[1];

                return res;
            }, {})
    }

    function addLoader(deferredObj) {
        if ($("#loader").length < 1) {
            $("body").prepend('<div id="loader" class="loadcloak"></div>');
        }
        if(deferredObj) {
        	var $loader;
        	$(document).find("#loader").each(function(e) {
        		$loader = $(this);
        		var loaderClasses = $loader.attr("class").split(" ");
        		loaderClasses.push("loadcloak");
        		$loader.attr("class", loaderClasses.join(" "));
        	});
        	$.when(deferredObj).always(function() {
        		var loaderClasses = $loader.attr("class").split(" ");
        		var loadCloakIndex = loaderClasses.indexOf("loadcloak");
        		loaderClasses.splice(loadCloakIndex, 1);
        		$loader.attr("class", loaderClasses.join(" "));
        		if(loaderClasses.filter(function(c) { return c === "loadcloak"; }).length === 1) {
        			$loader.remove();
        		}
        	})
        }
    }

    /**
     * Initializes any DOM element just populated, also called on $(document).ready()
     */
    function init(el) {
        el.find('.ajax-update:not(form)').on('click', function (e) {
            var context = $(e.currentTarget.dataset.target);
            var className = e.currentTarget.dataset["classname"];
            var targetUrl = e.currentTarget.dataset["target_url"];
            var postEventName = e.currentTarget.dataset["post_event_name"];
            var postEventTargetSelector = e.currentTarget.dataset["post_event_target"];
            var postEventPriority = e.currentTarget.dataset["post_event_priority"];
            // The following HTML will be replaced by the actual content
            context.find("." + className).empty().html($('script[data-template="loading-indicator"]').html());
            var params = getCommonParams();
            params["location"] = window.location.toString(); // possibly redirect here to refresh page
            addLoader(getContent("GET", targetUrl, $.extend(params, e.currentTarget.dataset), function (data) {
                maintainHistory(className, onAjaxUpdate(data, className), data);
                if(postEventName && postEventTargetSelector) {
                	var postEventTarget = $(postEventTargetSelector);
                	var eventNameArr = postEventName.split(/[,\s]/g);
                	if(postEventTarget.length > 1) try {
                		var priorityArr = postEventPriority.split(/[,\s]/g);
                		var maxPriorityIndex = 0;
                		for(var i = 1; i < postEventTarget.length; i++) {
                			if(priorityArr[i] > priorityArr[maxPriorityIndex]) {
                				maxPriorityIndex = i;
                			}
                		}
                		$(postEventTarget[maxPriorityIndex]).trigger(eventNameArr[maxPriorityIndex]);
                	} catch (e) {
                		console.log(e);
                	} else
                	if(postEventPriority) { // selector returned zero or one of many possible
                		postEventTarget.trigger(eventNameArr[0]);
                	} else { // selector expected to return a single target
                		postEventTarget.trigger(postEventName);
                	}
                }
            }));
        });
        // Possibly render parts of page on click
        el.find('form.ajax-update').on('submit', function (e) {
            e.preventDefault();
            var fieldData = getCommonParams();
            var form = $(e.currentTarget);
            form.find("input").each(function (ndx, el) {
            	if((['checkbox','radio'].indexOf(el.type) < 0 || el.checked) && el.name) {
            		fieldData[el.name] = $(el).val();
            	}
            });
            form.find("textarea, select").each(function (ndx, el) {
            	if((['checkbox','radio'].indexOf(el.type) < 0 || el.checked) && el.name) {
            		fieldData[el.name] = $(el).val();
            	}
            });
            var className = fieldData["classname"];
            var targetUrl = fieldData["target_url"];
            var postEventName = form.data("post_event_name");
            var postEventTargetSelector = form.data("post_event_target");
            var postEventPriority = form.data["post_event_priority"];
            addLoader(getContent("POST", targetUrl, fieldData, function (data) {
                maintainHistory(className, onAjaxUpdate(data, className), data);
                if(postEventName && postEventTargetSelector) {
                	var postEventTarget = $(postEventTargetSelector);
                	var eventNameArr = postEventName.split(/[,\s]/g);
                	if(postEventTarget.length > 1) try {
                		var priorityArr = postEventPriority.split(/[,\s]/g);
                		var maxPriorityIndex = 0;
                		for(var i = 1; i < postEventTarget.length; i++) {
                			if(priorityArr[i] > priorityArr[maxPriorityIndex]) {
                				maxPriorityIndex = i;
                			}
                		}
                		$(postEventTarget[maxPriorityIndex]).trigger(eventNameArr[maxPriorityIndex]);
                	} catch (e) {
                		console.log(e);
                	} else
                	if(postEventPriority) { // selector returned zero or one of many possible
                		postEventTarget.trigger(eventNameArr[0]);
                	} else { // selector expected to return a single target
                		postEventTarget.trigger(postEventName);
                	}
                }
            }));
        });
       	// Ensure all ordinary forms submit the page refresh url too (may not be known to server due to intermediaries)
	   	el.find('form:not(.ajax-update)').submit(function (e) {
            $(e.currentTarget).find('input[type="hidden"][name="location"]:first').each(function (ndx, el) {
		    	$(el).attr("value", window.location.toString());
		    });
            return true;
	   	});
        // Possibly render parts of page on load/refresh
        el.find("input[name='ajax-update']").each(function () {
            var className = $(this).val();
            var targetUrl = e.currentTarget.dataset["target_url"];
            // The target area may be anywhere, so the whole document is scanned
            $("." + className).empty();
            addLoader(getContent("GET", targetUrl, {"classname": className}, function (data) {
            	maintainHistory(className, onAjaxUpdate(data, className), data);
            }));
        });
	   	// Toast style messages
	   	$("input[type=hidden][name=user_message]").each(function() {
	   		var currentTarget = $(this);
	   		showAlert(currentTarget.data("alert", currentTarget.data("header"), currentTarget.val()));
	   		currentTarget.remove();
	   	});
	   	// Pagination
	   	el.find(".pagination").each(function(ndx, paginationContext) {
	   		paginationContext.className.split("/s+/").forEach(function(className) {
	   			var totalPagesInput = $("input[name=" + className + "-total-data-pages]").filter(":first");
	   			if(totalPagesInput.length > 0) {
	   				var val = totalPagesInput.val();
	   				var totalPages = (/^\d+$)/.test(val)) ? parseInt(val, 10) : 1;
	   				if(totalPages > 1) {
	   					// The following updates the global stateObj
	   					handlePaginationContext($(paginationContext), className, totalPages, true);
	   				} else {
	   					$(paginationContext).empty();
	   				}
	   			}
	   		});
	   	});
	   	var paginationControlCount = 0;
	   	$.each(stateObj, function(className, state) {
	   		paginationControlCount++;
	   		state["dataHtml"] = [];
	   		getNodesToUpdate(className).each(function(ndx, el) {
	   			state.dataHtml.push(el.innerHtml);
	   		});
	   	});
	   	if(paginationControlCount > 0) {
	   		history.replaceState(stateObj, null, window.location.toString());
	   	}
        // Typeahead
        el.find('input[type="text"][data-provide="typeahead"]').each(function () {
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
                source: function (query, process) {
                    self.toggleClass("typeahead-loading");
                    getContent(
                    	"GET",
                    	undefined,
                    	$.extend(params, {"query": query}),
                        function (data) { // called on success
                            process(getDataToRender(data)[0]);
                        },
                        function (data) { // called on error
                            console.log(data);
                        },
                        function () { // called always
                            self.toggleClass("typeahead-loading");
                        });
                },
                minLength: 1,
                items: 'all',
                autoSelect: false,
                delay: 1000,
                fitToElement: true,
                highlighter: function (item) {
                    return applyTemplate(highlighterTemplate, JSON.parse(item));
                },
                updater: function (item) {
                    return applyTemplate(updaterTemplate, item);
                },
                displayText: function (item) {
                    if (typeof (item) != "object") {
                        return item;
                    }
                    return JSON.stringify(item);
                }
            });
        });
        // Clear the modal content when hidden - to be populated by server on success only
        $('.modal').on('hidden.bs.modal', function() {
        	$(this).empty();
        });
    }

// On ajax update functions
    /**
     * Handles JSONML format, see: http://www.jsonml.org/
     */
    function onAjaxUpdate(data, className) {
    	var htmlArray = [];
        if (className) {
            var dataArray = getDataToRender(data);
            if (dataArray && dataArray.length > 0) {
                var nodesToUpdate = getNodesToUpdate(className);
                $.each(nodesToUpdate, function (ndx, el) {
                	if(ndx < dataArray.length) {
                		htmlArray.push(el.innerHTML);
	                    el.innerHTML = "";
	                    generateAjaxContent(el, dataArray[ndx], 0);
	                    init($(el));
                	} else {
                		return false;
                	}
                });
            }
        }
        return htmlArray;
    }

    function getDataToRender(data) {
        var dataArray;
        if (data.constructor === Array) {
            dataArray = data;
        } else if (typeof data === 'object') {
        	if(data && data.status && data.status === 302 && data.location && typeof data.location === 'string') {
        		// the way to redirect AJAX requests, otherwise not followed by the browser
        		window.location.replace(data.location);
        	} else {
	            $.each(data, function (key, val) {
	                if (val.constructor === Array) {
	                    dataArray = val;
	                    return false;
	                }
	            });
        	}
        }
        return dataArray;
    }

    function getTotalPages(data, className) {
        var totalPages;
        if (typeof data === 'object') {
            $.each(data, function (key, val) {
                if (typeof val === 'object') {
                    totalPages = val[className];
                    if (totalPages) {
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
        if (level > 0) {
            el = document.createElement(data[0]);
            parent.appendChild(el);
        }
        for (var i = 1; i < data.length; i++) {
            if (data[i].constructor === Array) {
                generateAjaxContent(el, data[i], 1 + level);
            } else if (typeof data[i] === 'object') {
                setAjaxAttributes(el, data[i]);
            } else if (typeof data[i] === 'string') {
                el.appendChild(document.createTextNode(data[i]));
            }
        }
    }

    function setAjaxAttributes(el, obj) {
        for (var name in obj) {
            el.setAttribute(name, obj[name]);
        }
    }

    /*------------------------------------------------------------------
    [  Very basic "template engine"  ]
    */

    function applyTemplate(template, params) {
        return template.map(function (tok, i) {
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

        if (active != 1) {
            content = applyTemplate(templates["start"], {"page": (active - 1)});
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
     * @return {boolean|String} content = false | html string
     */
    function buildPagination(paramsObj, paginationContext) {
        var content,
            totalPages = paramsObj.totalPages,
            currentPage = paramsObj.page,
            showRange = paramsObj.showRange,
            templates = paramsObj.templates;
        if (totalPages <= 0 || !paramsObj.showRange) {
            return false;
        } else if (totalPages < 2) {
            content = "";
        } else {
            if (totalPages < showRange * 2) {
                content = addPages(1, totalPages, currentPage, totalPages, templates);
            }
            else if (currentPage <= showRange * 2) {
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
    }

    function getRequestBody(oParams) {
        var aParams = [],
            sParam;
        $.each(oParams, function (j, valR) {
            sParam = "";
            sParam = encodeURIComponent(j);
            sParam += "=";
            sParam += encodeURIComponent(valR);
            aParams.push(sParam);
        });

        return aParams.join("&");
    }

    function getContent(type, url, params, callbackDone, callbackFail, callbackAlways) {
        return jQuery.ajax({
            beforeSend: function (xhr) {
                xhr.setRequestHeader("Accept", "application/json");
            },
            url: url || window.location.href.split("?")[0],
            data: params,
            type: type || 'GET'
        })
        .done(callbackDone)
        .fail(function(jqXHR, textStatus, errorThrown) {
        	showAlert('danger', 'Error', 'AJAX request failed');
        	if(callbackFail) {
        		callbackFail();
        	}
        })
        .always(callbackAlways);
    }

    function handlePaginationContext(paginationContext, className, totalPages, addEventHandler) {

        var templates = {};
        ["start", "active", "regular", "end"].forEach(function (templateName) {
            $('script[data-template="' + className + '-' + templateName + '-button-item"]').filter(":first").each(function (ndx, el) {
                templates[templateName] = $(el).html().split(/\{\{(.+?)\}\}/g);
            });
        });
        var currentPage = 1,
        	targetUrl = paginationContext.attr("data-target_url"),
            isSecondary = paginationContext.attr("data-secondary");

        var pageParamValue = parseInt(getCommonParams()[className]);
        if (pageParamValue && !isNaN(pageParamValue) && pageParamValue > 0) {
            currentPage = Math.min(pageParamValue, totalPages);
        }

        if (addEventHandler) {
            // on click rebuild pagination
            paginationContext.on("click", "li.page,.prev,.next,.start,.end", function (event) {

                // don't reload page on click
                event.preventDefault();

                var pagesAvailable = stateObj[className].totalPages;

                if ($(this).is('li.page')) {
                    currentPage = parseInt($(this).text());
                }
                if ($(this).is('.prev')) {
                    currentPage--;
                    if (currentPage < 1) {
                        currentPage = 1;
                    }
                    ;
                }
                if ($(this).is('.next')) {
                    currentPage++;
                    if (currentPage > pagesAvailable) {
                        currentPage = pagesAvailable;
                    }
                    ;
                }
                if ($(this).is('.start')) {
                    currentPage = 1;
                }
                if ($(this).is('.end')) {
                    currentPage = pagesAvailable;
                }

                var currentParams = getCommonParams();
                currentParams[className] = currentPage;

                addLoader(getContent(
                	"GET",
                	targetUrl,
                    $.extend({}, currentParams, {
                        "classname": className
                    }),
                    function (data) { // AJAX update success callback
                        onAjaxUpdate(data, className);
                        var state = stateObj[className];
                        state.page = currentPage;
                        state.data = data;
                        $("." + className + ".pagination").each(function (ndx, el) {
                            buildPagination(state, $(el));
                        });
                        //Change page number in url and push it to history
                        var newUrl = window.location.href.split("?")[0] + "?" + getRequestBody(currentParams);
                        history.pushState(stateObj, null, newUrl);
                    }
                ));
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

        return paginationContext;
    }

    function addPopstateListener() {
        if (!window.popstateListener) {
            // block adding multiple instances
            window.popstateListener = popstateListener;
            // History events handled
            window.addEventListener('popstate', window.popstateListener);
        }
    }

    function popstateListener(e) {
        var popObject = e.state;
        $.each(popObject, function (className, state) {
            $("." + className + ".pagination").each(function (ndx, el) {
                buildPagination(state, $(el));
            });
            if (state.data) {
                onAjaxUpdate(state.data, className);
            } else if (state.dataHtml) {
                getNodesToUpdate(className).each(function (ndx, el) {
                    $(el).html(state.dataHtml[ndx]);
                });
            }
        });
    }

    var stateObj = {};
    var ajaxUpdateHappened = false;
    
    function maintainHistory(className, htmlArray, data) {
    	if(htmlArray.length > 0) {
    		var state = {};
    		state[className] = stateObj[className] || {};
    		if(!ajaxUpdateHappened) {
    			state[className].dataHtml = htmlArray;
    			history.pushState(state, null, window.location.toString());
    		}
    		
    		ajaxUpdateHappened = true;
    		
    		addPopstateListener();
    		
    		//TODO restore form data
    		
    		state = {};
    		state[className] = stateObj[className] || {};
    		state.totalPages = getTotalPages(data, className);
    		state[className].data = getDataToRender(data);
    		
    		history.pushState(state, null, window.location.toString());
    	}
    }

    function showAlert(type, title, message) {
        var icon, theme;
        switch (type) {
            case "warning":
                icon = "fa fa-exclamation-triangle";
                theme = "jnoty-warning";
                break;
            case "danger":
                icon = "fa fa-times-circle-o";
                theme = "jnoty-danger";
                break;
            case "success":
                icon = "fa fa-check-square-o";
                theme = "jnoty-success";
                break;
            default:
                icon = "fa fa-info-circle";
                theme = "jnoty-info";
        }

        $.jnoty(message, {
            header: title,
            theme: theme,
            icon: icon,
            sticky: true,
            position: "top-right"
        });
    }

    $(document).ready(function ($) {
        // Render arbitrary data on page
        init($(this));
    });

    /*------------------------------------------------------------------
    [  Detect Browser an OS to show correct key combination to trigger accesskey  ]
    */
    var browser = navigator.userAgent.toLowerCase();

    // If Win or Linux
    if (navigator.appVersion.indexOf("Win") != -1 || navigator.appVersion.indexOf("Linux") != -1) {

        // If Firefox and Chrome
        if (!(window.mozInnerScreenX == null) || (browser.indexOf('chrome') > -1)) {
            $('.shortcut > i').text('Alt + Shift');
        }

    }

    // If MAC OS
    if (navigator.platform.toUpperCase().indexOf('MAC') >= 0) {
        $('.shortcut > i').text('Ctrl + Alt');
        // Check if Chrome's shortcuts are OK
    }

    /*------------------------------------------------------------------
    [  Loader & Cloak loader  ]
    */
    $(document).ready(function ($) {
        // Add loader to the innermost element to make IE happy
        $(document).on("click", ".addLoader:not(.disabled)", function (event) {
            addLoader();
        });

        // Add Cloak Loader
        $(".loadcloak:visible").addClass('hidden');
    });

    /*------------------------------------------------------------------
    [  Checkbox trigger state   ]
    */
    $(document).on('click', '.triggerState', function () {
        var forElement = $(this).attr('for');
        $("#" + forElement).trigger('click');
    });

    window.onbeforeunload = function () {
        $(".loadcloak:visible").removeClass('hidden');
    }

})();