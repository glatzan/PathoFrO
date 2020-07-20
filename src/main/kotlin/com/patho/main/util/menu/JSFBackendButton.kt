package com.patho.main.util.menu

import org.primefaces.behavior.ajax.AjaxBehavior
import org.primefaces.behavior.ajax.AjaxBehaviorListenerImpl
import org.primefaces.component.commandbutton.CommandButton
import org.slf4j.LoggerFactory
import javax.el.ELContext
import javax.el.ExpressionFactory
import javax.el.MethodExpression
import javax.faces.component.html.HtmlPanelGroup
import javax.faces.context.FacesContext

class JSFBackendButton : CommandButton() {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    companion object {
        const val DEFAULT_ID = "button%id"
    }

    fun initializeButton(signature: String, variables: List<VariableHolder<Any>> = listOf(), update: String = "", process: String = "", onComplete: String = "", value: String = "", style: String = "", id: String = ""): JSFBackendButton {
        this.value = value
        this.style = style

        this.process = process
        this.oncomplete = oncomplete
        this.update = update
        this.id = if ("" == id) DEFAULT_ID.replace(Regex("%id"), (Math.round(Math.random() * 1000)).toString()) else id

        this.actionExpression = buildMethodExpression(FacesContext.getCurrentInstance().application.expressionFactory, FacesContext.getCurrentInstance().elContext, signature, variables)

        return this
    }

    fun addAjaxBehaviorToButton(event: String, signature: String, variables: List<VariableHolder<Any>> = listOf(), update: String = "", process: String = "", onComplete: String = ""): JSFBackendButton {
        val ajaxBehavior = FacesContext.getCurrentInstance().application.createBehavior(AjaxBehavior.BEHAVIOR_ID) as AjaxBehavior

        ajaxBehavior.process = process
        ajaxBehavior.oncomplete = oncomplete
        ajaxBehavior.update = update

        val me = buildMethodExpression(FacesContext.getCurrentInstance().application.expressionFactory, FacesContext.getCurrentInstance().elContext, signature, variables)

        ajaxBehavior.addAjaxBehaviorListener(AjaxBehaviorListenerImpl(me, me))

        this.addClientBehavior(event, ajaxBehavior)

        return this
    }

    private fun buildMethodExpression(expressionFactory: ExpressionFactory, context: ELContext,
                                      methodSignature: String, variables: List<VariableHolder<Any>>): MethodExpression {
        val methodSignatureBuilder = StringBuilder("#{$methodSignature(")

        val classArr: Array<Class<Any>> = Array(variables.size) { p -> variables[p].obj.javaClass }

        for ((i, p) in variables.withIndex()) {
            context.variableMapper.setVariable(p.name,
                    expressionFactory.createValueExpression(p.obj, p.obj.javaClass))
            methodSignatureBuilder.append(p.name + if (i != variables.size - 1) "," else "")
        }

        methodSignatureBuilder.append(")}")

        logger.debug("Creating methode: $methodSignatureBuilder")

        return expressionFactory.createMethodExpression(context, methodSignatureBuilder.toString(), null, classArr)
    }

    fun addToParent(parent: HtmlPanelGroup?): JSFBackendButton {
        if (parent == null) {
            return this
        }
        parent.children.add(this)
        return this
    }

    class VariableHolder<out Any>(val obj: Any, val name: String)
}