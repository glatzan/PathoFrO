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
				'summary' : arguments[1],
				'detail' : arguments[2],
				'severity' : arguments[3]
			});
		else {

		}
	}
}

/**
 * Sets the scrollspeet to an custom scrollbar
 * 
 * @param id
 * @param speed
 * @returns
 */
function setScrollPanelScrollSpeed(id, speed) {

	if (arguments.length == 1)
		speed = 100;

	var idDataTbl = getAlteredID(id);

	$(idDataTbl).jScrollPane({
		mouseWheelSpeed : speed
	});
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
	var primfcid = str.replace(':', '\\:');
	var idDataTbl = '#' + primfcid;

	return idDataTbl;
}

/**
 * REturns an url parameter
 * 
 * @param name
 * @returns
 */
function getURLParameter(name) {
	var value = decodeURIComponent((RegExp(name + '=' + '(.+?)(&|$)').exec(
			location.search) || [ , "" ])[1]);
	return (value !== 'null') ? value : false;
}

/**
 * Show an overlaypanel with a datatable. Will call a reset function, clear the
 * search filter and unselects all rows. A enter key press will be routet to an
 * submit function
 * 
 * @param panelID
 * @param parentID
 * @param resetContentFunction
 * @param clearFiltersID
 * @param focusElementID
 * @param submitFunction
 * @returns
 */
function showAndUpdateOverlayPanel(panelID, parentID, resetContentFunction,
		clearFiltersID, focusElementID, submitFunction) {

	// only work if panel is not visible
	if (!PF(panelID).jq.hasClass("ui-overlay-visible")) {

		PF(panelID).show(parentID);

		// reset values in backend bean
		resetContentFunction();

		// clear datatable
		PF(clearFiltersID).clearFilters();
		PF(clearFiltersID).unselectAllRows();

		// on enter submit
		var t = $(focusElementID);
		t.on("keydown", function(e) {
			if (e.which == 13) {
				submitNewCustomCaseHistory();
				e.preventDefault();
				return false;
			}
		});

		focusElement(focusElementID);
	}
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
	setTimeout(function() {
		var t = $(clintId);
		t.focus();
		// focus at the end of the input
		var test = t.val();
		t.val("");
		t.val(test);
	}, 200);

}