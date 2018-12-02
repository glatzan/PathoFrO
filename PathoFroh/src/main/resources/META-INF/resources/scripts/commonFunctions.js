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
	var primfcid = str.replace(new RegExp(':', 'g'), '\\:');
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
function showAndUpdateOverlayPanel(panelID, parentID, resetContentFunction,
		dataTable, focusElementID, submitFunction) {

	// only work if panel is not visible
	if (!PF(panelID).jq.hasClass("ui-overlay-visible")) {

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
		
		var data = focusElementID;
		
		if (data === parseInt(data, 10)) {
			// searching for filters to focus
			var filters = PF(dataTable).jq.find("[id$='filter']");

			if (filters.length > 0 && filters.length > data) {
				data = "#"+filters[data].id.replace(/\:/g, "\\:");
			}
			
		} else {
			data =  getAlteredID(focusElementID);
		}
		
		if(data != null){
			focusElement(data);
		}
		
		if (submitFunction != null) {

			// on enter submit
			var t = $(data);

			
			// submit on return only used if submitFunction is not null
			t.on("keydown", function(e) {
				if (e.which == 13) {
					submitFunction();
					e.preventDefault();
					return false;
				}
			});
		}
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