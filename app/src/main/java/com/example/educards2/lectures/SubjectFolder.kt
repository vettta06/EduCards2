package com.example.educards2.lectures

data class SubjectFolder(
    val name: String,
    val lectureFiles: List<LectureFile>,
    var isExpanded: Boolean = false
)