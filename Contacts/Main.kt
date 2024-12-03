package contacts

import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.exitProcess

enum class Command(val command: String) {
    Add(command = "add"),
    List(command = "list"),
    Search(command = "search"),
    Count(command = "count"),
    Exit(command = "exit"),
}

enum class SearchActions(val command: String) {
    Back(command = "back"),
    Again(command = "again"),
}

enum class RecordAction(val command: String) {
    Edit(command = "edit"),
    Delete(command = "delete"),
    Menu(command = "menu")
}

enum class PersonFields(val field: String) {
    Name(field = "name"),
    Surname(field = "surname"),
    Number(field = "number"),
    Birth(field = "birth"),
    Gender(field = "gender"),
}

enum class OrganizationFields(val field: String) {
    Number(field = "number"),
    Address(field = "address"),
}

enum class Gender(val gender: String) {
    Male(gender = "M"),
    Female(gender = "F"),
}

enum class ContactType(val type: String) {
    Person(type = "person"),
    Organization(type = "organization"),
}

const val ENTER_ACTION_MESSAGE = "Enter action (add, list, search, count, exit):"
const val ENTER_NAME = "Enter the name:"
const val ENTER_SURNAME = "Enter the surname:"
const val ENTER_PHONE_NUMBER = "Enter the number:"
const val EMPTY_NUMBER = "[no number]"
const val SELECT_PERSON_FIELD = "Select a field (name, surname, birth, gender, number):"
const val SELECT_ORGANIZATION_FIELD = "Select a field (address, number):"
const val RECORD_UPDATED = "The record updated!"
const val RECORD_REMOVED = "The record removed!"
const val ENTER_TYPE = "Enter the type (person, organization):"
const val ENTER_BIRTH_DATE = "Enter the birth date:"
const val ENTER_GENDER = "Enter the gender (M, F):"
const val BAD_GENDER = "Bad gender!"
const val NO_DATA = "[no data]"
const val ENTER_ORGANIZATION_NAME = "Enter the organization name:"
const val ENTER_ADDRESS = "Enter the address:"
const val BAD_BIRTH_DATE = "Bad birth date!"
const val WRONG_NUMBER = "Wrong number format!"
const val ENTER_SEARCH_QUERY = "Enter search query:"
const val SEARCH_ENTER_ACTION = "[search] Enter action ([number], back, again):"
const val LIST_ENTER_ACTION = "[list] Enter action ([number], back):"
const val RECORD_ENTER_ACTION = "[record] Enter action (edit, delete, menu):"
const val LIST_EMPTY = "No records in list yet!"

private val PHONE_NUMBER_REGEX = Regex(
    "^\\+?(\\(\\d{2,}\\)|\\d{1,})(?:[-\\s](\\(\\d{2,}\\)|\\d{2,}))*$"
)

val BIRTH_DATE_REGEX = Regex(
    """^\\d{4}-(0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01])${'$'}"""
)

abstract class Contact(
    var name: String,
    number: String,
    val type: ContactType,
    val createdAt: String,
    var updatedAt: String
) {
    var number: String = EMPTY_NUMBER
        set(value) {
            if (validatePhoneNumber(value)) {
                field = value
            } else {
                println(WRONG_NUMBER)
                field = EMPTY_NUMBER
            }
        }

    init {
        this.number = number
    }

    abstract fun printInfo()
    abstract fun printInList(idx: Int)
    abstract fun updateField(field: String, value: String)
    abstract fun searchMatches(query: String): Boolean

    fun updateTimestamp() {
        updatedAt = getCurrentDateTime().toString()
    }

    private fun validatePhoneNumber(number: String): Boolean {
        return PHONE_NUMBER_REGEX.matches(number)
    }
}

