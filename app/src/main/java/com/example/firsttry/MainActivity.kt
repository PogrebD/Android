package com.example.firsttry

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.firsttry.databinding.ActivityMainBinding
import org.json.JSONObject
import java.util.*


class MainActivity : AppCompatActivity() {

    private var historyArray = LinkedList<String>()
    private var history : SharedPreferences? = null
    private lateinit var bindingClass : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("mylog", "create")

        bindingClass = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bindingClass.root)

        history = getSharedPreferences("TABLE", Context.MODE_PRIVATE)
        val historyset: Set<String> = history?.getStringSet("str", historyArray.toSet()) as Set<String>
        historyArray.addAll(historyset)

        val adapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,historyArray)
        bindingClass.gottenBIN.setOnClickListener {
            bindingClass.historylist.adapter = adapter
            bindingClass.historylist.visibility = View.VISIBLE
        }

        bindingClass.historylist.setOnItemClickListener { _, _, i, _ ->
            bindingClass.gottenBIN.setText(historyArray[i])
            clickBget()
        }

        bindingClass.Bget.setOnClickListener{
           clickBget()
        }

        bindingClass.bg.setOnClickListener(){
            bindingClass.historylist.visibility = View.INVISIBLE
        }

        clickLink()
    }
    private fun clickLink(){
        bindingClass.tvBANKphone.setOnClickListener(){
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(bindingClass.tvBANKphone.text.toString())))
            startActivity(intent)
        }
        bindingClass.tvBANKurl.setOnClickListener(){
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://" + Uri.encode(bindingClass.tvBANKurl.text.toString().filter { !it.isWhitespace() })))
            startActivity(intent)
        }
        bindingClass.tvCOUNTRY.setOnClickListener(){
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:${bindingClass.tvlati.text.toString()},${bindingClass.tvlong.text.toString()}"))
            startActivity(intent)
        }
    }

    private fun clickBget(){

        Log.d("mylog", "$historyArray")

        bindingClass.historylist.visibility = View.INVISIBLE
        val bin :String = bindingClass.gottenBIN.text.toString()
        if(!historyArray.contains(bin)){
            if(bin != "") historyArray.addFirst(bin)
            if (historyArray.size > 3) historyArray.removeLast()
        }

        if (bin == "") {
            val errorDialog = AlertDialog.Builder(this@MainActivity)
            errorDialog.setMessage("Вы забыли ввести BIN")
            errorDialog.setPositiveButton( "OK" ){ dialog, _ ->
                dialog.dismiss()
            }
            Log.d("mylog", "error")
            errorDialog.show()
        }
        else {
            getBINInf(bin)
        }
    }

    private fun saveData(linkedList: LinkedList<String>){
        val editor  = history?.edit()
        editor?.putStringSet("str", linkedList.toSet())
        editor?.apply()
    }

    private fun getBINInf(bin: String){
        Log.d("mylog", "getbininfo")
        val url = "https://lookup.binlist.net/$bin"
        val tag = "tag"
        val queue = Volley.newRequestQueue(this)
        val request = StringRequest(
            Request.Method.GET,
            url,
            {
                result -> parseBin(result)
            },
            {
                val errorDialog = AlertDialog.Builder(this@MainActivity)
                errorDialog.setMessage("Некорректный BIN")
                errorDialog.setPositiveButton( "OK" ){ dialog, _ ->
                    dialog.dismiss()
                }
                Log.d("mylog", "error")
                errorDialog.show()

            }
        )
        request.tag = tag
        queue.add(request)
        Log.d("mylog", "add")
    }

    private fun parseBin(result: String) {
        val mainObject = JSONObject(result)

        parseString(mainObject, "scheme", bindingClass.tvSCHEME)
        parseString(mainObject, "brand", bindingClass.tvBRAND)
        parseString(mainObject, "type", bindingClass.tvTYPE)
        parseString(mainObject, "prepaid", bindingClass.tvPREPAID)
        parseJSObjStr(mainObject, "number","length", bindingClass.tvLENGTH)
        parseJSObjStr(mainObject, "number","lunh", bindingClass.tvLUHN)
        parseJSObjStr(mainObject, "country","name", bindingClass.tvCOUNTRY)
        parseJSObjStr(mainObject, "bank","name", bindingClass.tvBANKname)
        parseJSObjStr(mainObject, "bank","phone", bindingClass.tvBANKphone)
        parseJSObjStr(mainObject, "bank","url", bindingClass.tvBANKurl)
        parseJSObjStr(mainObject, "country", "latitude",bindingClass.tvlati)
        parseJSObjStr(mainObject, "country", "longitude",bindingClass.tvlong)
    }
    private fun parseString(mainObject: JSONObject, name: String, textv : TextView){
        if(mainObject.isNull(name)){
            textv.visibility = View.INVISIBLE
        }
        else {
            textv.text = mainObject.getString(name)
            textv.visibility = View.VISIBLE
        }
    }
    private fun parseJSObjStr(mainObject: JSONObject,jsonName: String ,name: String, textv : TextView){
        if(mainObject.isNull(jsonName)){
            textv.visibility = View.INVISIBLE
        }
        else {
            val obj = mainObject.getJSONObject(jsonName)
            if (obj.isNull(name)){
                textv.visibility = View.INVISIBLE
            }
            else {
                textv.text = obj.getString(name)
                textv.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        saveData(historyArray)
    }
}


