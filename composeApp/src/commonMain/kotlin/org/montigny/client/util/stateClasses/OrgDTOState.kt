package org.montigny.client.util.stateClasses

sealed class OrgDTOState {
    object DisplayName : OrgDTOState()
    data class Logo(val placeholderLogo : ByteArray?): OrgDTOState()
}