datatableOverlayExtend = {
    init: function (widgetVar) {

        if (!PrimeFaces.widgets[widgetVar] || !PrimeFaces.widgets[widgetVar + "_datatable"]) {
            PrimeFaces.error("Widget for var '" + widgetVar + "' not available!");
            return
        }

        // overwrite refresh
        PrimeFaces.widgets[widgetVar + "_datatable"].crefresh = function (cfg) {
            this._refresh(cfg);
            if (PrimeFaces.widgets[widgetVar + "_datatable"].executeOnEnter != null) {
                let filters = this.jq.find("[id$='filter']");
                filters.off("keydown");
                filters.on("keydown", function (e) {
                    if (e.which === 13) {
                        e.preventDefault();
                        PrimeFaces.widgets[widgetVar + "_datatable"].executeOnEnter();
                        return false;
                    }
                });
            }
        };


        if(PrimeFaces.widgets[widgetVar + "_datatable"]._refresh == null) {
            PrimeFaces.widgets[widgetVar + "_datatable"]._refresh = PrimeFaces.widgets[widgetVar + "_datatable"].refresh;
        }

        PrimeFaces.widgets[widgetVar + "_datatable"].refresh = PrimeFaces.widgets[widgetVar + "_datatable"].crefresh

        // show and focus
        PrimeFaces.widgets[widgetVar].showAndFocus = function (parentID, executeOnEnter) {
            if(this.isVisible())
                return false;

            this.show(parentID);
            PrimeFaces.widgets[widgetVar + "_datatable"].clearFilters();
            PrimeFaces.widgets[widgetVar + "_datatable"].unselectAllRows();
            PrimeFaces.widgets[widgetVar + "_datatable"].executeOnEnter = executeOnEnter
        };

    }
};
