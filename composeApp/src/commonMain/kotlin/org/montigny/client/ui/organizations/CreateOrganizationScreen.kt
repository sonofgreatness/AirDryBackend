package org.montigny.client.ui.organizations

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.skia.Image
import org.montigny.client.navigation.Destination
import org.montigny.client.navigation.Navigator
import org.montigny.client.ui.Theme.GreenMain
import org.montigny.client.ui.organizations.viewModels.OrganizationViewModel
import org.montigny.client.util.stateClasses.OrgDTOState
import org.montigny.client.util.stateClasses.UiState
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.util.*
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOrganizationScreen(
    orgId : String? =null,
    organizationViewModel: OrganizationViewModel,
    navigator: Navigator,
    isUpdateOrganization: Boolean = false
) {
    val createdOrg by if(!isUpdateOrganization)
        organizationViewModel.createdOrganization.collectAsState()
    else
        organizationViewModel.updateOrganization.collectAsState()

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val nameError = createdOrg.displayName.isNullOrBlank()

    val uiState by if(!isUpdateOrganization)
        organizationViewModel.createOrganizationState.collectAsState()
    else
        organizationViewModel.updateOrganizationState.collectAsState()

    LaunchedEffect(orgId) {
        if(orgId == null && isUpdateOrganization)
            throw Exception("null Organization ID  supplied")
        if (isUpdateOrganization && orgId != null)
             organizationViewModel.initializeUpdateOrg(orgId)
    }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        val isWideScreen = maxWidth > 800.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // --- Header ---
            if(!isUpdateOrganization)
            {
            Text("Organization Profile", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Define your organization identity. Upload a logo and provide details below.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            }else {
                Text("Edit Organization Profile", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Define your organization identity. Upload a logo and provide details below.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(24.dp))

            // --- Logo Section (Top priority) ---
            LogoUploadCard(
                logoBytes = createdOrg.logo,
                onSelectLogo = { fileBytes, fileName ->
                    organizationViewModel.onCreateOrgChangeListener(fileName, OrgDTOState.Logo(fileBytes),isUpdateOrganization)
                }
            )

            Spacer(Modifier.height(24.dp))

            // --- Organization Details ---
            OrganizationDetailsFields(
                displayName = createdOrg.displayName.orEmpty(),
                focusManager = focusManager,
                onValueChange = { newValue, state ->
                    organizationViewModel.onCreateOrgChangeListener(newValue, state,isUpdateOrganization)
                },
                nameError = nameError
            )

            Spacer(Modifier.height(32.dp))

            // --- Actions ---
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            if(!isUpdateOrganization)
                            organizationViewModel.createOrganization()
                            else
                                organizationViewModel.updateOrganization()
                        }
                    },
                    enabled = !nameError &&  uiState !is UiState.Loading && uiState !is UiState.Success
                ) {
                    if(!isUpdateOrganization)
                    Text(if (uiState is UiState.Loading) "Creating..." else "Create")
                    else
                        Text(if (uiState is UiState.Loading) "Editing..." else "Edit")
                }

                OutlinedButton(onClick = { navigator.navigateTo(Destination.Organizations) }) {
                    Text("Done")
                }
            }

            Spacer(Modifier.height(16.dp))

            // --- Status Messages ---
            when (uiState) {
                is UiState.Error -> Text(
                    (uiState as UiState.Error).message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
                is UiState.Success -> Text(
                    if (!isUpdateOrganization)"✅ Organization created successfully!"
                    else "✅ Organization updated successfully!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                else -> {}
            }
        }
    }
}

// --- Organization Details ---
@Composable
fun OrganizationDetailsFields(
    displayName: String,
    focusManager: FocusManager,
    onValueChange: (String, OrgDTOState) -> Unit,
    nameError: Boolean
) {
    OutlinedTextField(
        value = displayName,
        onValueChange = { onValueChange(it, OrgDTOState.DisplayName) },
        label = { Text("Organization Name") },
        modifier = Modifier.fillMaxWidth(),
        isError = nameError,
        singleLine = true,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = {
            focusManager.moveFocus(FocusDirection.Down)
        })
    )
    if (nameError) {
        Text(
            "Organization name cannot be empty",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

// --- Logo Picker with Preview ---
@Composable
fun LogoUploadCard(
    logoBytes: ByteArray?,
    onSelectLogo: (ByteArray, String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var logoName by remember { mutableStateOf("No file selected") }
    var imageBitmap by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    // Decode and preview image
    LaunchedEffect(logoBytes) {
        logoBytes?.let { bytes ->
            try {
                val skiaImage = Image.makeFromEncoded(bytes)
                imageBitmap = skiaImage.toComposeImageBitmap()
                error = null
            } catch (e: Exception) {
                imageBitmap = null
                error = "Unsupported image format."
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(0.6f)
            .shadow(8.dp, shape = MaterialTheme.shapes.medium)
            .border(1.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.medium)
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Company Logo", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))

            // --- Logo Preview ---
            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap!!,
                    contentDescription = "Logo Preview",
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .border(2.dp, GreenMain, CircleShape)
                )
                Spacer(Modifier.height(8.dp))
                Text(logoName, style = MaterialTheme.typography.bodySmall)
            } else {
                Box(
                    Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Logo", color = MaterialTheme.colorScheme.outline)
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(onClick = {
                coroutineScope.launch(Dispatchers.IO) {
                    val file = chooseLogoFile()
                    if (file != null && file.exists()) {
                        if (file.extension.lowercase(Locale.getDefault()) in listOf("png", "jpg", "jpeg")) {
                            val bytes = file.readBytes()
                            logoName = file.name
                            onSelectLogo(bytes, file.name)
                        } else {
                            error = "Only PNG and JPG files are supported."
                        }
                    }
                }
            }) {
                Text("Select Logo")
            }

            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
/**
 * Opens a native file picker dialog (blocking call on desktop)
 */
private fun chooseLogoFile(): File? {
    return try {
        val chooser = JFileChooser().apply {
            dialogTitle = "Select Company Logo"
            fileFilter = FileNameExtensionFilter("Image Files (*.png, *.jpg, *.jpeg)", "png", "jpg", "jpeg")
            isAcceptAllFileFilterUsed = false
        }

        val result = chooser.showOpenDialog(null)
        if (result == JFileChooser.APPROVE_OPTION) chooser.selectedFile else null
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
