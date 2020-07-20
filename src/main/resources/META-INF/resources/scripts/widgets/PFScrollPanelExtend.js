PrimeFaces.widget.ScrollPanel = PrimeFaces.widget.ScrollPanel
    .extend({
        init: function (cfg) {
            cfg.mouseWheelSpeed = 100;
            cfg.resizeSensor = true;
            cfg.autoReinitialise = false;
            this._super(cfg);

            //
            // if (this.jsp._reinitialise != null)
            //     this.jsp._reinitialise = this.jsp.reinitialise
            //
            // this.jsp.reinitialise = function () {
            //     console.log("hallo")
            //     this.jsp._reinitialise()
            // }

        },

    });