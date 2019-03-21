package com.patho.main.util.menu

import com.patho.main.ui.menu.MethodSignature
import com.patho.main.ui.menu.VaribaleHolder
import org.primefaces.behavior.ajax.AjaxBehavior
import org.primefaces.behavior.ajax.AjaxBehaviorListenerImpl
import org.primefaces.component.commandbutton.CommandButton
import javax.el.ELContext
import javax.el.ExpressionFactory
import javax.el.MethodExpression
import javax.faces.component.html.HtmlPanelGroup
import javax.faces.context.FacesContext

class JSFMethodSignature {
    private val signature: String
    private var update: String? = null
    private var process: String? = null
    private val buttonId: String

    private var onComplete: String? = null

    private val button: CommandButton? = null

    private val varibales: Array<VaribaleHolder<*>>

    internal var fc: FacesContext
    internal var ef: ExpressionFactory
    internal var context: ELContext

    fun MethodSignature(signature: String, vararg varibales: VaribaleHolder<*>): ??? {
        this(signature, null, null, null, *varibales)
    }

    fun MethodSignature(signature: String, update: String?, process: String?, onComplete: String?,
                        vararg varibales: VaribaleHolder<*>): ??? {
        this.varibales = varibales
        this.signature = signature
        this.update = update
        this.process = process
        this.onComplete = onComplete
        this.buttonId = "button" + Math.round(Math.random() * 1000)
    }

    fun addAjaxBehaviorToButton(event: String, siganture: MethodSignature): MethodSignature {
        val ajaxBehavior = fc.application.createBehavior(AjaxBehavior.BEHAVIOR_ID) as AjaxBehavior

        if (siganture.process != null)
            ajaxBehavior.process = siganture.process
        if (siganture.update != null)
            ajaxBehavior.update = siganture.update
        if (siganture.onComplete != null)
            ajaxBehavior.oncomplete = siganture.onComplete

        val me = buildMethodExpression(ef, context, siganture.signature, siganture.varibales)

        ajaxBehavior.addAjaxBehaviorListener(AjaxBehaviorListenerImpl(me, me))

        getButton().addClientBehavior(event, ajaxBehavior)

        return this
    }

    fun generateButton(): MethodSignature {
        fc = FacesContext.getCurrentInstance()
        ef = fc.application.expressionFactory
        context = fc.elContext

        val button = CommandButton()
        button.value = ""
        button.style = "visible:none"

        if (getProcess() != null)
            button.process = getProcess()
        if (getUpdate() != null)
            button.update = getUpdate()
        if (getOnComplete() != null)
            button.oncomplete = getOnComplete()

        // assign random id to avoid duplicate id for subsquent click
        button.id = getButtonId()
        button.actionExpression = buildMethodExpression(ef, context, getSignature(), getVaribales())

        setButton(button)

        return this
    }

    fun addToParent(parent: HtmlPanelGroup?): MethodSignature {
        if (parent == null) {
            return this
        }

        parent.children.add(getButton())

        return this
    }

    fun setUpdate(update: String): MethodSignature {
        getButton().setUpdate(update)
        this.update = update
        return this
    }

    fun setProcess(process: String): MethodSignature {
        getButton().setProcess(process)
        this.process = process
        return this
    }

    fun setOncomplete(onComplete: String): MethodSignature {
        getButton().setOncomplete(onComplete)
        this.onComplete = onComplete
        return this
    }

    private fun buildMethodExpression(ef: ExpressionFactory, context: ELContext,
                                      methodSignature: String, varibales: Array<VaribaleHolder<*>>?): MethodExpression {
        val methodSignatureBuilder = StringBuilder("#{$methodSignature(")
        var classArr: Array<Class<*>>? = null

        if (varibales != null) {
            classArr = arrayOfNulls(varibales.size)
            // creating variables
            for (i in varibales.indices) {
                context.variableMapper.setVariable(varibales[i].getVaribaleName(),
                        ef.createValueExpression(varibales[i].getObj(), varibales[i].getObj().javaClass))

                classArr[i] = varibales[i].getObj().javaClass
                methodSignatureBuilder.append(varibales[i].getVaribaleName() + if (i != varibales.size - 1) "," else "")
            }
        }

        methodSignatureBuilder.append(")}")
        log.debug("Creating methode: $methodSignatureBuilder")

        return ef.createMethodExpression(context, methodSignatureBuilder.toString(), null, classArr)
    }
}