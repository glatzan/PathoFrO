package com.patho.main.rest

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping(value = ["/data"])
open class PDFRestHandler {

    open fun handlePDFUpload(@RequestParam("file")  file : MultipartFile ,
                             @RequestParam(value = "piz", required = false)  piz : String,
                             @RequestParam(value = "taskID", required = false)  caseID :String,
                             @RequestParam(value = "fileName", required = false) fileName : String ,
                             @RequestParam(value = "documentType", required = false) documentType : String){

    }
}