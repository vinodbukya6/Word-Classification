package com.ml.quaterion.sarcaso

import android.content.Context
import android.os.AsyncTask
import android.os.Build
import androidx.annotation.RequiresApi
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.Iterator
import kotlin.collections.List
import kotlin.collections.average
import kotlin.collections.indices
import kotlin.collections.maxBy
import kotlin.collections.set
import kotlin.collections.toList
import kotlin.collections.toTypedArray
import java.util.Arrays

class EmbeddingBuilder  {

    private var context : Context? = null
    private var filename : String? = null
    private var callback : VocabCallback? = null
    private var maxlen : Int? = null
    private var embeddingData : HashMap< String , DoubleArray >? = null
    private var embeddingDim : Int? = null


    constructor( context: Context , jsonFilename : String , embeddingDim : Int ){
        this.context = context
        this.filename = jsonFilename
        this.embeddingDim = embeddingDim
    }

    fun loadVocab () {
        val loadVocabularyTask = LoadVocabularyTask( callback )
        loadVocabularyTask.execute( loadJSONFromAsset( filename ))
    }

    private fun loadJSONFromAsset(filename : String? ): String? {
        var json: String? = null
        try {
            val inputStream = context!!.assets.open(filename )
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            json = String(buffer)
        }
        catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
        //println(json)
        return json
    }

    fun setCallback( callback: VocabCallback ) {
        this.callback = callback
    }

    fun tokenize ( message : String ): Array<DoubleArray> {
        val tokens : List<String> = Tokenizer.getTokens( message ).toList()
        val tokenizedMessage = ArrayList<DoubleArray>()
        for ( part in tokens ) {
            var vector : DoubleArray? = null
            if ( embeddingData!![part] == null ) {
                vector = DoubleArray( embeddingDim!! ){ 0.0 }
            }
            else{
                vector = embeddingData!![part]
            }
            tokenizedMessage.add( vector!! )

        }
        return tokenizedMessage.toTypedArray()
    }
    fun dotProduct(arg1: DoubleArray, arg2: DoubleArray): Double{
        var dotprod = 0.0;
        for (i in arg1.indices){
            dotprod += arg1[i] * arg2[i]
        }
        return dotprod
    }

    fun setVocab( data : HashMap<String, DoubleArray>? ) {
        this.embeddingData = data
    }

    fun setMaxLength( maxlen : Int ) {
        this.maxlen = maxlen
    }

