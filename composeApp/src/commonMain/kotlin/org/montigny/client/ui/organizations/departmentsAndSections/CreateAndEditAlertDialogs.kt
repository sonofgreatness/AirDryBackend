package org.montigny.client.ui.organizations.departmentsAndSections

import androidx.compose.material.AlertDialog
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.montigny.client.dto.Organization.CreateSectDTO
import org.montigny.client.dto.Organization.DepartmentResponse
import org.montigny.client.util.stateClasses.UserRegisterDTOState
import java.util.UUID

@Composable
fun EditDepartmentDialog(department: DepartmentResponse, onDismiss: () -> Unit, onConfirm: (String, UUID?) -> Unit) {
    var name by remember { mutableStateOf(department.displayName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Department") },
        text = {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Department Name") })
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name,department.id) }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun EditSectionDialog(section: CreateSectDTO, onDismiss: () -> Unit, onConfirm: (String, UUID?, UUID?) -> Unit) {
    var name by remember { mutableStateOf(section.displayName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Section") },
        text = {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Section Name") })
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name, section.id , section.departmentId) }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun CreateDepartmentDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Department") },
        text = {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Department Name") })
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name) }) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun CreateSectionDialog(department: DepartmentResponse, onDismiss: () -> Unit, onConfirm: (String,UUID?) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Section for ${department.displayName}") },
        text = {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Section Name") })
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name,department.id) }) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
