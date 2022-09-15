package com.intelnet.omniwallet.ui.addWallet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.intelnet.omniwallet.base.BaseViewModel
import com.intelnet.omniwallet.repository.AddWalletRepository
import com.intelnet.omniwallet.repository.PreferencesRepository
import com.intelnet.omniwallet.util.Data
import com.intelnet.omniwallet.util.Event
import com.intelnet.omniwallet.util.Status
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

typealias EventPhrase = Event<Data<Pair<String, String>>>
typealias EventAddress = Event<Data<String>>

@HiltViewModel
class AddWalletViewModel @Inject
constructor(
    private val repository: AddWalletRepository,
    private val preferencesRepository: PreferencesRepository
) : BaseViewModel() {

    private val _phraseLiveData: MutableLiveData<EventPhrase> =
        MutableLiveData()
    val phraseLiveData: LiveData<EventPhrase> = _phraseLiveData

    private val _addressLiveData: MutableLiveData<EventAddress> =
        MutableLiveData()
    val addressLiveData: LiveData<EventAddress> = _addressLiveData

    fun createWallet(pass: String, remember: Boolean) {
        val disposable = repository.createWallet(pass)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                _phraseLiveData.value = Event(Data(responseType = Status.LOADING))
            }
            .doOnComplete {
                Timber.d("Close waitting dialog")
            }
            .subscribe(
                { response ->
                    Timber.d("On Next Called")
                    preferencesRepository.setRememberLogin(remember)
//                    preferencesRepository.setAddress(response.first)

                    _phraseLiveData.value =
                        Event(
                            Data(
                                responseType = Status.SUCCESSFUL,
                                data = response
                            )
                        )
                }, { error ->
                    Timber.e("On Error Called, ${error.message}")
                    _phraseLiveData.value =
                        Event(Data(Status.ERROR, null, error = Error(error.message)))
                }, {
                    Timber.d("On Complete Called")
                }
            )
        addDisposable(disposable)
    }

    fun importPrivateKey(prvKey: String, pass: String, remember: Boolean) {
        val disposable = repository.importPrivateKey(prvKey, pass)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                _addressLiveData.value = Event(Data(responseType = Status.LOADING))
            }
            .doOnComplete {
                Timber.d("Close waitting dialog")
            }
            .subscribe(
                { response ->
                    Timber.d("On Next Called: $response")
                    preferencesRepository.setRememberLogin(remember)
                    preferencesRepository.setAddress(response)

                    _addressLiveData.value =
                        Event(
                            Data(
                                responseType = Status.SUCCESSFUL,
                                data = response
                            )
                        )
                }, { error ->
                    Timber.e("On Error Called, ${error.message}")
                    _addressLiveData.value =
                        Event(Data(Status.ERROR, null, error = Error(error.message)))
                }, {
                    Timber.d("On Complete Called")
                }
            )
        addDisposable(disposable)
    }

    fun importWordPhrase(wordPhrase: String, pass: String, remember: Boolean) {
        val disposable = repository.importMnemonic(wordPhrase, pass)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                _addressLiveData.value = Event(Data(responseType = Status.LOADING))
            }
            .doOnComplete {
                Timber.d("Close waitting dialog")
            }
            .subscribe(
                { response ->
                    Timber.d("On Next Called")
                    preferencesRepository.setRememberLogin(remember)
                    preferencesRepository.setAddress(response)

                    _addressLiveData.value =
                        Event(
                            Data(
                                responseType = Status.SUCCESSFUL,
                                data = response.toString()
                            )
                        )
                }, { error ->
                    Timber.e("On Error Called, ${error.message}")
                    _addressLiveData.value =
                        Event(Data(Status.ERROR, null, error = Error(error.message)))
                }, {
                    Timber.d("On Complete Called")
                }
            )
        addDisposable(disposable)
    }
}