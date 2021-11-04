package com.example.des3776

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.ObservableArrayList
import java.io.Serializable

data class Hypertension(
    val name: String,
    val count: Int,
    val maxP: Double,
    val minP: Double
): Serializable
//    : BaseObservable() {
//
//    @get:Bindable
//    var name: String = "none"
//        set(value) {
//            field = value
//            notifyPropertyChanged(BR.name)
//        }
//
//    @get:Bindable
//    var count: Int = 0
//        set(value) {
//            field = value
//            notifyPropertyChanged(BR.count)
//        }
//
//    @get:Bindable
//    var maxP: Double = 0.0
//        set(value) {
//            field = value
//            notifyPropertyChanged(BR.maxP)
//        }
//
//    @get:Bindable
//    var minP: Double = 0.0
//        set(value) {
//            field = value
//            notifyPropertyChanged(BR.minP)
//        }
//}


//class ObservableHypertensions : BaseObservable() {
//    @get:Bindable
//    var data = mutableListOf<Hypertension>()
//        set(value) {
//            field = value
//            notifyPropertyChanged(BR.data)
//        }
//}