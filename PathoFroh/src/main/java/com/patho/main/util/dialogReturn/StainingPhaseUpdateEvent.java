package com.patho.main.util.dialogReturn;

import com.patho.main.model.patient.Task;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StainingPhaseUpdateEvent {
	private Task task;
}
