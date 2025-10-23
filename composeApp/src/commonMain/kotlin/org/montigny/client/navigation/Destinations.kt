package org.montigny.client.navigation

sealed class Destination(val route: String) {
    object Users : Destination("users")
    object Organizations : Destination("organizations")
    object Work : Destination("work")
    object Reports : Destination("reports")
    object Settings : Destination("settings")
    object Login : Destination("login")
    object MainSettings : Destination("main_settings")
    data class EditUser(val userId: String) : Destination("edit_user/$userId")
    object  CreateAccount: Destination("create_user")
    object CreateOrganization: Destination("create_organization")
    data class EditOrganization(val orgId: String) : Destination("edit_organization/$orgId")
    data class  ViewOrgWithDepartmentsAndSections(val userId : String) : Destination("view_org_with_departments_and_sections")
    object ManageStacksScreen: Destination("manage_stacks_screen")
    object CreateStack: Destination("create_stack")
    object ViewStackCodes:Destination("view_stack_codes")
    data class  EditStack(val  stackId : String):Destination("edit_stack")
    object  WorkLandingPage: Destination("work_landing_page")
    object  CreateWork: Destination("create_work")
    data class  EditWork(val  workId : String) : Destination("edit_work/$workId")
    data class  ViewUserWork(val  userId : String) : Destination("view_work/$userId")
    object  FilterUsers: Destination("filter_users")


}