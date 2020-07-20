package com.patho.main.model.interfaces

import com.patho.main.model.person.Person
import com.patho.main.service.impl.SpringContextBridge
import javax.persistence.Transient

interface FullName {

    var title: String

    var firstName: String

    var lastName: String

    var gender: Person.Gender

    /**
     * Returns a full name with title, name and surname.
     *
     * @return
     */
    @Transient
    fun getFullName(): String {
        val result = StringBuilder()

        if (title.isNotEmpty()) result.append("$title ")
        if (firstName.isNotEmpty()) result.append("$firstName ")
        if (lastName.isNotEmpty()) result.append("$lastName ")

        // remove the last space from the string
        return if (result.isNotEmpty())
            result.substring(0, result.length - 1)
        else
            ""
    }

    /**
     * Returns a gender name, surname and title
     *
     * @return
     */
    @Transient
    fun getFullNameAndTitle(): String {
        val result = StringBuilder()
        if (gender == Person.Gender.FEMALE)
            result.append(SpringContextBridge.services().resourceBundle.get("enum.gender.FEMALE_TITLE"))
        else
            result.append(SpringContextBridge.services().resourceBundle.get("enum.gender.MALE_TITLE"))

        result.append(getFullName())

        return result.substring(0, result.length - 1).replace("Apl.", "")
    }

    /**
     * Returns gender name and title
     */
    fun getNameAndTitle(): String {
        val result = StringBuilder()

        if (gender == Person.Gender.FEMALE)
            result.append(SpringContextBridge.services().resourceBundle.get("enum.gender.FEMALE_TITLE"))
        else
            result.append(SpringContextBridge.services().resourceBundle.get("enum.gender.MALE_TITLE"))

        if (title.isNotEmpty()) result.append("$title ")
        if (lastName.isNotEmpty()) result.append("$lastName ")

        return result.substring(0, result.length - 1).replace("Apl.", "")
    }
}