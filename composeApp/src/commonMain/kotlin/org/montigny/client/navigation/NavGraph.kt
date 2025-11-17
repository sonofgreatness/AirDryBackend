package org.montigny.client.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import org.montigny.client.auth.SessionManager
import org.montigny.client.domain.synchronization.SyncSettingsRepository
import org.montigny.client.ui.Theme.ThemeManager
import org.montigny.client.ui.organizations.CreateOrganizationScreen
import org.montigny.client.ui.organizations.OrganizationScreen
import org.montigny.client.ui.organizations.ViewOrgWithDepartmentsAndSectionsScreen
import org.montigny.client.ui.organizations.viewModels.OrganizationDepartmentsAndSectionsViewModel
import org.montigny.client.ui.organizations.viewModels.OrganizationViewModel
import org.montigny.client.ui.reports.GenerateReportsScreen
import org.montigny.client.ui.reports.ReportsScreen
import org.montigny.client.ui.reports.WeeklyApprovalScreen
import org.montigny.client.ui.reports.viewModel.GenerateReportsViewModel
import org.montigny.client.ui.reports.viewModel.WeekApprovalsViewModel
import org.montigny.client.ui.settings.MainSettingsScreen
import org.montigny.client.ui.settings.stacks.CreateStackScreen
import org.montigny.client.ui.settings.stacks.ManageStacksScreen
import org.montigny.client.ui.settings.stacks.ViewStackCodesScreen
import org.montigny.client.ui.settings.stacks.viewModel.StacksViewModel
import org.montigny.client.ui.users.EditUserFormScreen
import org.montigny.client.ui.users.FilterUsersScreen
import org.montigny.client.ui.users.LoginScreen
import org.montigny.client.ui.users.UserFormScreen
import org.montigny.client.ui.users.UsersScreen
import org.montigny.client.ui.users.viewModels.AuthViewModel
import org.montigny.client.ui.users.viewModels.FilterUsersViewModel
import org.montigny.client.ui.users.viewModels.UserViewModel
import org.montigny.client.ui.work.CreateOrUpdateWorkScreen
import org.montigny.client.ui.work.UserWorkScreen
import org.montigny.client.ui.work.WorkLandingScreen
import org.montigny.client.ui.work.WorkScreen
import org.montigny.client.ui.work.viewModel.CreateAndUpdateWorkViewModel
import org.montigny.client.ui.work.viewModel.LoadUserWorkViewModel

import org.montigny.client.ui.work.viewModel.LoadWorkViewModel

@Composable
fun NavGraph(
    navigator: Navigator,
    authViewModel: AuthViewModel,
    sessionManager: SessionManager,
    usersViewModel: UserViewModel,
    organizationViewModel : OrganizationViewModel,
    organizationViewModelWithDpt : OrganizationDepartmentsAndSectionsViewModel,
    syncSettingsRepository: SyncSettingsRepository,
    themeManager: ThemeManager,
    stacksViewModel: StacksViewModel,
    loadWorkViewModel: LoadWorkViewModel,
    createAndUpdateWorkViewModel: CreateAndUpdateWorkViewModel,
    loadUserWorkViewModel: LoadUserWorkViewModel,
    filterUsersViewModel: FilterUsersViewModel,
    weekApprovalsViewModel: WeekApprovalsViewModel,
    generateReportsViewModel: GenerateReportsViewModel,
    ) {
    val currentDestination by navigator.currentDestination
     val nextDestination by navigator.nextDestination
    when (currentDestination) {
        Destination.Users -> UsersScreen(usersViewModel,navigator)
        Destination.Organizations -> OrganizationScreen(organizationViewModel,navigator)
        Destination.Work -> WorkScreen(loadWorkViewModel,navigator)
        Destination.ManageStacksScreen-> ManageStacksScreen(stacksViewModel, navigator)
        Destination.Reports -> ReportsScreen(navigator)

        Destination.Login -> LoginScreen(authViewModel, sessionManager) {

            if (nextDestination != null )
                navigator.navigateTo(nextDestination!!)
            else
                navigator.navigateTo(Destination.Users)
        }
        Destination.MainSettings -> MainSettingsScreen(
            darkThemeEnabled = themeManager.isDarkThemeEnabled.value,
            onToggleTheme = { themeManager.setDarkTheme(it) },
            initialSyncIntervalHours = syncSettingsRepository.getSyncIntervalHours().toInt(),
            onSyncIntervalChange = { syncSettingsRepository.setSyncIntervalHours(it.toLong()) },
        )
        is Destination.EditUser -> {
            EditUserFormScreen(
                userId = (currentDestination as Destination.EditUser).userId,
                userViewModel = usersViewModel,
                navigator = navigator)
        }
        Destination.CreateAccount -> UserFormScreen(usersViewModel,navigator)
        Destination.CreateOrganization -> CreateOrganizationScreen(null,organizationViewModel,navigator)
        is Destination.EditOrganization -> {
            CreateOrganizationScreen(orgId = (currentDestination as Destination.EditOrganization).orgId,
                organizationViewModel,
                navigator,true)
        }
        is Destination.ViewOrgWithDepartmentsAndSections ->{
            ViewOrgWithDepartmentsAndSectionsScreen(
                userId = (currentDestination as Destination.ViewOrgWithDepartmentsAndSections).userId,
                navigator = navigator,
                organizationViewModel = organizationViewModelWithDpt) }
        Destination.CreateStack ->CreateStackScreen(
            viewModel =stacksViewModel,
            navigator = navigator)
        Destination.ViewStackCodes -> ViewStackCodesScreen(stacksViewModel,navigator)
        is Destination.EditStack -> CreateStackScreen(
            stackId =(currentDestination as Destination.EditStack).stackId ,
            viewModel =stacksViewModel,
            navigator = navigator,
            isUpdate = true)
        is  Destination.WorkLandingPage -> WorkLandingScreen(navigator)
        is Destination.CreateWork-> CreateOrUpdateWorkScreen(navigator, createAndUpdateWorkViewModel)
        is Destination.EditWork -> {
            CreateOrUpdateWorkScreen(navigator,createAndUpdateWorkViewModel
            ,true, workId = (currentDestination as Destination.EditWork).workId) }
        is Destination.ViewUserWork -> UserWorkScreen(
            userId = (currentDestination as Destination.ViewUserWork).userId,
            navigator = navigator,
            loadUserWorkViewModel,
            {
                if (nextDestination != null )
                    navigator.navigateTo(nextDestination!!)
                else
                    navigator.navigateTo(Destination.Work)
            }
        )
        is Destination.FilterUsers -> FilterUsersScreen(navigator,
            filterUsersViewModel, usersViewModel)
        is Destination.ApproveWeek -> WeeklyApprovalScreen(weekApprovalsViewModel)
        is Destination.GenerateReports -> GenerateReportsScreen(generateReportsViewModel)
    }
}
