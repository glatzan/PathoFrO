package com.patho.main.util.user

import com.patho.main.util.exception.DialogException

/**
 * Exception is thrown if histo group could not be found
 */
class HistoGroupNotFoundException : DialogException("Group not found")

/**
 * Exception is thrown if histo user could not be found
 */
class HistoUserNotFoundException : DialogException("User not found")