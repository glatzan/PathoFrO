datatableOverlayExtend = {
    init: function (widgetVar) {
        if (!PrimeFaces.widgets[widgetVar] || !PrimeFaces.widgets[widgetVar + "_datatable"]) {
            PrimeFaces.error("Widget for var '" + widgetVar + "' not available!");
            return
        }

        PrimeFaces.widgets[widgetVar].showAndFocus = function (parentID, executeOnEnter) {
            this.show(parentID);
            console.log("----")
            console.log(executeOnEnter)
            // PF(widgetVar + "_datatable").clearFilters();
            // PF(widgetVar + "_datatable").unselectAllRows();

            console.log(PF(widgetVar + "_datatable"))

            // let filters = PF(widgetVar + "_datatable").jq.find("[id$='filter']");
            PF(widgetVar + "_datatable").jq.find("[id$='filter']").on("keydown",  function(e){alert('afd')});
            // console.log("----1")
            //
            // console.log(filters)
            // filters[0].value = "asdsdas"
            //
            // // if (filters.length > 0) {
            //     setTimeout(function () {
            //         // filters[0].focus();
            //         //filters[0].value = filters[0].value;
            //         filters[0].value = "asdsdas"
            //     }, 500);
            //
            // // filters.off("keydown");
            // filters.on("keydown", function (e) {
            //     console.log("asfdasdfaaa")
            //     if (e.which === 13) {
            //         e.preventDefault();
            //         console.log("was")
            //         //executeOnEnter();
            //         return false;
            //     }
            // });

                // console.log("----2")
                // if (executeOnEnter != null) {
                //     console.log(filters)
                //     console.log("----3")
                //     filters.off("keydown");
                //     filters.on("keypress", function (e) {
                //         console.log("asfdasdfaaa")
                //         if (e.which === 13) {
                //             e.preventDefault();
                //             //executeOnEnter();
                //             return false;
                //         }
                //     });
                // }
            // }

        };
    }
};
