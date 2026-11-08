package com.drivenext.app.ui.registration

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.drivenext.app.R
import com.drivenext.app.databinding.FragmentStep3Binding
import com.drivenext.app.ui.common.attachDatePicker
import com.drivenext.app.ui.common.showErrorDialog
import com.drivenext.app.ui.common.showPhoto
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import java.io.File

class Step3Fragment : Fragment(R.layout.fragment_step3) {

    private val viewModel: RegistrationViewModel by activityViewModels()

    private var _binding: FragmentStep3Binding? = null
    private val binding get() = _binding!!

    private val pickImageFromGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) onPhotoReceived(uri)
    }

    private val takePhotoWithCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSaved: Boolean ->
        val uri = viewModel.cameraPhotoUri
        if (isSaved && uri != null) onPhotoReceived(uri)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentStep3Binding.bind(view)

        binding.avatar.setOnClickListener { askPhotoSource(PhotoTarget.AVATAR) }
        binding.licensePhoto.setOnClickListener { askPhotoSource(PhotoTarget.LICENSE) }
        binding.passportPhoto.setOnClickListener { askPhotoSource(PhotoTarget.PASSPORT) }
        viewModel.savedPhotos().forEach { (target, uri) -> imageViewFor(target).showPhoto(uri) }

        binding.etLicense.doAfterTextChanged { updateNextButton() }
        binding.etIssueDate.doAfterTextChanged { updateNextButton() }
        updateNextButton()

        attachDatePicker(binding.tilIssueDate, binding.etIssueDate)

        binding.btnNext.setOnClickListener {
            val error = viewModel.register(
                licenseNumber = binding.etLicense.text.toString(),
                issueDate = binding.etIssueDate.text.toString()
            )
            if (error != null) showErrorDialog(error)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateNextButton() {
        binding.btnNext.isEnabled = viewModel.isStep3Filled(
            licenseNumber = binding.etLicense.text.toString(),
            issueDate = binding.etIssueDate.text.toString()
        )
    }

    private fun askPhotoSource(target: PhotoTarget) {
        viewModel.selectedPhotoTarget = target
        val sources = arrayOf(getString(R.string.gallery), getString(R.string.camera))

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.add_photo)
            .setItems(sources) { _, index ->
                if (index == 0) openGallery() else openCamera()
            }
            .show()
    }

    private fun openGallery() {
        pickImageFromGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun openCamera() {
        val uri = createCameraPhotoUri()
        viewModel.cameraPhotoUri = uri
        takePhotoWithCamera.launch(uri)
    }

    private fun createCameraPhotoUri(): Uri {
        val context = requireContext()
        val file = File(context.cacheDir, "images/photo_${System.currentTimeMillis()}.jpg")
        file.parentFile?.mkdirs()
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    private fun onPhotoReceived(uri: Uri) {
        val target = viewModel.savePhoto(uri) ?: return
        imageViewFor(target).showPhoto(uri)
    }

    private fun imageViewFor(target: PhotoTarget): ShapeableImageView = when (target) {
        PhotoTarget.AVATAR -> binding.ivAvatar
        PhotoTarget.LICENSE -> binding.ivLicensePhoto
        PhotoTarget.PASSPORT -> binding.ivPassportPhoto
    }
}
