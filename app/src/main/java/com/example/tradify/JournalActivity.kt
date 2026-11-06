package com.example.tradify

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class JournalActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private lateinit var listView: ListView
    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_journal)

        listView = findViewById(R.id.listJournal)
        etTitle = findViewById(R.id.etJournalTitle)
        etContent = findViewById(R.id.etJournalContent)
        val btnSave = findViewById<Button>(R.id.btnSaveJournal)

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val content = etContent.text.toString().trim()

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, getString(R.string.journal_fill), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val entry = JournalEntry(title = title, content = content, date = today)

            val uid = auth.currentUser?.uid ?: return@setOnClickListener
            db.collection("users").document(uid).collection("journal")
                .add(entry)
                .addOnSuccessListener { loadEntries() }
            etTitle.setText("")
            etContent.setText("")
            Toast.makeText(this, getString(R.string.journal_saved), Toast.LENGTH_SHORT).show()
        }

        loadEntries()
    }

    private fun loadEntries() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).collection("journal")
            .get()
            .addOnSuccessListener { result ->
                val entries = result.documents.mapNotNull { it.toObject(JournalEntry::class.java) }
                    .sortedByDescending { it.date }
                val lines = entries.map { it.date + " - " + it.title + "\n" + it.content }
                listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, lines)
            }
    }
}