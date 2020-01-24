/**
 * disable button, args... widgetVar
 *
 * @param disable
 * @returns
 */
function disableButton(disable) {

    for (var i = 1; i < arguments.length; i++) {
        if (disable)
            PF(arguments[i]).disable();
        else
            PF(arguments[i]).enable();
    }

}

/**
 * Displays a growl message,even from a diaglo
 *
 * @param name
 * @returns
 */
function updateGlobalGrowl(name) {

    var windowElement = window;

    var i = 0;

    // searching for growl with name, going up the parent docuemntes (max 10
    // times) to finde it
    do {
        i++;
    } while (PrimeFaces.widgets[name] == null
    && (windowElement != windowElement.parent)
    && (windowElement = windowElement.parent) != null && i < 10)

    var growl = windowElement.PF(name);

    if (growl != null) {
        if (arguments.length == 2)
            growl.renderMessage(arguments[1])
        else if (arguments.length == 4)
            growl.renderMessage({
                'summary': arguments[1],
                'detail': arguments[2],
                'severity': arguments[3]
            });
        else {

        }
    }
}

/**
 * function is triggered by the backend to show a dialog, workaround
 *
 * @param btn
 * @returns
 */
function clickButtonFromBean(btn) {
    $(getAlteredID(btn)).click();
}

/**
 * function is executed by backend, workaround
 *
 * @param btn
 * @returns
 */
function executeFunctionFromBean(btn) {
    btn();
}

/**
 * Generates a pf id form a normal id text:test
 *
 * @param str
 * @returns
 */
function getAlteredID(str) {
    // for selection in JQuery the ids with : must be endoded with \\:
    var primfcid = str.replace(new RegExp(':', 'g'), '\\:');
    var idDataTbl = '#' + primfcid;

    return idDataTbl;
}

/**
 * Returns an url parameter
 *
 * @param name
 * @returns
 */
function getURLParameter(name) {
    var value = decodeURIComponent((RegExp(name + '=' + '(.+?)(&|$)').exec(
        location.search) || [, ""])[1]);
    return (value !== 'null') ? value : false;
}

/**
 * Sets the focus on an element with a delay. Is used to set the focus for
 * overlaypanels
 *
 * @param clintId
 * @returns
 */
function focusElement(clintId) {
    // settings focus, with an delay of 100ms
    setTimeout(function () {
        var t = $(clintId);
        t.focus();
        // focus at the end of the input
        var test = t.val();
        t.val("");
        t.val(test);
    }, 200);

}

var dataTableFunction = {
    syncColumnWidthOnGroupingTables: function (id, scrollbarWidth) {
        // Change the selector if needed
        var $table = $(id);
        var $bodyCells = $table.find('thead:first tr:first').children()
        // column with array
        var colWidth = $bodyCells.map(function () {
            return $(this).width();
        }).get();

        // extend last column
        colWidth[colWidth.length - 1] = colWidth[colWidth.length - 1] + scrollbarWidth;

        // Set the width of thead columns
        $table.find('tbody tr').children().each(function (i, v) {
            $(v).width(colWidth[i]);
        });
    }
}

