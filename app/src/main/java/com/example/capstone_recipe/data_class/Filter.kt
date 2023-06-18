package com.example.capstone_recipe.data_class

import java.io.Serializable

/** *레시피 검색 시 사용되는 필터 목록 */
data class Filter(
    var includeIngredient: MutableList<String>? = null,
    var excludeIngredient: MutableList<String>? = null,
    var timeLimit: String? = null,
    var calorieLimit: String? = null,
    var levelLimit: LEVEL? = null
): Serializable

