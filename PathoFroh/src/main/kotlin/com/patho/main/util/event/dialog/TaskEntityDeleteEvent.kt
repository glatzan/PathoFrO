package com.patho.main.util.event.dialog

import com.patho.main.model.interfaces.ID

/**
 * Event with selected entity to delete
 */
class TaskEntityDeleteEvent(entity: ID) : SelectEvent<ID>(entity)