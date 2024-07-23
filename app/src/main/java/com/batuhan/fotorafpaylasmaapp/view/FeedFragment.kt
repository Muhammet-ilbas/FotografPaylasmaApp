package com.batuhan.fotorafpaylasmaapp.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.batuhan.fotorafpaylasmaapp.R
import com.batuhan.fotorafpaylasmaapp.adapter.PostAdapter
import com.batuhan.fotorafpaylasmaapp.databinding.FragmentFeedBinding
import com.batuhan.fotorafpaylasmaapp.model.Post
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore


class FeedFragment : Fragment(),PopupMenu.OnMenuItemClickListener {
    private var _binding: FragmentFeedBinding? = null
    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!
    private lateinit var popup: PopupMenu
    private lateinit var auth : FirebaseAuth
    private lateinit var db : FirebaseFirestore
    private  val postList : ArrayList<Post> = arrayListOf()
    private  var adapter : PostAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
        db = Firebase.firestore

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.floatingActionButton.setOnClickListener { floatActionButtonTiklandi(it) }

        popup = PopupMenu(requireContext(),binding.floatingActionButton)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.my_popup_menu,popup.menu)
        popup.setOnMenuItemClickListener (this)

        fireStoreVerileriAl()

        adapter = PostAdapter(postList)
        binding.feedRecylerView.layoutManager = LinearLayoutManager(requireContext())
        binding.feedRecylerView.adapter = adapter
    }

    fun fireStoreVerileriAl(){
        db.collection("Posts").orderBy("date",Query.Direction.DESCENDING).addSnapshotListener { value, error ->
            if (error != null){
                Toast.makeText(requireContext(),error.localizedMessage,Toast.LENGTH_LONG).show()
            }else{
                if(value !=null && !value.isEmpty){
                    postList.clear()
                    val documents = value.documents
                    for (document in documents)
                    {
                        val comment = document.get("comment") as String
                        val email = document.get("email") as String
                        val dowlandUrl = document.get("dowlandUrl") as String

                        val post = Post(comment,dowlandUrl,email)
                        postList.add(post)
                    }
                    adapter?.notifyDataSetChanged()
                }
            }
        }
    }


    fun floatActionButtonTiklandi(view: View){
        popup.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.yuklemeItem) {
          //  val action = FeedFragmentDirections.actionFeedFragment2ToYuklemeFragment2()
          //  Navigation.findNavController(requireView()).navigate(action)
            actionFeedFragmentToYuklemeFragment(requireView())

        } else if (item?.itemId == R.id.cikisItem) {
            //cıkıs işlemi
            auth.signOut()
            //val action = FeedFragmentDirections.actionFeedFragment2ToKayitFragment2()
         //   Navigation.findNavController(requireView()).navigate(action)
           actionFeedFragmentToKayitFragment(requireView())
        }
        return true

    }

    fun actionFeedFragmentToYuklemeFragment(view: View){
        val action = FeedFragmentDirections.actionFeedFragment2ToYuklemeFragment2()
        Navigation.findNavController(requireView()).navigate(action)
    }
    fun actionFeedFragmentToKayitFragment(view: View)
    {
        val action = FeedFragmentDirections.actionFeedFragment2ToKayitFragment2()
        Navigation.findNavController(requireView()).navigate(action)
    }
}