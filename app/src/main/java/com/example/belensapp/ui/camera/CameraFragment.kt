package com.example.belensapp.ui.camera

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.belensapp.R
import com.example.belensapp.api.ApiConfig
import com.example.belensapp.api.PredictResponse
import com.example.belensapp.databinding.FragmentCameraBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private var capturedPhoto: Bitmap? = null
    private var uploadedPhotoUri: Uri? = null

    private lateinit var progressBar: ProgressBar

    companion object {
        private const val REQUEST_CODE_TAKE_PHOTO = 1
        private const val REQUEST_CODE_UPLOAD_PHOTO = 2
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)

        progressBar = binding.progressBar
        progressBar.visibility = View.GONE

        binding.btnTakePhoto.setOnClickListener { openCamera() }
        binding.btnUploadPhoto.setOnClickListener { openGallery() }
        binding.btnProcess.setOnClickListener { processImage() }
        updateThemeAppearance()

        return binding.root
    }

    private fun updateThemeAppearance() {
        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.tvTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.nighttext))
                binding.btnTakePhoto.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                binding.btnUploadPhoto.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                binding.btnProcess.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                binding.btnTakePhoto.iconTint = ContextCompat.getColorStateList(requireContext(), R.color.black)
                binding.btnUploadPhoto.iconTint = ContextCompat.getColorStateList(requireContext(), R.color.black)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.btnTakePhoto.iconTint = null
                binding.btnUploadPhoto.iconTint = null
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateThemeAppearance()
    }

    private fun openCamera() {
        val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePhotoIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(takePhotoIntent, REQUEST_CODE_TAKE_PHOTO)
        } else {
            Toast.makeText(requireContext(), "Camera not available", Toast.LENGTH_SHORT).show()
        }
    }



    private fun openGallery() {
        val uploadPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        uploadPhotoIntent.type = "image/*"
        startActivityForResult(uploadPhotoIntent, REQUEST_CODE_UPLOAD_PHOTO)
    }

    private fun processImage() {
        showLoading(true)

        when {
            capturedPhoto != null -> {
                val file = bitmapToFile(capturedPhoto!!)
                file?.let {
                    uploadImage(it)
                }
            }
            uploadedPhotoUri != null -> {
                val file = uriToFile(uploadedPhotoUri!!)
                file?.let {
                    uploadImage(it)
                } ?: run {
                    showLoading(false)
                    Toast.makeText(requireContext(), "Failed to process uploaded photo", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                showLoading(false)
                Toast.makeText(requireContext(), "No photo selected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadImage(file: File) {
        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

        val client = ApiConfig.getApiService().predict(body)
        client.enqueue(object : Callback<PredictResponse> {
            override fun onResponse(call: Call<PredictResponse>, response: Response<PredictResponse>) {
                showLoading(false)

                if (response.isSuccessful) {
                    val result = response.body()
                    Toast.makeText(requireContext(), "Prediction: ${result?.data?.prediction}", Toast.LENGTH_LONG).show()

                    result?.let {
                        val bundle = Bundle()
                        bundle.putParcelable("predict_response", it)
                        bundle.putString("image_uri", uploadedPhotoUri.toString())
                        findNavController().navigate(R.id.navigation_result, bundle)
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PredictResponse>, t: Throwable) {
                showLoading(false)
                Toast.makeText(requireContext(), "Error: No Internet", Toast.LENGTH_SHORT).show()
                Log.e("CameraFragment", "onFailure: ${t.message}", t)
            }
        })
    }

    private fun showLoading(isLoading: Boolean) {
        activity?.runOnUiThread {
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnProcess.isEnabled = !isLoading
            binding.btnTakePhoto.isEnabled = !isLoading
            binding.btnUploadPhoto.isEnabled = !isLoading
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            binding.ivImagePreview.layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            binding.ivImagePreview.scaleType = ImageView.ScaleType.CENTER_CROP

            when (requestCode) {
                REQUEST_CODE_TAKE_PHOTO -> {
                    val photo: Bitmap? = data?.extras?.get("data") as? Bitmap
                    if (photo != null) {
                        capturedPhoto = photo
                        binding.ivImagePreview.setImageBitmap(photo)
                        val photoFile = bitmapToFile(capturedPhoto!!)
                        val photoUri = Uri.fromFile(photoFile)
                        uploadedPhotoUri = photoUri
                    }
                }
                REQUEST_CODE_UPLOAD_PHOTO -> {
                    val selectedImage: Uri? = data?.data
                    if (selectedImage != null) {
                        uploadedPhotoUri = selectedImage
                        Glide.with(this).load(selectedImage).into(binding.ivImagePreview)
                    }
                }
            }
        }
    }

    private fun bitmapToFile(bitmap: Bitmap): File {
        val timestamp = System.currentTimeMillis()
        val file = File(requireContext().cacheDir, "image_${timestamp}.jpg")
        file.createNewFile()
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
        val bitmapData = bos.toByteArray()
        FileOutputStream(file).apply {
            write(bitmapData)
            flush()
            close()
        }
        return file
    }

    private fun uriToFile(uri: Uri): File? {
        val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, uri)
        return bitmapToFile(bitmap)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}