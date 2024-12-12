package com.example.belensapp.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class PredictResponse(
	@field:SerializedName("data")
	val data: Data? = null,

	@field:SerializedName("status")
	val status: String? = null,

	@field:SerializedName("NewsResponse")
	val newsResponse: List<NewsResponseItem?>? = null

) : Parcelable

@Parcelize
data class Gizi(

	@field:SerializedName("Nama Produk")
	val namaProduk: String? = null,

	@field:SerializedName("total_energi")
	val totalEnergi: Int? = null,

	@field:SerializedName("gula")
	val gula: Int? = null,

	@field:SerializedName("lemak_jenuh")
	val lemakJenuh: Int? = null,

	@field:SerializedName("grade")
	val grade: String? = null,

	@field:SerializedName("protein")
	val protein: Int? = null,

	@field:SerializedName("rekomendasi")
	val rekomendasi: String? = null,

	@field:SerializedName("garam")
	val garam: Int? = null
):Parcelable

@Parcelize
data class Data(

	@field:SerializedName("file_url")
	val fileUrl: String? = null,

	@field:SerializedName("gizi")
	val gizi: Gizi? = null,

	@field:SerializedName("confidence")
	val confidence: String? = null,

	@field:SerializedName("prediction")
	val prediction: String? = null,

	@field:SerializedName("message")
	val message: String? = null
) : Parcelable



@Parcelize
data class NewsResponseItem(

	@field:SerializedName("imageUrl")
	val imageUrl: String? = null,

	@field:SerializedName("description")
	val description: String? = null,

	@field:SerializedName("title")
	val title: String? = null,

	@field:SerializedName("url")
	val url: String? = null
) : Parcelable