class ContactPerson(
    name: String,
    number: String,
    var surname: String,
    birthDate: String,
    gender: String,
) : Contact(
    name,
    number,
    type = ContactType.Person,
    createdAt = getCurrentDateTime().toString(),
    updatedAt = getCurrentDateTime().toString()
) {
    var birthDate: String = NO_DATA
        set(value) {
            if (validateBirthDate(value)) {
                field = value
            } else {
                println(BAD_BIRTH_DATE)
                field = NO_DATA
            }
        }

    var gender: String = NO_DATA
        set(value) {
            if (validateGender(value)) {
                field = value
            } else {
                println(BAD_GENDER)
                field = NO_DATA
            }
        }

    init {
        this.birthDate = birthDate
        this.gender = gender
    }

    override fun printInfo() {
        println("Name: $name")
        println("Surname: $surname")
        println("Birth date: $birthDate")
        println("Gender: $gender")
        println("Number: $number")
        println("Time created: $createdAt")
        println("Time last edit: $updatedAt")
    }

    override fun printInList(idx: Int) {
        println("${idx + 1}. $name $surname")
    }

    override fun updateField(field: String, value: String) {
        when (field) {
            FieldsToEditEnum.Name.name -> name = value
            FieldsToEditEnum.Surname.name -> surname = value
            FieldsToEditEnum.Number.name -> number = value
            FieldsToEditEnum.BirthDate.name -> birthDate = value
            FieldsToEditEnum.Gender.name -> gender = value
            else -> throw IllegalArgumentException("Field $field not exists in ContactPerson")
        }

        updateTimestamp()
    }

    override fun searchMatches(query: String): Boolean {
        return this.name.contains(query, ignoreCase = true) || this.surname.contains(query, ignoreCase = true)
    }

    private fun validateBirthDate(birthDate: String): Boolean {
        return BIRTH_DATE_REGEX.matches(birthDate)
    }

    private fun validateGender(gender: String): Boolean {
        return gender == Gender.Male.gender || gender == Gender.Female.gender
    }

    enum class FieldsToEditEnum {
        Name,
        Surname,
        Number,
        BirthDate,
        Gender
    }
}

class ContactOrganization(
    name: String,
    number: String,
    var address: String
) : Contact(
    name,
    number,
    type = ContactType.Organization,
    createdAt = getCurrentDateTime().toString(),
    updatedAt = getCurrentDateTime().toString()
) {
    override fun printInfo() {
        println("Organization name: $name")
        println("Address: $address")
        println("Number: $number")
        println("Time created: $createdAt")
        println("Time last edit: $updatedAt")
    }

    override fun printInList(idx: Int) {
        println("${idx + 1}. $name")
    }

    override fun updateField(field: String, value: String) {
        when (field) {
            FieldsToEditEnum.Name.name -> name = value
            FieldsToEditEnum.Number.name -> number = value
            FieldsToEditEnum.Address.name -> address = value
            else -> throw IllegalArgumentException("Field $field not exists in ContactPerson")
        }

        updateTimestamp()
    }

    override fun searchMatches(query: String): Boolean {
        return this.name.contains(query, ignoreCase = true)
    }

    enum class FieldsToEditEnum() {
        Name,
        Number,
        Address
    }
}

fun main() {
    App().run()
}

class Contacts {
    val list = mutableListOf<Contact>()

    fun getContactByIdx(idx: Int): Contact {
        return list[idx]
    }

    fun getAllByQuery(query: String): List<Contact> {
        return list.filter { it.searchMatches(query) }
    }

    fun addContact(contact: Contact) {
        list.add(contact)
    }

    fun listContacts() {
        list.forEachIndexed { idx, contact -> contact.printInList(idx) }
    }

    fun removeContactByIdx(idx: Int) {
        list.removeAt(idx)
    }
}

class App() {
    private val contacts = Contacts()

