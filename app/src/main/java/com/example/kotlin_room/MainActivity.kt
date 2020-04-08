package com.example.kotlin_room

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ActionMenuView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlin_room.adapter.NoteAdapter
import com.example.kotlin_room.data.Note
import com.example.kotlin_room.viewmodels.NoteViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    companion object{
        const val ADD_NOTE_REQUEST = 1
        const val EDIT_NOTE_REQUEST = 2
    }

    private lateinit var noteViewModel: NoteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonAddNote.setOnClickListener{
            startActivityForResult(
                Intent(this, AddEditNoteActivity::class.java),
                ADD_NOTE_REQUEST
            )
        }

        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.setHasFixedSize(true)

        var adapter = NoteAdapter()

        recycler_view.adapter = adapter

        noteViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)

        noteViewModel.getAllNotes().observe(this, Observer<List<Note>> { adapter.submitList(it) })


        ItemTouchHelper(object: ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT.or(ItemTouchHelper.RIGHT)){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val note: Note = adapter.getNoteAt(viewHolder.adapterPosition)
                noteViewModel.delete(note)
                Toast.makeText(baseContext, "Note Deleted", Toast.LENGTH_SHORT).show()

                val name = note.id

                var snackbar = Snackbar.make(coordinator_layout!!, name!!.toString() + " removed from history!", Snackbar.LENGTH_LONG)
                snackbar.setAction("UNDO"){
                    noteViewModel.insert(note)
                }
                snackbar.setActionTextColor(Color.YELLOW)
                snackbar.show()
            }
        }).attachToRecyclerView(recycler_view)

        adapter.setOnItemClickListener(object: NoteAdapter.OnItemClickListener{
            override fun onItemClick(note: Note) {
                var intent = Intent(baseContext, AddEditNoteActivity::class.java)
                intent.putExtra(AddEditNoteActivity.EXTRA_ID, note.id)
                intent.putExtra(AddEditNoteActivity.EXTRA_TITLE, note.title)
                intent.putExtra(AddEditNoteActivity.EXTRA_DESCRIPTION, note.description)
                intent.putExtra(AddEditNoteActivity.EXTRA_PRIORITY, note.priority)

                startActivityForResult(intent, EDIT_NOTE_REQUEST)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item?.itemId){
            R.id.delete_all_notes -> {
                noteViewModel.deleteAllNotes()
                Toast.makeText(this, "All notes deleted!", Toast.LENGTH_SHORT).show()
                true
            }
            else ->{
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == ADD_NOTE_REQUEST && resultCode == Activity.RESULT_OK){
            val newNote = Note(
                data!!.getStringExtra(AddEditNoteActivity.EXTRA_TITLE),
                data.getStringExtra(AddEditNoteActivity.EXTRA_DESCRIPTION),
                data.getIntExtra(AddEditNoteActivity.EXTRA_PRIORITY, 1)
            )
            noteViewModel.insert(newNote)

            Toast.makeText(this, "Note saved!", Toast.LENGTH_SHORT).show()
        }else if (requestCode == EDIT_NOTE_REQUEST && resultCode == Activity.RESULT_OK){
            val id = data?.getIntExtra(AddEditNoteActivity.EXTRA_ID, -1)

            if(id == -1){
                Toast.makeText(this, "Could not update! Error!", Toast.LENGTH_SHORT).show()
            }

            val updateNote = Note(
                data!!.getStringExtra(AddEditNoteActivity.EXTRA_TITLE),
                data.getStringExtra(AddEditNoteActivity.EXTRA_DESCRIPTION),
                data.getIntExtra(AddEditNoteActivity.EXTRA_PRIORITY, 1)
            )
            updateNote.id = data.getIntExtra(AddEditNoteActivity.EXTRA_ID, -1)
            noteViewModel.update(updateNote)
        }else{
            Toast.makeText(this, "Note note saved!", Toast.LENGTH_SHORT).show()
        }
    }
}
