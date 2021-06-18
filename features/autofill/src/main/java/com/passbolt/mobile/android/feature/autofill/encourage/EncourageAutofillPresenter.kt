package com.passbolt.mobile.android.feature.autofill.encourage

import com.passbolt.mobile.android.feature.autofill.info.AutofillInformationProvider

class EncourageAutofillPresenter(
    private val autofillInformationProvider: AutofillInformationProvider
) : EncourageAutofillContract.Presenter {

    override var view: EncourageAutofillContract.View? = null

    override fun goToSettingsClick() {
        if (!autofillInformationProvider.isAutofillSupported()) {
            view?.showAutofillNotSupported()
        } else {
            view?.openAutofillSettings()
        }
    }

    override fun closeClick() {
        view?.dismissWithNoAction()
    }

    override fun maybeLaterClick() {
        view?.dismissWithNoAction()
    }

    override fun autofillSettingsClosedWithResult() {
        if (autofillInformationProvider.isPassboltAutofillServiceSet()) {
            view?.closeWithSuccess()
        }
    }
}