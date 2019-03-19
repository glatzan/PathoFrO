package com.patho.main.util.bearer

import com.patho.main.model.interfaces.ID
import java.io.Serializable

abstract class AbstactBearer(override var id: Long) : ID, Serializable {}