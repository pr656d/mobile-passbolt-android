package com.passbolt.mobile.android.feature.home.screen

import androidx.annotation.VisibleForTesting
import com.passbolt.mobile.android.core.mvp.authentication.BaseAuthenticatedPresenter
import com.passbolt.mobile.android.core.mvp.coroutinecontext.CoroutineLaunchContext
import com.passbolt.mobile.android.core.mvp.session.runAuthenticatedOperation
import com.passbolt.mobile.android.feature.secrets.usecase.decrypt.SecretInteractor
import com.passbolt.mobile.android.core.commonresource.GetResourcesUseCase
import com.passbolt.mobile.android.feature.autofill.resources.FetchAndUpdateDatabaseUseCase
import com.passbolt.mobile.android.mappers.ResourceModelMapper
import com.passbolt.mobile.android.storage.usecase.accountdata.GetSelectedAccountDataUseCase
import com.passbolt.mobile.android.ui.ResourceModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch

/**
 * Passbolt - Open source password manager for teams
 * Copyright (c) 2021 Passbolt SA
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License (AGPL) as published by the Free Software Foundation version 3.
 *
 * The name "Passbolt" is a registered trademark of Passbolt SA, and Passbolt SA hereby declines to grant a trademark
 * license to "Passbolt" pursuant to the GNU Affero General Public License version 3 Section 7(e), without a separate
 * agreement with Passbolt SA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not,
 * see GNU Affero General Public License v3 (http://www.gnu.org/licenses/agpl-3.0.html).
 *
 * @copyright Copyright (c) Passbolt SA (https://www.passbolt.com)
 * @license https://opensource.org/licenses/AGPL-3.0 AGPL License
 * @link https://www.passbolt.com Passbolt (tm)
 * @since v1.0
 */
class HomePresenter(
    coroutineLaunchContext: CoroutineLaunchContext,
    private val getResourcesUseCase: GetResourcesUseCase,
    private val resourceModelMapper: ResourceModelMapper,
    private val getSelectedAccountDataUseCase: GetSelectedAccountDataUseCase,
    private val fetchAndUpdateDatabaseUseCase: FetchAndUpdateDatabaseUseCase,
    private val secretInteractor: SecretInteractor
) : BaseAuthenticatedPresenter<HomeContract.View>(coroutineLaunchContext), HomeContract.Presenter {

    override var view: HomeContract.View? = null
    private val job = SupervisorJob()
    private val scope = CoroutineScope(job + coroutineLaunchContext.ui)
    private var currentSearchText: String = ""
    private var allItemsList: List<ResourceModel> = emptyList()
    private var currentMoreMenuResource: ResourceModel? = null

    override fun attach(view: HomeContract.View) {
        super<BaseAuthenticatedPresenter>.attach(view)
        fetchPasswords()
        getSelectedAccountDataUseCase.execute(Unit).avatarUrl?.let {
            view.displayAvatar(it)
        }
    }

    override fun detach() {
        scope.coroutineContext.cancelChildren()
        super<BaseAuthenticatedPresenter>.detach()
    }

    override fun searchTextChange(text: String) {
        currentSearchText = text
        filterList()
    }

    private fun fetchPasswords() {
        scope.launch {
            when (val result =
                runAuthenticatedOperation(needSessionRefreshFlow, sessionRefreshedFlow) {
                    getResourcesUseCase.execute(Unit)
                }) {
                is GetResourcesUseCase.Output.Failure<*> -> {
                    view?.hideRefreshProgress()
                    view?.hideProgress()
                    view?.showError()
                }
                is GetResourcesUseCase.Output.Success -> {
                    view?.hideRefreshProgress()
                    view?.hideProgress()
                    allItemsList = result.resources.map { resourceModelMapper.map(it) }
                    fetchAndUpdateDatabaseUseCase.execute(FetchAndUpdateDatabaseUseCase.Input(allItemsList))
                    displayPasswords()
                }
            }
        }
    }

    private fun displayPasswords() {
        if (allItemsList.isEmpty()) {
            view?.showEmptyList()
        } else {
            if (currentSearchText.isEmpty()) {
                view?.showPasswords(allItemsList)
            } else {
                filterList()
            }
        }
    }

    private fun filterList() {
        val filtered = allItemsList.filter {
            it.searchCriteria.contains(currentSearchText)
        }
        if (filtered.isEmpty()) {
            view?.showSearchEmptyList()
        } else {
            view?.showPasswords(filtered)
        }
    }

    override fun refreshClick() {
        view?.showProgress()
        fetchPasswords()
    }

    override fun refreshSwipe() {
        fetchPasswords()
    }

    override fun moreClick(resourceModel: ResourceModel) {
        currentMoreMenuResource = resourceModel
        view?.navigateToMore(resourceModel)
    }

    override fun itemClick(resourceModel: ResourceModel) {
        view?.navigateToDetails(resourceModel)
    }

    override fun menuLaunchWebsiteClick() {
        currentMoreMenuResource?.let {
            if (it.url.isNotEmpty()) {
                view?.openWebsite(it.url)
            }
        }
    }

    override fun menuCopyUsernameClick() {
        currentMoreMenuResource?.let {
            view?.addToClipboard(USERNAME_LABEL, it.username)
        }
    }

    override fun menuCopyUrlClick() {
        currentMoreMenuResource?.let {
            view?.addToClipboard(URL_LABEL, it.url)
        }
    }

    override fun menuCopyPasswordClick() {
        doAfterFetchAndDecrypt { decryptedSecret ->
            view?.addToClipboard(SECRET_LABEL, String(decryptedSecret))
        }
    }

    private fun doAfterFetchAndDecrypt(action: (ByteArray) -> Unit) {
        scope.launch {
            when (val output =
                runAuthenticatedOperation(needSessionRefreshFlow, sessionRefreshedFlow) {
                    secretInteractor.fetchAndDecrypt(requireNotNull(currentMoreMenuResource?.resourceId))
                }
            ) {
                is SecretInteractor.Output.DecryptFailure -> view?.showDecryptionFailure()
                is SecretInteractor.Output.FetchFailure -> view?.showFetchFailure()
                is SecretInteractor.Output.Success -> {
                    action(output.decryptedSecret)
                }
            }
        }
    }

    companion object {
        @VisibleForTesting
        const val SECRET_LABEL = "Secret"

        private const val USERNAME_LABEL = "Username"
        private const val URL_LABEL = "Url"
    }
}
