package com.ftg.carrepo.Screens

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.ftg.carrepo.RoomDB.MyDatabase
import com.ftg.carrepo.Services.BackgroundDownloadService
import com.ftg.carrepo.databinding.FragmentDownloadPageBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class DownloadPage : Fragment() {

    private lateinit var bind: FragmentDownloadPageBinding
    private lateinit var database: MyDatabase
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentDownloadPageBinding.inflate(inflater, container, false)
        database = MyDatabase.getDatabase(requireContext())
        setActionListeners()
        return bind.root
    }

    private fun setActionListeners(){

        bind.start.setOnClickListener {
            val intent = Intent(requireContext(), BackgroundDownloadService::class.java)
            requireActivity().startService(intent)
            makeToast("Downloading Start")
            bind.progressBar.visibility = View.INVISIBLE
            bind.downAnim.visibility = View.VISIBLE
        }

        bind.cancel.setOnClickListener {
            val intent = Intent(requireContext(), BackgroundDownloadService::class.java)
            requireActivity().stopService(intent)
            requireActivity().runOnUiThread {
                bind.downAnim.visibility = View.INVISIBLE
                bind.progressBar.visibility = View.INVISIBLE
            }
            makeToast("Canceled")
        }


        bind.restart.setOnClickListener {
            val intent = Intent(requireContext(), BackgroundDownloadService::class.java)
            requireActivity().stopService(intent)
            requireActivity().runOnUiThread {
                bind.downAnim.visibility = View.INVISIBLE
                bind.progressBar.visibility = View.VISIBLE
            }
            CoroutineScope(Dispatchers.IO).launch {
                withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                 //   database.getDao().deleteFullRecord()
                }
                requireActivity().runOnUiThread {
                    bind.downAnim.visibility = View.INVISIBLE
                    bind.progressBar.visibility = View.INVISIBLE
                    Toast.makeText(requireContext(), "Offline Record Cleared", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun makeToast(text: String) {
        requireActivity().runOnUiThread {
            Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
        }
    }
}