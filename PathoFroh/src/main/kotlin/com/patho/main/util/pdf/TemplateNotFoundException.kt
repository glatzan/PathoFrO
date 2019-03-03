package com.patho.main.util.pdf

import java.nio.file.FileSystemNotFoundException

class TemplateNotFoundException : FileSystemNotFoundException("Template not found") {
}