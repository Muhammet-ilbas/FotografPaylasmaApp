package com.batuhan.fotorafpaylasmaapp.view

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import com.batuhan.fotorafpaylasmaapp.databinding.FragmentYuklemeBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import java.util.UUID


class YuklemeFragment : Fragment() {
    private var _binding: FragmentYuklemeBinding? = null
    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!

    private lateinit var activityResultLauncher : ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher : ActivityResultLauncher<String>

    var secilenGorsel : Uri? = null
    var secilenBitMap : Bitmap? = null

    private lateinit var auth : FirebaseAuth
    private lateinit var storage : FirebaseStorage
    private lateinit var db : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        storage = Firebase.storage
        db = Firebase.firestore

        registerLauncher()


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentYuklemeBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.yukleButton.setOnClickListener { yukleTiklandi(it) }
        binding.imageView.setOnClickListener { gorselSec(it) }

    }

    fun yukleTiklandi(view: View){
        val uuid = UUID.randomUUID()
        val gorselAdi = "${uuid}.jpg"

         val reference = storage.reference
        val gorselReferansi = reference.child("images").child(gorselAdi)
        if (secilenGorsel !=null)
        {
            gorselReferansi.putFile(secilenGorsel!!).addOnSuccessListener { uploadTask ->
                //urlyi alma kodu yazacağız
                gorselReferansi.downloadUrl.addOnSuccessListener { uri->
                    if (auth.currentUser != null)
                    {
                        val dowlandUrl = uri.toString()
                        //veri tabanına kayıt yapmamız gerekiyor
                        val postMap = hashMapOf<String,Any>()
                        postMap.put("dowlandUrl", dowlandUrl)
                        postMap.put("email",auth.currentUser!!.email.toString())
                        postMap.put("comment",binding.commentText.text.toString())
                        postMap.put("date", Timestamp.now())

                        db.collection("Posts").add(postMap).addOnSuccessListener { documentReference->
                            //veri database yüklenmiş oluyor
                            val action = YuklemeFragmentDirections.actionYuklemeFragment2ToFeedFragment23()
                            Navigation.findNavController(view).navigate(action)
                        }.addOnFailureListener { exception ->
                            Toast.makeText(requireContext(),exception.localizedMessage,Toast.LENGTH_LONG).show()
                        }

                    }


                }
            }.addOnFailureListener { exception ->
                Toast.makeText(requireContext(),exception.localizedMessage,Toast.LENGTH_LONG).show()
            }
        }

    }
    fun gorselSec(view: View){
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.TIRAMISU){
            if (ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.READ_MEDIA_IMAGES)!=PackageManager.PERMISSION_GRANTED)
            {
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),Manifest.permission.READ_MEDIA_IMAGES)){
                    Snackbar.make(view,"Galeriye gitmek için izin gerekiyor",Snackbar.LENGTH_INDEFINITE).setAction("Lütfen İzin Verin",
                        View.OnClickListener {
                            //izin istiyoruz
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        }  ).show()

                }else{
                    //izin istememiz lazım
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            }else{
                //izin var galeriye girme kodunu yazıcaz
                val intentToGalery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGalery)
                 }
        }else{
            //Read External Storage
            if (ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED)
            {
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),Manifest.permission.READ_EXTERNAL_STORAGE)){
                    Snackbar.make(view,"Galeriye gitmek için izin gerekiyor",Snackbar.LENGTH_INDEFINITE).setAction("Lütfen İzin Verin",
                        View.OnClickListener {
                            //izin istiyoruz
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }  ).show()

                }else{
                    //izin istememiz lazım
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }else{
                //izin var galeriye girme kodunu yazıcaz
                val intentToGalery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGalery)
            }

        }
    }

    fun registerLauncher() {
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val intentFromResult = result.data
                if (intentFromResult != null) {
                    secilenGorsel = intentFromResult.data
                }
                try {
                    if (Build.VERSION.SDK_INT >= 28) {
                        val source = ImageDecoder.createSource(requireActivity().contentResolver, secilenGorsel!!)
                        secilenBitMap = ImageDecoder.decodeBitmap(source)
                        binding.imageView.setImageBitmap(secilenBitMap)
                    } else {
                        secilenBitMap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, secilenGorsel)
                        binding.imageView.setImageBitmap(secilenBitMap)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result) {
                // izin verildi
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            } else {
                // kullanıcı izni reddetti
                Toast.makeText(requireContext(), "İzni reddettiniz, izne ihtiyacımız var", Toast.LENGTH_LONG).show()
            }
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}