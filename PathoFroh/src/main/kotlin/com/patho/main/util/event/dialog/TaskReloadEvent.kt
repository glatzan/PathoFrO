package com.patho.main.util.event.dialog

import org.apache.tools.ant.Task

/**
 * Task reload event
 */
class TaskReloadEvent(var task : Task) : ReloadEvent()