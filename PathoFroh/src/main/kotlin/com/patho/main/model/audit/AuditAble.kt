package com.patho.main.model.audit

import com.patho.main.model.util.audit.Audit

interface AuditAble {
    open var audit: Audit?
}