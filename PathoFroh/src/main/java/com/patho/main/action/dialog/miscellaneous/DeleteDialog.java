package com.patho.main.action.dialog.miscellaneous;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.model.interfaces.ID;
import com.patho.main.service.BlockService;
import com.patho.main.service.SampleService;
import com.patho.main.service.SlideService;
import com.patho.main.util.event.dialog.TaskEntityDeleteEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
@Getter
@Setter
public class DeleteDialog extends AbstractDialog {

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private SampleService sampleService;

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private SlideService slideService;

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private BlockService blockService;

    private ID toDelete;

    private String headline;

    private String text;

    public DeleteDialog initAndPrepareBean(ID toDelete) {
        return initAndPrepareBean(toDelete, "", "");
    }

    public DeleteDialog initAndPrepareBean(ID toDelete, String headline, String text) {
        if (initBean(toDelete, headline, text))
            prepareDialog();
        return this;
    }

    public boolean initBean(ID toDelete, String headline, String text) {
        setToDelete(toDelete);
        setHeadline(headline.equals("") ? "" : resourceBundle.get(headline));
        setText(text.equals("") ? "" : resourceBundle.get(text));
        return super.initBean(Dialog.DELETE_ID_OBJECT);
    }

    public DeleteDialog header(String text) {
        return header(text, "");
    }

    public DeleteDialog header(String text, Object... args) {
        setHeadline(resourceBundle.get(text, args));
        return this;
    }

    public DeleteDialog ctext(String text) {
        return ctext(text, "");
    }

    public DeleteDialog ctext(String text, Object... args) {
        setText(resourceBundle.get(text, args));
        return this;
    }

    public void deleteAndHide() {
        hideDialog(new TaskEntityDeleteEvent(getToDelete()));
    }

}
