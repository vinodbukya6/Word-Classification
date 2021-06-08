package com.ml.quaterion.sarcaso

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.annotation.IntegerRes
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var embeddingBuilder: EmbeddingBuilder
    private var isVocabLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        startLoadVocab()

        classify_button.setOnClickListener( View.OnClickListener {
            val message = message_text.text.toString().toLowerCase().trim()
            var allRudeWords = ArrayList<String>()
            val allFormalWords = ArrayList<String>()
            val allInformalWords = ArrayList<String>()
            if ( !TextUtils.isEmpty( message ) ){
                if ( isVocabLoaded ) {
                    val tokenizedMessage = embeddingBuilder.tokenize(message) // gives embeddings for each word
                    // val tokensCount = tokenizedMessage.count()
                    println(tokenizedMessage)
                    val catList = listOf("Formal words","Informal words", "Rude words")
                    //var hashMap : HashMap<String, String> = HashMap<String, String> ()
                    val (formalwordEmbed,informalwordEmbed,rudewordEmbed) = embeddingBuilder.embedWordCategory()
                    val givenTextTokens : List<String> = Tokenizer.getTokens( message ).toList()
                    for (token in givenTextTokens){
                        var (formalString, informalString, rudeString) = embeddingBuilder.checkWordCategory(token)
                        if (formalString.isEmpty() && informalString.isEmpty() && rudeString.isEmpty()) {
                            val tokenVector = embeddingBuilder.tokenize(token)
                            val formalScores = embeddingBuilder.outputCalculation(tokenVector, formalwordEmbed)
                            val informalScores = embeddingBuilder.outputCalculation(tokenVector, informalwordEmbed)
                            val rudeScores = embeddingBuilder.outputCalculation(tokenVector, rudewordEmbed)
                            val maxIndex = embeddingBuilder.maxValueIndex(formalScores,informalScores, rudeScores)
                            var catName = catList[maxIndex[0]]
                            if (catName == "Formal words") {
                                allFormalWords.add(token)
                            }
                            else if (catName == "Informal words") {
                                allInformalWords.add(token)
                            }
                            else {
                                allRudeWords.add(token)
                            }
                        }
                        else{
                            if (formalString.isNotEmpty() && formalString[0] == "Formal words") {
                                allFormalWords.add(token)
                            }
                            if (informalString.isNotEmpty() && informalString[0] == "Informal words") {
                                allInformalWords.add(token)
                            }
                            if (rudeString.isNotEmpty() && rudeString[0] == "Rude words") {
                                allRudeWords.add(token)
                            }
                        }
                    }
                    result_text.text = "Formal Words: $allFormalWords\nInformal Words: $allInformalWords\nRude Words: $allRudeWords"
                }
                else {
                    Toast.makeText(this@MainActivity, "Vocab not loaded", Toast.LENGTH_SHORT).show()
                }
            }
            else{
                Toast.makeText( this@MainActivity, "Please enter a message.", Toast.LENGTH_LONG).show();
            }
        })

    }

    private fun startLoadVocab(){
        embeddingBuilder = EmbeddingBuilder( this , "embedding.json" , 50 )
        embeddingBuilder.setMaxLength( 16 )
        embeddingBuilder.setCallback( object : EmbeddingBuilder.VocabCallback {
            override fun onDataProcessed( result : HashMap<String, DoubleArray>? ) {
                embeddingBuilder.setVocab( result )
                isVocabLoaded = true
            }
        })
        embeddingBuilder.loadVocab()
    }

}