package com.patho.main.util.dialogReturn;

import com.patho.main.model.patient.Task;
import com.patho.main.model.user.HistoUser;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReloadUserEvent implements DialogReturnEvent {
	protected HistoUser user;
}
