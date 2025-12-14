package org.montigny.client.ui.organizations.departmentsAndSections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.montigny.client.dto.Organization.CreateSectDTO
import org.montigny.client.dto.Organization.DepartmentResponse
import org.montigny.client.ui.Theme.Danger

@Composable
fun DepartmentItem(
    department: DepartmentResponse,
    onEditDepartment: (DepartmentResponse) -> Unit,
    onAddSection: (DepartmentResponse) -> Unit,
    onEditSection: (CreateSectDTO) -> Unit,
    onDeleteSection:(CreateSectDTO) -> Unit,
    onDeleteDepartment: (DepartmentResponse) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    department.displayName,
                    style = MaterialTheme.typography.titleSmall
                )
                Row {
                    IconButton(onClick = { onEditDepartment(department) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Department")
                    }
                    IconButton(onClick = { onDeleteDepartment(department) }) {
                        Icon(Icons.Default.Delete,
                            contentDescription = "Delete Department",
                            tint = Danger
                        )
                    }
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Toggle Sections"
                        )
                    }
                }
            }

            if (expanded) {
                Column(
                    modifier = Modifier.padding(start = 12.dp, top = 4.dp)
                ) {
                    department.sections.forEach { section ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "• ${section.displayName}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { onEditSection(section) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Section")
                            }
                            IconButton(onClick = { onDeleteSection(section) }) {
                                Icon(Icons.Default.Delete,
                                    contentDescription = "Delete Section",
                                   tint = Danger)
                            }
                        }
                    }

                    TextButton(onClick = { onAddSection(department) }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Section")
                        Text("Add Section")
                    }
                }
            }
        }
    }
}
