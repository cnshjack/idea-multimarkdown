/*
 * Copyright (c) 2015-2015 Vladimir Schneider <vladimir.schneider@gmail.com>
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package com.vladsch.idea.multimarkdown.util

enum class Severity private constructor(val value: Int) {
    INFO(0), WEAK_WARNING(1), WARNING(2), ERROR(3)
}

class InspectionResult(val id: String, val severity: Severity, val fixedLink: String? = null, val fixedFilePath: String? = null) {
    var referenceId: Any? = null

    constructor(mismatch: InspectionResult, fixedLink: String? = null, fixedFilePath: String? = null) : this(mismatch.id, mismatch.severity, fixedLink, fixedFilePath)

    var handled: Boolean = false

    fun compareTo(other: InspectionResult): Int {
        return if (severity == other.severity && fixedLink == other.fixedLink && fixedFilePath == other.fixedFilePath) id.compareTo(other.id) else -1
    }

    private fun dataPrint(value: Any?) :String {
        return if (value == null) "null" else when(value) {
            is Unit -> ""
            is Int, is Long, is Boolean, is Float, is Double, is Byte  -> "$value"
            is Char  -> "'$value'"
            else -> "\"$value\""
        }
    }

    override fun toString(): String {
        return "InspectionResults(${dataPrint(referenceId)}, \"$id\", Severity.$severity, ${if (fixedLink == null) "null" else "\"$fixedLink\"" }, ${if (fixedFilePath == null) "null" else "\"$fixedFilePath\""})"
    }

    fun isA(id: String) = this.id == id

    //    /*  0 */arrayOf<Any?>(18, GitHubLinkInspector.ID_WIKI_LINK_HAS_DASHES , Severity.WEAK_WARNING, "Normal File", null) /*  0 */
    fun toArrayOfTestString(rowId: Int = 0, removePrefix: String = ""): String {
        val rowPad = " ".repeat(3 - rowId.toString().length) + rowId
        return "arrayOf($rowPad, \"$id\", Severity.$severity, ${if (fixedLink == null) "null" else "\"$fixedLink\"" }, ${if (fixedFilePath == null) "null" else "\"${if (!removePrefix.isEmpty()) PathInfo.relativePath(removePrefix.suffixWith('/'), fixedFilePath) else fixedFilePath}\""})"
    }

    companion object {
        @JvmStatic fun handled(results: List<InspectionResult>, vararg ids: String) {
            for (result in results) {
                if (!result.handled && ids.contains(result.id)) {
                    result.handled = true
                }
            }
        }
    }
}
