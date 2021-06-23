package com.passbolt.mobile.android.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ProcessLifecycleOwner
import com.passbolt.mobile.android.storage.cache.passphrase.PassphraseMemoryCache
import com.passbolt.mobile.android.storage.factory.EncryptedFileFactory
import com.passbolt.mobile.android.storage.factory.EncryptedSharedPreferencesFactory
import com.passbolt.mobile.android.storage.factory.KeySpecProvider
import com.passbolt.mobile.android.storage.repository.passphrase.PassphraseRepository
import com.passbolt.mobile.android.storage.usecase.GetAccountDataUseCase
import com.passbolt.mobile.android.storage.usecase.GetAccountsUseCase
import com.passbolt.mobile.android.storage.usecase.GetAllAccountsDataUseCase
import com.passbolt.mobile.android.storage.usecase.GetPassphraseUseCase
import com.passbolt.mobile.android.storage.usecase.GetPrivateKeyUseCase
import com.passbolt.mobile.android.storage.usecase.GetSelectedAccountUseCase
import com.passbolt.mobile.android.storage.usecase.GetSelectedUserPrivateKeyUseCase
import com.passbolt.mobile.android.storage.usecase.GetSessionUseCase
import com.passbolt.mobile.android.storage.usecase.RemoveAccountDataUseCase
import com.passbolt.mobile.android.storage.usecase.RemoveAccountUseCase
import com.passbolt.mobile.android.storage.usecase.RemoveAllAccountDataUseCase
import com.passbolt.mobile.android.storage.usecase.RemovePassphraseUseCase
import com.passbolt.mobile.android.storage.usecase.RemovePrivateKeyUseCase
import com.passbolt.mobile.android.storage.usecase.RemoveSelectedAccountPassphraseUseCase
import com.passbolt.mobile.android.storage.usecase.RemoveSelectedAccountUseCase
import com.passbolt.mobile.android.storage.usecase.RemoveSessionUseCase
import com.passbolt.mobile.android.storage.usecase.RemoveUserAvatarUseCase
import com.passbolt.mobile.android.storage.usecase.SaveAccountDataUseCase
import com.passbolt.mobile.android.storage.usecase.SaveAccountUseCase
import com.passbolt.mobile.android.storage.usecase.SavePassphraseUseCase
import com.passbolt.mobile.android.storage.usecase.SavePrivateKeyUseCase
import com.passbolt.mobile.android.storage.usecase.SaveSelectedAccountUseCase
import com.passbolt.mobile.android.storage.usecase.SaveSessionUseCase
import com.passbolt.mobile.android.storage.usecase.SaveUserAvatarUseCase
import com.passbolt.mobile.android.storage.usecase.UpdateAccountDataUseCase
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

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

val storageModule = module {
    single { provideSharedPreferences(androidApplication()) }
    single {
        EncryptedFileFactory(
            context = androidApplication(),
            keySpecProvider = get()
        )
    }
    single {
        EncryptedSharedPreferencesFactory(
            context = androidApplication(),
            keySpecProvider = get()
        )
    }
    single { KeySpecProvider() }
    single {
        SaveAccountUseCase(
            sharedPreferences = get()
        )
    }
    single {
        GetAccountDataUseCase(
            encryptedSharedPreferencesFactory = get()
        )
    }
    single {
        GetAccountsUseCase(
            sharedPreferences = get()
        )
    }
    single {
        GetPassphraseUseCase(
            encryptedFileFactory = get()
        )
    }
    single {
        GetSelectedUserPrivateKeyUseCase(
            encryptedFileFactory = get(),
            getSelectedAccountUseCase = get()
        )
    }
    single {
        GetPrivateKeyUseCase(
            encryptedFileFactory = get()
        )
    }
    single {
        GetSelectedAccountUseCase(
            encryptedSharedPreferencesFactory = get()
        )
    }
    single {
        GetAllAccountsDataUseCase(
            getAccountDataUseCase = get(),
            getAccountsUseCase = get()
        )
    }
    single {
        GetSessionUseCase(
            encryptedSharedPreferencesFactory = get()
        )
    }
    single {
        SaveAccountDataUseCase(
            encryptedSharedPreferencesFactory = get()
        )
    }
    single {
        UpdateAccountDataUseCase(
            getSelectedAccountUseCase = get(),
            encryptedSharedPreferencesFactory = get()
        )
    }
    single {
        SavePassphraseUseCase(
            encryptedFileFactory = get(),
            getSelectedAccountUseCase = get()
        )
    }
    single {
        SavePrivateKeyUseCase(
            encryptedFileFactory = get()
        )
    }
    single {
        SaveUserAvatarUseCase(
            getSelectedAccountUseCase = get(),
            encryptedFileFactory = get()
        )
    }
    single {
        SaveSelectedAccountUseCase(
            encryptedSharedPreferencesFactory = get()
        )
    }
    single {
        SaveSessionUseCase(
            encryptedSharedPreferencesFactory = get()
        )
    }
    single {
        PassphraseMemoryCache(
            coroutineLaunchContext = get(),
            lifecycleOwner = get(named<ProcessLifecycleOwner>())
        )
    }
    factory {
        PassphraseRepository(
            passphraseMemoryCache = get(),
            getPassphraseUseCase = get(),
            savePassphraseUseCase = get(),
            selectedAccountUseCase = get(),
            removeSelectedAccountPassphraseUseCase = get()
        )
    }
    factory {
        RemovePassphraseUseCase(androidContext())
    }
    factory {
        RemoveSelectedAccountPassphraseUseCase(
            androidContext(),
            getSelectedAccountUseCase = get()
        )
    }
    factory {
        RemoveAccountDataUseCase(encryptedSharedPreferencesFactory = get())
    }
    factory {
        RemoveAllAccountDataUseCase(
            getSelectedAccountUseCase = get(),
            removeAccountDataUseCase = get(),
            removePassphraseUseCase = get(),
            removePrivateKeyUseCase = get(),
            removeSelectedAccountUseCase = get(),
            removeSessionUseCase = get(),
            removeUserAvatarUseCase = get(),
            removeAccountUseCase = get()
        )
    }
    factory {
        RemoveUserAvatarUseCase(androidContext())
    }
    factory {
        RemoveSessionUseCase(encryptedSharedPreferencesFactory = get())
    }
    factory {
        RemoveSelectedAccountUseCase(encryptedSharedPreferencesFactory = get())
    }
    factory {
        RemovePrivateKeyUseCase(androidContext())
    }
    factory {
        RemoveAccountUseCase(
            sharedPreferences = get()
        )
    }
}

private fun provideSharedPreferences(appContext: Context): SharedPreferences {
    return appContext.getSharedPreferences("user-accounts", Context.MODE_PRIVATE)
}