var commonFunctions = {
    /**
     * Show an overlaypanel with a datatable. Will call a reset function, clear the
     * search filter and unselects all rows. A enter key press will be routed to an
     * submit function
     *
     * Updating the overlay should be performed via update of the calling object.
     *
     * Example for an single overlaypanel handling e.g. a whole datatable
     * <p:commandLink
     *  actionListener="setting object"
     *  partialSubmit="true"
     *  process="@this"
     *  update="contentForm:contentpanel"
     *  oncomplete="commonFunctions.showAndUpdateOverlayPanel('contactInfoOverlayPanel', '#{component.clientId}' ,null, 'null', null, null);">
     *  <i class="fa fa-info-circle"/>
     * </p:commandLink>
     *
     * @param panelID
     *            ID of the overlaypanel, widgetwar
     * @param parentID
     *            ID of the element the overlaypanel should be aligned with, id
     * @param resetContentFunction
     *            If data should be resetted on show, remotecommand oder javascript
     *            function
     * @param clearFiltersID
     *            Datatable to clear, widgetwar
     * @param focusElementID
     *            Element in overlaypanel which should be focused, id
     *            ('#contentForm\\:privatePhysicianOverlayPanelDataTable\\:nameColumn\\:filter');
     *            or number of the input element
     * @param submitFunction
     *            Function which will be called on enter while focus is located in
     *            the filter input
     * @returns
     */
    showAndUpdateOverlayPanel: function (panelID, parentID, resetContentFunction,
                                         dataTable, focusElementID, submitFunction) {

        // only work if panel is not visible
        // if (!PF(panelID).jq.hasClass("ui-overlay-visible")) {
        if (!PF(panelID).isVisible()) {

            PF(panelID).show(parentID);

            if (resetContentFunction != null) {
                // reset values in backend bean
                resetContentFunction();
            }

            if (dataTable != null) {
                // clear datatable
                PF(dataTable).clearFilters();
                PF(dataTable).unselectAllRows();
            }

            if (focusElementID != null) {
                var data = focusElementID;

                if (data === parseInt(data, 10)) {
                    // searching for filters to focus
                    var filters = PF(dataTable).jq.find("[id$='filter']");

                    if (filters.length > 0 && filters.length > data) {
                        data = "#" + filters[data].id.replace(/\:/g, "\\:");
                    }

                } else {
                    data = getAlteredID(focusElementID);
                }

                focusElement(data);
            }

            if (submitFunction != null) {

                // on enter submit
                var t = $(data);


                // submit on return only used if submitFunction is not null
                t.on("keydown", function (e) {
                    if (e.which == 13) {
                        submitFunction();
                        e.preventDefault();
                        return false;
                    }
                });
            }
        }
    },

    /**
     * Searches for an element with the parentClass and shows the
     * overlaypanel at that element'S position
     */
    showOverlayPanelParentByClass: function (panelID, parentClass) {
        parentClass = "." + parentClass;
        var tmpSel = $(parentClass);
        if (tmpSel.length >= 1) {
            this.showOverlayPanel(panelID, tmpSel[0].id)
        }
    },

    /**
     *  Shows the overlaypanel at the parentID
     * @param panelID
     *      ID and widget var of the overlay panel
     * @param parentID
     *      ID of the element the overlay panel should be aligned with
     * @resetContentFunction
     *      Javascript function which is called on dialog open
     */
    showOverlayPanel: function (panelID, parentID, resetContentFunction = null) {

        // only work if panel is not visible
        // if (!PF(panelID).jq.hasClass("ui-overlay-visible")) {
        if (!PF(panelID).isVisible()) {

            PF(panelID).show(parentID);

            if (resetContentFunction != null) {
                // reset values in backend bean
                resetContentFunction();
            }
        }
    },

    /**
     * Hides an overlayPanel
     * @param overlayPanelID
     */
    hideOverlayPanel: function (overlayPanelID) {
        if (PF(overlayPanelID).isVisible())
            PF(overlayPanelID).hide();
    },

    /**
     *  Removes the mousedown handler from the p:overlaypanel. Adds a new handler
     *  that will close the overplay panel if it was not target of the mouse click.
     *  If the overlay panel should jump between to call entities the enmities have to contain the
     *  css class "doNotHideOverlay_js_Class"
     *  @param overlayPanelID
     *      ID and widget var of the overly panel
     *  @doNotHideOnOverlayPanelClick
     *      If true the overlay panel will not be closed by clicking on a child of it
     */
    addGlobalHideOverlayPanelOnMouseClickHandler: function (overlayPanelID, doNotHideOnOverlayPanelClick = false) {
        //var hideNS = 'mousedown.idInputPannel';
        var hideNS = 'mousedown.' + overlayPanelID;

        // Function checks if click is on a link which should show the overlay. If so
        // and the overlay is visible the overlay will not be hidden. Otherwise the overlay will
        // be hidden
        $(document.body)
            .off(hideNS)
            .on(
                hideNS,
                function (e) {
                    // do nothing if overlay is hidden
                    if ($("#" + overlayPanelID).hasClass(
                        'ui-overlay-hidden')) {
                        return;
                    }

                    //do nothing on target mousedown
                    if ($("#" + overlayPanelID).target) {
                        var target = $(e.target);
                        if ($(hideNS).target.is(target)
                            || $this.target.has(target).length > 0) {
                            return;
                        }
                    }

                    // check if pannel is visivle
                    if (PF(overlayPanelID).isVisible()) {
                        // check if on link is click that should show the overlay
                        if ($(e.target).hasClass(
                            "doNotHideOverlay_js_Class")) {
                            return;
                        } else if (doNotHideOnOverlayPanelClick) {
                            var test = $(e.target);
                            var i = 0;
                            do {
                                if (test.get(0).id == PF(overlayPanelID).id) {
                                    return;
                                }
                            } while ((test = test.parent()).length != 0 && i++ < 40);
                            PF(overlayPanelID).hide();

                        } else {
                            PF(overlayPanelID).hide();
                        }
                    }
                });
    },

    addHandler: function (componentID, event, handlerFunction) {
        var hideNS = event + "." + componentID;
        $(document.body)
            .off(hideNS)
            .on(hideNS, function (e) {
                handlerFunction();
            });
    },

    /**
     * This will alter an element to show an overlay panel on mouse enter.
     * On mouse out the panel will be closed. This is applicable for e.g. buttons or command links.
     *
     * @param classID
     * @param overlayPanelID
     */
    addShowDialogOnMouseEnterForClickable: function (classID, overlayPanelID) {
        var tmpSel = $(classID);

        tmpSel.mouseenter(function () {
            this.click();
        });

        tmpSel.mouseleave(function () {
            commonFunctions.hideOverlayPanel(overlayPanelID)
        })
    },

    /**
     * This function will search for the element with the trigger class and will display the
     * overlay panel at the posion of the first found element.
     * This is applicable for e.g. treeNodes. (Links can not be used this treeNodes)
     * @param classID
     * @param overlayPanelID
     * @param identifierClass
     */
    addShowDialogOnMouseEnterForCommand: function (classID, overlayPanelID, identifierClass) {
        var tmpSel = $(classID);

        tmpSel.mouseenter(function () {
            var classes = this.className.split(' ');
            for (i = 0; i < classes.length; i++) {
                if (classes[i].startsWith("triggerCommand_")) {
                    var ex = classes[i].replace("triggerCommand_", "");
                    var fn = window[ex];
                    if (typeof fn === "function") {
                        this.className = this.className + " " + identifierClass;
                        fn();
                    }
                    break;
                }
            }
        });

        tmpSel.mouseleave(function () {
            commonFunctions.hideOverlayPanel(overlayPanelID);
            // removing the identifier class on mouse out
            this.className = this.className.replace(identifierClass, "").trim()
        })
    },
    /**
     * Sets the scrollspeet to an custom scrollbar
     *
     * @param id
     * @param speed
     * @param sensor
     * @returns
     */
    initializeCustomScrollPanel: function (id, speed = 100, sensor = true) {

        var idDataTbl = getAlteredID(id);

        $(idDataTbl).jScrollPane({
            mouseWheelSpeed: speed,
            resizeSensor: sensor,
            autoReinitialise: !sensor,
        });
    }

}