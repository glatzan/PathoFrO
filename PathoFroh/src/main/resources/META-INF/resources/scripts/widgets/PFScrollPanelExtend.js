PrimeFaces.widget.ScrollPanel = PrimeFaces.widget.ScrollPanel
    .extend({
        init: function (cfg) {
            cfg.mouseWheelSpeed = 100
            cfg.resizeSensor = true
            cfg.autoReinitialise = false
            this._super(cfg);
        },
    });