PrimeFaces.widget.AutoComplete = PrimeFaces.widget.AutoComplete
    .extend({

        init: function (cfg) {
            this._super(cfg);

            this.requestCanceled = false;
        },

        showSuggestions: function (query) {
            if (this.requestCanceled) {
                this.items = this.panel.find('.ui-autocomplete-item');
                this.items.attr('role', 'option');

                if (this.cfg.grouping) {
                    this.groupItems();
                }

                this.bindDynamicEvents();

                var $this = this, hidden = this.panel.is(':hidden');

                if (hidden) {
                    this.show();
                } else {
                    this.alignPanel();
                }

                if (this.items.length > 0) {
                    var firstItem = this.items.eq(0);

                    // highlight first item
                    if (this.cfg.autoHighlight && firstItem.length) {
                        firstItem.addClass('ui-state-highlight');
                    }

                    // highlight query string
                    if (this.panel.children().is('ul') && query.length > 0) {
                        this.items.each(function () {
                            var item = $(this), text = item.html(), re = new RegExp(
                                PrimeFaces.escapeRegExp(query), 'gi'),
                                highlighedText = text.replace(re, '$&');
                            item.html(highlighedText);
                        });
                    }

                    if (this.cfg.forceSelection) {
                        this.currentItems = [];
                        this.items.each(function (i, item) {
                            $this.currentItems.push($(item).attr('data-item-label'));
                        });
                    }

                    // show itemtip if defined
                    if (this.cfg.itemtip && firstItem.length === 1) {
                        this.showItemtip(firstItem);
                    }

                    this.displayAriaStatus(this.items.length + this.cfg.resultsMessage);
                } else {
                    if (this.cfg.emptyMessage) {
                        var emptyText =
                            '<div class="ui-autocomplete-emptyMessage ui-widget">'
                            + this.cfg.emptyMessage + '</div>';
                        this.panel.html(emptyText);
                    } else {
                        this.panel.hide();
                    }
                    this.displayAriaStatus(this.cfg.ariaEmptyMessage);
                }
            }
            this.requestCanceled = false;
        },

        cancelRequest: function () {
            this.requestCanceled = true;
        }

    });