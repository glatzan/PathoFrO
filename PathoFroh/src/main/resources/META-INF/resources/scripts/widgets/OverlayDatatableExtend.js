function extendOverlayDatatable(overlayWidgetVar) {
    var widget = PrimeFaces.widgets[overlayWidgetVar];

    if (!widget) {
        PrimeFaces.error("Widget for var '" + widgetVar + "' not available!");
    }else
        console.log(widget)

    PrimeFaces.widgets[overlayWidgetVar].testFunction = function () {

    };

}