package com.example.des3776

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HypertensionViewModel: ViewModel() {
    var observableData: MutableLiveData<ArrayList<Hypertension>> = MutableLiveData<ArrayList<Hypertension>>()

    init {
        var hypertensionData = ArrayList<Hypertension>()
        hypertensionData.add(Hypertension("none", 0, 0.0, 0.0))
        hypertensionData.add(Hypertension("none", 0, 0.0, 0.0))
        hypertensionData.add(Hypertension("none", 0, 0.0, 0.0))
        observableData.postValue(hypertensionData)
    }
}