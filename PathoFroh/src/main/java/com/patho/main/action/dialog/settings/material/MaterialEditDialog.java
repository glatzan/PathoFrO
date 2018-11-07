package com.patho.main.action.dialog.settings.material;

import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.model.MaterialPreset;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Getter
@Setter
public class MaterialEditDialog extends AbstractDialog<MaterialEditDialog> {
	private boolean newMaterial;

	private MaterialPreset materialPreset;

	public MaterialEditDialog initAndPrepareBean() {
		return initAndPrepareBean(new MaterialPreset());
	}

	public MaterialEditDialog initAndPrepareBean(MaterialPreset material) {
		if (initBean(material))
			prepareDialog();
		return this;
	}

	public boolean initBean(MaterialPreset material) {
		setNewMaterial(material.getId() == 0);
		setMaterialPreset(material);

		return super.initBean(task, Dialog.SETTINGS_STAINING_EDIT);
	}
}