    fun loadJSONFromAsset2(): String? {
        var json: String? = null
        json = try {
            val `is`: InputStream = context!!.assets.open("categorywords.json" )
            //getActivity().getAssets().open("yourfilename.json")
            val size: Int = `is`.available()
            val buffer = ByteArray(size)
            `is`.read(buffer)
            `is`.close()
            String(buffer, Charset.forName("UTF-8"))
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
        //println(json)
        return json
    }
    fun embedWordCategory(): (Triple<Array<DoubleArray>,Array<DoubleArray>,Array<DoubleArray>>){
        val dataWords = loadJSONFromAsset2()
        val jsonObject = JSONObject(dataWords)
        //val iterator : Iterator<String> = jsonObject.keys()
        //println(iterator)
        val formalWords: JSONArray = jsonObject.getJSONArray("Formal words")
        val informalWords: JSONArray = jsonObject.getJSONArray("Informal words")
        val rudeWords: JSONArray = jsonObject.getJSONArray("Rude words")
        val formalWordVector = ArrayList<DoubleArray>()
        val informalWordVector = ArrayList<DoubleArray>()
        val rudeWordVector = ArrayList<DoubleArray>()
        for (  x in 0 until formalWords.length() ) {
            var vector : DoubleArray? = null
            val pet: String = formalWords.getString(x)
            if ( embeddingData!![pet] == null ) {
                vector = DoubleArray( embeddingDim!! ){ 0.0 }
            }
            else{
                vector = embeddingData!![pet]
            }
            formalWordVector.add( vector!! )
        }
        for (  x in 0 until informalWords.length() ) {
            var vector : DoubleArray? = null
            val pet: String = informalWords.getString(x)
            if ( embeddingData!![pet] == null ) {
                vector = DoubleArray( embeddingDim!! ){ 0.0 }
            }
            else{
                vector = embeddingData!![pet]
            }
            informalWordVector.add( vector!! )
        }
        for (  x in 0 until rudeWords.length() ) {
            var vector : DoubleArray? = null
            val pet: String = rudeWords.getString(x)
            if ( embeddingData!![pet] == null ) {
                vector = DoubleArray( embeddingDim!! ){ 0.0 }
            }
            else{
                vector = embeddingData!![pet]
            }
            rudeWordVector.add( vector!! )
        }
        // println(formalWordVector.toTypedArray().count())
        return Triple(formalWordVector.toTypedArray(),informalWordVector.toTypedArray(), rudeWordVector.toTypedArray())
    }

    fun outputCalculation(tokenizedMessage: Array<DoubleArray>, formalwordEmbed:  Array<DoubleArray>): (ArrayList<Double>){
        val meanValue = ArrayList<Double>()
        for (i in tokenizedMessage.indices){
            val formalWordVector = ArrayList<Double>()
            val arr1 = tokenizedMessage[i]
            for (j in formalwordEmbed.indices){
                val arr2 = formalwordEmbed[j]
                val dotprod = dotProduct(arr1, arr2)
                formalWordVector.add( dotprod!! )
            }
            meanValue.add(formalWordVector.average())
        }
       return meanValue
    }
    fun maxValueIndex(arr1:ArrayList<Double>, arr2:ArrayList<Double>, arr3:ArrayList<Double>): (ArrayList<Int>){
        val Arr1 = ArrayList<Int>()
        //val catList = listOf("Formal words","Informal words", "Rude words")
        for (i in arr1.indices){
            val Arr2 = ArrayList<Double>()
            Arr2.add(arr1[i])
            Arr2.add(arr2[i])
            Arr2.add(arr3[i])
            val maxIdx = Arr2.indices.maxBy { Arr2[it] } ?: -1
            //println(arr1)
            Arr1.add(maxIdx)
        }
        return Arr1
    }
    fun checkWordCategory(givenword : String ): (Triple<ArrayList<String>,ArrayList<String>,ArrayList<String>>){
        val dataWords = loadJSONFromAsset2()
        val jsonObject = JSONObject(dataWords)
        val formalWords: JSONArray = jsonObject.getJSONArray("Formal words")
        val informalWords: JSONArray = jsonObject.getJSONArray("Informal words")
        val rudeWords: JSONArray = jsonObject.getJSONArray("Rude words")
        val formalstring = ArrayList<String>()
        val informalstring = ArrayList<String>()
        val rudestring = ArrayList<String>()
        for (  x in 0 until formalWords.length() ) {
            val pet: String = formalWords.getString(x)
            if (pet == givenword) {
                formalstring.add("Formal words")
            }
        }
        for (  x in 0 until informalWords.length() ) {
            val pet: String = informalWords.getString(x)
            if (pet == givenword) {
                informalstring.add("Informal words")
            }
        }
        for (  x in 0 until rudeWords.length() ) {
            val pet: String = rudeWords.getString(x)
            if (pet == givenword) {
                rudestring.add("Rude words")
            }
        }
        return Triple(formalstring,informalstring,rudestring)
    }

    interface VocabCallback {
        fun onDataProcessed( result : HashMap<String, DoubleArray>?)
    }

    private inner class LoadVocabularyTask(callback: VocabCallback?) : AsyncTask<String, Void, HashMap<String, DoubleArray>?>() {

        private var callback : VocabCallback? = callback

        override fun doInBackground(vararg params: String?): HashMap<String, DoubleArray>? {
            val jsonObject = JSONObject( params[0] )
            val iterator : Iterator<String> = jsonObject.keys()
            val data = HashMap< String , DoubleArray >()
            while ( iterator.hasNext() ) {
                val key = iterator.next()
                val array = jsonObject.getJSONArray( key )
                val embeddingArray = DoubleArray( array.length() )
                for ( x in 0 until array.length() ) {
                    embeddingArray[x] = array.get( x ) as Double
                }
                data[key] = embeddingArray
            }
            return data
        }

        override fun onPostExecute(vocab: HashMap<String, DoubleArray>?) {
            super.onPostExecute(vocab)
            callback?.onDataProcessed( vocab )
        }

    }

}