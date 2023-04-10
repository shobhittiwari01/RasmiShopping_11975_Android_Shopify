package com.shopify.mltranslation

// TRANSlATION CODE BY MOHD JAVED ON 10 JUNE 2022
object Translation {
    //lateinit var Translator: Translator
   // lateinit var conditions: DownloadConditions

    interface TranslationCallback {
        fun transdata(textvalue:String)
    }

    interface LanguageCallback {
        fun getLanguage(textvalue:String)
    }

    interface DownloadCallback {
        fun getDownloadStatus(flag:Boolean)
    }

    /*
    fun identifyLanguage(textvalue: String,languageCallback: LanguageCallback){
        val languageIdentifier = LanguageIdentification.getClient()
        languageIdentifier.identifyLanguage(textvalue)
            .addOnSuccessListener { languageCode ->
                if (languageCode == "und") {
                    languageCallback.getLanguage("404")
                } else {
                    Log.i("javed", "Language: $languageCode")
                    languageCallback.getLanguage(languageCode)
                }
            }
            .addOnFailureListener {
                languageCallback.getLanguage("404")
            }
    }*/

     /*fun initialzeTranslatorModel(initialLanguage:String,language_to_translate: String,downloadCallback: DownloadCallback) {
         if(initialLanguage!=language_to_translate) {
             var options = TranslatorOptions.Builder()
                 .setSourceLanguage(initialLanguage)
                 .setTargetLanguage(language_to_translate)
                 .build()
             Translator = Translation.getClient(options)
             conditions = DownloadConditions.Builder()
                 .build()
             Translator.downloadModelIfNeeded(conditions)
                 .addOnSuccessListener {
                     downloadCallback.getDownloadStatus(true)
                 }
                 .addOnFailureListener { exception ->
                     Log.d("javed", "downloadexception " + exception)
                     downloadCallback.getDownloadStatus(false)
                 }
         }
    }*/

     @JvmStatic
    fun translatetext(text_to_translate: String,language_to_translate: String,translationCallback: TranslationCallback){
        /*Translator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                Translator.translate(text_to_translate)
                    .addOnSuccessListener { translatedText ->
                        translationCallback.transdata(translatedText)
                    }
                    .addOnFailureListener { exception ->
                        Log.d("javed", "exception: " + exception)
                        translationCallback.transdata(text_to_translate)
                    }
            }
            .addOnFailureListener { exception ->
                Log.d("javed", "downloadexception " + exception)
                translationCallback.transdata(text_to_translate)
            }*/
         val translate = translate_api()
         translate.setOnTranslationCompleteListener(object :
             translate_api.OnTranslationCompleteListener {
             override fun onStartTranslation() {
                 // here you can perform initial work before translated the text like displaying progress bar
             }
             override fun onCompleted(text: String?) {
                 translationCallback.transdata(text!!)
             }
             override fun onError(e: Exception?) {}
         })
         translate.execute(text_to_translate, "en", language_to_translate)
     }
}