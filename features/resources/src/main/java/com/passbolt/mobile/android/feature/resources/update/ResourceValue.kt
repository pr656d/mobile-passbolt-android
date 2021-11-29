package com.passbolt.mobile.android.feature.resources.update

import com.passbolt.mobile.android.entity.resource.ResourceField

class ResourceValue(
    val field: ResourceField,
    var value: String? = null
) {
    val uiTag: String
        get() = this.field.name
}
