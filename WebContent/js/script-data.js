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
    	//TODO remove
    	$(".modal-body form").on('submit', function() {
    		history.back()
    	})
        el.find('.ajax-update:not(form)').on('click', function (e) {
            var context = $(e.currentTarget.dataset.target);
            var className = e.currentTarget.dataset["classname"];
            var targetUrl = e.currentTarget.dataset["target_url"];
            // The following HTML will be replaced by the actual content
            context.find("." + className).empty().html($('script[data-template="loading-indicator"]').html());
            var params = getCommonParams();
            params["location"] = window.location.toString(); // possibly redirect here to refresh page
            addLoader();
            getContent(targetUrl, $.extend(params, e.currentTarget.dataset), function (data) {
                maintainHistory(className, onAjaxUpdate(data, className), data);
            });
        });
        // Possibly render parts of page on click
       el.find('form.ajax-update').on('submit', function (e) {
            e.preventDefault();
            var fieldData = getCommonParams();
            var form = $(e.currentTarget);
            form.find("input, textarea, select").each(function (ndx, el) {
                // radio and checkbox input types not supported !!!
                fieldData[el.name] = $(el).val();
            });
            var className = fieldData["classname"];
            var targetUrl = fieldData["target_url"];
            var postEventName = form.data("post_event_name");
            var postEventTargetSelector = form.data("post_event_target");
            addLoader();
            getContent(targetUrl, fieldData, function (data) {
                maintainHistory(className, onAjaxUpdate(data, className), data);
                if(postEventName && postEventTargetSelector) {
                	$(postEventTargetSelector).trigger(postEventName);
                }
            });
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
            getContent(targetUrl, {"classname": className}, function (data) {
            	maintainHistory(className, onAjaxUpdate(data, className), data);
            });
        });
        // Possibly render parts of page on load/refresh
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
            var totalPages = getTotalPages(data, className);
            if (!isNaN(totalPages)) {
                $(".pagination." + className).each(function (ndx, paginationContext) {
                    if (totalPages > 1) {
                        handlePaginationContext($(paginationContext), className, totalPages, false);
                    } else {
                        $(paginationContext).empty();
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
            $.each(data, function (key, val) {
                if (val.constructor === Array) {
                    dataArray = val;
                    return false;
                }
            });
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
        paginationContext.on("click", ".addLoader", function (event) {
            if (!$(this).hasClass('disabled')) {
                addLoader();
            }
        });

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

    function getContent(url, params, callbackDone, callbackFail, callbackAlways) {
        jQuery.ajax({
            beforeSend: function (xhr) {
                xhr.setRequestHeader("Content-Type", "application/json");
                xhr.setRequestHeader("Accept", "application/json");
            },
            url: url || window.location.href.split("?")[0],
            data: params,
            type: 'GET'
        })
        .done(callbackDone)
        .fail(callbackFail)
        .always(function () {
            // Remove loader
            $("#loader").remove();
            if (callbackAlways) {
                callbackAlways();
            }
        });
    }

    function handlePaginationContext(paginationContext, className, totalPages, addEventHandler) {

        var templates = {};
        ["start", "active", "regular", "end"].forEach(function (templateName) {
            $('script[data-template="' + className + '-' + templateName + '-button-item"]').filter(":first").each(function (ndx, el) {
                templates[templateName] = $(el).html().split(/\{\{(.+?)\}\}/g);
            });
        });
        var currentPage = 1,
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

                getContent(
                	undefined,
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
    			state[className].data.Html = htmlArray;
    			history.pushState(state, null, window.location.toString());
    		}
    		
    		ajaxUpdateHappened = true;
    		
    		addPopStateListener();
    		
    		//TODO restore form data
    		
    		state = {};
    		state[className] = stateObj[className] || {};
    		state.totalPages = getTotalPages(data, className);
    		state[className].data = getDataToRender(data);
    		
    		history.pushState(state, null, window.location.toString());
    	}
    }

    $(document).ready(function ($) {
        $(".pagination").each(function (ndx, paginationContext) {
            paginationContext.className.split(/\s+/).forEach(function (className) {
                var totalPages = parseInt($("input[name='" + className + "-total-data-pages']").filter(":first").val(), 10) || 1;
                if (!isNaN(totalPages) && totalPages > 1) {
                    // Note the following updates the global stateObj
                    handlePaginationContext($(paginationContext), className, totalPages, true);
                }
            });
        });
        var paginationControlCount = 0;
        $.each(stateObj, function (className, state) {
            paginationControlCount++;
            state["dataHtml"] = [];
            getNodesToUpdate(className).each(function (ndx, el) {
                state.dataHtml.push(el.innerHTML);
            });
        });
        if (paginationControlCount > 0) {
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