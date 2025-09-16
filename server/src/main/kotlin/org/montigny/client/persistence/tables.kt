package org.montigny.client.persistence

import org.jetbrains.exposed.dao.id.UUIDTable
import org.montigny.client.persistence.converters.timestamptz

open class BaseTable(name: String) : UUIDTable(name) {
    val createdAt = timestamptz("created_at").clientDefault { java.time.Instant.now() }
    val updatedAt = timestamptz("updated_at").clientDefault { java.time.Instant.now() }
    val createdBy = uuid("created_by").nullable()
    val updatedBy = uuid("updated_by").nullable()
}
object UserTypes : BaseTable("user_type") {
    val displayname = varchar("displayname", 50)
}
object Organizations : BaseTable("organization") {
    val displayname = varchar("displayname", 42)
    val logo = binary("logo").nullable()
}
object Departments : BaseTable("department") {
    val displayname = varchar("displayname", 42)
    val organizationId = reference("organization_id", Organizations)
}
object Sections : BaseTable("section") {
    val displayname = varchar("displayname", 42)
    val departmentId = reference("department_id", Departments)
}
object AppUsers : BaseTable("app_user") {
    val name = varchar("name", 42)
    val surname = varchar("surname", 42)
    val phoneNumber = varchar("phone_number", 15)
    val secondaryPhone = varchar("secondary_phone_number", 15).nullable()
    val isApproved = bool("is_approved").default(false)
    val isActive = bool("is_active").default(true)
    val userTypeId = reference("user_type_id", UserTypes).nullable()
    val organizationId = reference("organization_id", Organizations).nullable()
    val departmentId = reference("department_id", Departments).nullable()
    val sectionId = reference("section_id", Sections).nullable()
    val email = varchar("email", 255).nullable().index(isUnique = true)
    val passwordHash = varchar("password_hash", 255).nullable()
}

