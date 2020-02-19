diagnosisContainerComponentExtend = {
    init: function (widgetVar) {

        if (!PrimeFaces.widgets[widgetVar + "_overlay"]) {
            PrimeFaces.error("Widget for var '" + widgetVar + "' not available!");
            return
        }

        let input = PrimeFaces.widgets[widgetVar + "_overlay"].jq.find("[id$='diagnosisName']");

        input.off("keydown");
        input.on("keydown", function (e) {
            if (e.which === 13) {
                e.preventDefault();
                // fire onHide function
                if (PrimeFaces.widgets[widgetVar + "_overlay"].cfg != null)
                    PrimeFaces.widgets[widgetVar + "_overlay"].cfg.onHide();
                PrimeFaces.widgets[widgetVar + "_overlay"].hide();
                return false;
            }
        });

        PrimeFaces.widgets[widgetVar + "_overlay"].showAndFocus = function (clientID) {
            this.show(clientID);
            let input = this.jq.find("[id$='diagnosisName']");
            console.log(input)
            setTimeout(function () {
                let tmp = input[0].value;
                input[0].value = "";
                input[0].value = tmp;
            }, 100);
        };
    }
};