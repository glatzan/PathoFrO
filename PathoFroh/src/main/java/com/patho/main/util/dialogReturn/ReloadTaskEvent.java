package com.patho.main.util.dialogReturn;

import com.patho.main.model.patient.Task;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReloadTaskEvent implements DialogReturnEvent {
	protected Task task;
}
