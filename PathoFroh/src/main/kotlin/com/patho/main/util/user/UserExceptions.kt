package com.patho.main.util.user

/**
 * Exception is thrown if histo group could not be found
 */
class HistoGroupNotFoundException : Throwable("Group not found")

/**
 * Exception is thrown if histo user could not be found
 */
class HistoUserNotFoundException: Throwable("User not found")