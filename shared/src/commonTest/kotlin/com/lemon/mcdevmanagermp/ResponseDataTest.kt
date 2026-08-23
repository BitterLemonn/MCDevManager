package com.lemon.mcdevmanagermp

import com.lemon.mcdevmanagermp.data.common.JSONConverter
import com.lemon.mcdevmanagermp.data.common.ResponseData
import kotlin.test.Test
import kotlin.test.assertEquals

class ResponseDataTest {
    @Test
    fun neteaseParamErrorIncludesFieldDetails() {
        val response = JSONConverter.decodeFromString<ResponseData<Unit>>(
            """
                {
                  "errors": {
                    "info": ["提交内容中含有非法字符: bad tag: i"],
                    "sync_item_info": {
                      "info": ["提交内容中含有非法字符: bad tag: i"]
                    }
                  },
                  "status": "params error"
                }
            """.trimIndent()
        )

        assertEquals(
            "info: 提交内容中含有非法字符: bad tag: i\n" +
                "sync_item_info.info: 提交内容中含有非法字符: bad tag: i",
            response.errorMessage()
        )
    }
}