    fun run() {
        app@ while (true) {
            println(ENTER_ACTION_MESSAGE)
            val command = readln()

            when (command) {
                Command.Exit.command -> {
                    break@app
                }

                Command.Count.command -> {
                    println("The Phone Book has ${contacts.list.size} records.")
                    println()
                }

                Command.Add.command -> {
                    println(ENTER_TYPE)
                    val type = readln()

                    when (type) {
                        ContactType.Person.type -> {
                            addPerson()
                        }

                        ContactType.Organization.type -> {
                            addOrganization()
                        }

                        else -> continue@app

                    }

                    println("The record added.")
                }

                Command.List.command -> {
                    if (contacts.list.isEmpty()) {
                        println(LIST_EMPTY)
                        continue@app
                    }

                    contacts.listContacts()

                    println(LIST_ENTER_ACTION)

                    val listCommand = readln()

                    if (listCommand.toIntOrNull() != null) {
                        contacts.getContactByIdx(listCommand.toInt() - 1).printInfo()
                    }
                }

                Command.Search.command -> {
                    search@ while (true) {
                        println(ENTER_SEARCH_QUERY)
                        val searchQuery = readln()

                        val result = contacts.getAllByQuery(searchQuery)
                        println("Fount ${result.size} results:")
                        result.forEachIndexed { idx, contact -> contact.printInList(idx) }
                        println()

                        println(SEARCH_ENTER_ACTION)
                        when (val action = readln()) {
                            SearchActions.Back.command -> {
                                break@search
                            }

                            SearchActions.Again.command -> {
                                continue@search
                            }

                            else -> {
                                if (action.toIntOrNull() != null) {
                                    val targetContact = result[action.toInt() - 1]
                                    targetContact.printInfo()

                                    println()
                                    println(RECORD_ENTER_ACTION)
                                    when (readln()) {
                                        RecordAction.Edit.command -> {
                                            edit(targetContact)
                                        }

                                        RecordAction.Delete.command -> {
                                            delete(targetContact)
                                        }

                                        RecordAction.Menu.command -> {
                                            break@search
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        exitProcess(0)
    }

    private fun addPerson() {
        println(ENTER_NAME)
        val name = readln()

        println(ENTER_SURNAME)
        val surname = readln()

        println(ENTER_PHONE_NUMBER)
        val number = readln()

        println(ENTER_BIRTH_DATE)
        val birthDate = readln()

        println(ENTER_GENDER)
        val gender = readln()

        contacts.addContact(
            ContactPerson(
                name = name,
                surname = surname,
                number = number,
                birthDate = birthDate,
                gender = gender
            )
        )
    }

    private fun addOrganization() {
        println(ENTER_ORGANIZATION_NAME)
        val name = readln()

        println(ENTER_ADDRESS)
        val address = readln()

        println(ENTER_PHONE_NUMBER)
        val number = readln()

        contacts.addContact(
            ContactOrganization(
                name = name,
                number = number,
                address = address
            )
        )
    }

    private fun editPerson(recordIdx: Int) {
        println(SELECT_PERSON_FIELD)
        val field = readln()
        val contact = contacts.getContactByIdx(recordIdx)

        when (field) {
            PersonFields.Name.field -> {
                println(ENTER_NAME)
                val name = readln()
                contact.updateField(ContactPerson.FieldsToEditEnum.Name.name, name)
            }

            PersonFields.Surname.field -> {
                println(ENTER_SURNAME)
                val surname = readln()
                contact.updateField(ContactPerson.FieldsToEditEnum.Surname.name, surname)
            }

            PersonFields.Number.field -> {
                println(ENTER_PHONE_NUMBER)
                val number = readln()
                contact.updateField(ContactPerson.FieldsToEditEnum.Number.name, number)
            }

            PersonFields.Gender.field -> {
                println(ENTER_GENDER)
                val gender = readln()
                contact.updateField(ContactPerson.FieldsToEditEnum.Gender.name, gender)
            }

            PersonFields.Birth.field -> {
                println(ENTER_BIRTH_DATE)
                val birth = readln()
                contact.updateField(ContactPerson.FieldsToEditEnum.BirthDate.name, birth)
            }
        }
    }

    private fun editOrganization(recordIdx: Int) {
        println(SELECT_ORGANIZATION_FIELD)
        val field = readln()
        val contact = contacts.getContactByIdx(recordIdx)

        when (field) {
            OrganizationFields.Number.field -> {
                println(ENTER_PHONE_NUMBER)
                val number = readln()
                contact.updateField(ContactOrganization.FieldsToEditEnum.Name.name, number)
            }

            OrganizationFields.Address.field -> {
                println(ENTER_ADDRESS)
                val address = readln()
                contact.updateField(ContactOrganization.FieldsToEditEnum.Address.name, address)
            }
        }
    }

    private fun edit(contact: Contact) {
        val recordIdx = contacts.list.indexOf(contact)

        when (contact.type) {
            ContactType.Person -> {
                editPerson(recordIdx)
            }

            ContactType.Organization -> {
                editOrganization(recordIdx)
            }
        }

        println(RECORD_UPDATED)
    }

    private fun delete(contact: Contact) {
        val recordIdx = contacts.list.indexOf(contact)
        contacts.removeContactByIdx(recordIdx)
        println(RECORD_REMOVED)
    }
}

fun Date.toString(locale: Locale = Locale.getDefault()): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", locale)
    return formatter.format(this)
}

fun getCurrentDateTime(): Date {
    return Calendar.getInstance().time
}
