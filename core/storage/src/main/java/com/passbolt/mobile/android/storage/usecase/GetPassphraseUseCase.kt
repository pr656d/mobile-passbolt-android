package com.passbolt.mobile.android.storage.usecase

import android.util.Base64
import com.passbolt.mobile.android.common.UseCase
import com.passbolt.mobile.android.common.extension.toCharArray
import com.passbolt.mobile.android.storage.cache.passphrase.PotentialPassphrase
import com.passbolt.mobile.android.storage.factory.EncryptedFileFactory
import com.passbolt.mobile.android.storage.factory.KeyBiometricSettings
import timber.log.Timber
import java.io.IOException

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

class GetPassphraseUseCase(
    private val encryptedFileFactory: EncryptedFileFactory
) : UseCase<GetPassphraseUseCase.Input, GetPassphraseUseCase.Output> {

    override fun execute(input: Input): Output {
        return try {
            val keyName = "${PASSPHRASE_FILE_NAME}_${input.userId}"
            val encryptedFile = encryptedFileFactory.get(
                fileName = keyName,
                keyBiometricSettings = KeyBiometricSettings(
                    authenticationRequired = true,
                    invalidatedByBiometricEnrollment = true
                )
            )

            encryptedFile.openFileInput().use {
                val bytes = it.readBytes()
                val decodedPassphrase = Base64.decode(bytes, Base64.DEFAULT)
                Output(PotentialPassphrase.Passphrase(decodedPassphrase.toCharArray()))
            }
        } catch (exception: IOException) {
            Timber.e(exception)
            Output(PotentialPassphrase.PassphraseNotPresent)
        }
    }

    class Input(
        val userId: String
    )

    class Output(
        val potentialPassphrase: PotentialPassphrase
    )
}
