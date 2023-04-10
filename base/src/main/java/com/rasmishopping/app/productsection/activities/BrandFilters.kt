object BrandFilters{
    val selectedFilters = hashMapOf<String,String>()

    fun asList():ArrayList<String>{
        val list = arrayListOf<String>()
        selectedFilters.values.forEach {
            list.add(it)
        }
        return list
    }
}