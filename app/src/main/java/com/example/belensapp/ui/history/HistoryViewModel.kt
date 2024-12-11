package com.example.belensapp.ui.history

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.belensapp.api.Data
import com.example.belensapp.api.Gizi
import com.example.belensapp.api.PredictResponse
import com.google.firebase.database.*

class HistoryViewModel : ViewModel() {

    private val _historyList = MutableLiveData<List<PredictResponse>>()
    val historyList: LiveData<List<PredictResponse>> = _historyList

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("history")

    // Fungsi untuk mengambil riwayat prediksi dari Firebase
    fun fetchHistory(userToken: String) {
        Log.d("HistoryViewModel", "Fetching history for token: $userToken")

        database.child(userToken).orderByChild("timestamp")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("HistoryViewModel", "Data snapshot received: ${snapshot.exists()}")
                    val historyList = mutableListOf<PredictResponse>()

                    if (!snapshot.exists()) {
                        _historyList.value = emptyList()
                        return
                    }

                    snapshot.children.forEach { dataSnapshot ->
                        try {
                            val historyMap = dataSnapshot.value as? Map<*, *>
                            val dataMap = historyMap?.get("data") as? Map<*, *>

                            if (dataMap != null) {
                                val giziMap = dataMap["gizi"] as? Map<*, *>
                                val gizi = giziMap?.let {
                                    Gizi(
                                        namaProduk = it["Nama Produk"] as? String,
                                        grade = it["grade"] as? String,
                                        totalEnergi = (it["totalEnergi"] as? Number)?.toInt(),
                                        protein = (it["protein"] as? Number)?.toInt(),
                                        gula = (it["gula"] as? Number)?.toInt(),
                                        lemakJenuh = (it["lemakJenuh"] as? Number)?.toInt(),
                                        rekomendasi = it["rekomendasi"] as String,
                                        garam = (it["garam"] as? Number)?.toInt()
                                    )
                                }

                                val data = Data(
                                    fileUrl = dataMap["fileUrl"] as? String,
                                    prediction = dataMap["prediction"] as? String,
                                    confidence = dataMap["confidence"] as? String,
                                    message = dataMap["message"] as? String,
                                    gizi = gizi
                                )

                                val predictResponse = PredictResponse(
                                    data = data,
                                    status = "success"
                                )

                                historyList.add(predictResponse)
                            }
                        } catch (e: Exception) {
                            Log.e("HistoryViewModel", "Error parsing history item", e)
                        }
                    }

                    _historyList.value = historyList.reversed()
                    Log.d("HistoryViewModel", "Final history list size: ${historyList.size}")
                }

                override fun onCancelled(error: DatabaseError) {
                    _error.value = "Failed to load history: ${error.message}"
                    Log.e("HistoryViewModel", "Error fetching history", error.toException())
                }
            })
    }

    // Fungsi untuk menyimpan prediksi ke Firebase
    fun savePredictionHistory(userToken: String, predictResponse: PredictResponse) {
        val timestamp = System.currentTimeMillis()

        // Menyimpan data prediksi ke Firebase dengan userToken dan timestamp sebagai path
        val historyData = mapOf(
            "timestamp" to timestamp,
            "data" to mapOf(
                "fileUrl" to predictResponse.data?.fileUrl,
                "prediction" to predictResponse.data?.prediction,
                "confidence" to predictResponse.data?.confidence,
                "message" to predictResponse.data?.message,
                "gizi" to mapOf(
                    "namaProduk" to predictResponse.data?.gizi?.namaProduk,
                    "grade" to predictResponse.data?.gizi?.grade,
                    "totalEnergi" to predictResponse.data?.gizi?.totalEnergi,
                    "protein" to predictResponse.data?.gizi?.protein,
                    "gula" to predictResponse.data?.gizi?.gula,
                    "lemakJenuh" to predictResponse.data?.gizi?.lemakJenuh,
                    "rekomendasi" to predictResponse.data?.gizi?.rekomendasi,
                    "garam" to predictResponse.data?.gizi?.garam
                )
            )
        )

        database.child(userToken).child(timestamp.toString()).setValue(historyData)
            .addOnSuccessListener {
                Log.d(
                    "HistoryViewModel",
                    "Prediction history saved successfully with gizi: ${predictResponse.data?.gizi}"
                )
            }
            .addOnFailureListener { e ->
                Log.e("HistoryViewModel", "Failed to save prediction history", e)
                _error.value = "Failed to save prediction history: ${e.message}"
            }
    }
}
