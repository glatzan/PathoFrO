package com.patho.main.util.templates

/**
 * Error class for missing templates
 */
class TemplateNotFoundException(message: String) : RuntimeException("Template $message not found!") {
}