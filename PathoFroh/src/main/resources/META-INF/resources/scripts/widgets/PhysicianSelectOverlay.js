selectPhysicianOverlay = {
    init: function (widgetVar) {
        if (!PrimeFaces.widgets[widgetVar] || !PrimeFaces.widgets[widgetVar + "_datatable"]) {
            PrimeFaces.error("Widget for var '" + widgetVar + "' not available!");
            return
        }

        let _show = PrimeFaces.widgets[widgetVar].show;

        PrimeFaces.widgets[widgetVar].showAndFocus = function (parentID) {
            this.show(parentID);

            PF(widgetVar + "_datatable").clearFilters();
            PF(widgetVar + "_datatable").unselectAllRows();

            let filters = PF(widgetVar + "_datatable").jq.find("[id$='filter']");

            if (filters.length > 0) {
                setTimeout(function () {
                    filters[0].focus();
                    filters[0].value = filters[0].value;
                }, 200);
            }
        };

    }
};
