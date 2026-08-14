package com.lemon.mcdevmanagermp.mcp

import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.types.BooleanSchema
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.ElicitRequestParams
import io.modelcontextprotocol.kotlin.sdk.types.ElicitResult
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.ToolAnnotations
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

internal fun Server.registerWorkTools(service: WorkToolService) {
    val readOnly = ToolAnnotations(
        readOnlyHint = true,
        destructiveHint = false,
        idempotentHint = true,
        openWorldHint = true
    )
    val write = ToolAnnotations(
        readOnlyHint = false,
        destructiveHint = true,
        idempotentHint = false,
        openWorldHint = true
    )

    addTool(
        name = "list_works",
        description = "查询当前账号作品列表，并返回状态及允许的后续操作。",
        inputSchema = schema(
            "platform" to stringProperty("平台，通常为 pe"),
            "status" to stringProperty("可选状态过滤，如 init/online"),
            "name" to stringProperty("可选作品名模糊过滤"),
        ),
        toolAnnotations = readOnly,
    ) { request ->
        result {
            service.listWorks(
                request.arguments?.string("platform") ?: "pe",
                request.arguments?.string("status"),
                request.arguments?.string("name")
            )
        }
    }

    addTool(
        name = "get_work_detail",
        description = "获取作品完整详情及当前允许操作。",
        inputSchema = schema("itemId" to stringProperty("作品 ID"), required = listOf("itemId")),
        toolAnnotations = readOnly,
    ) { request -> result { service.getWorkDetail(request.arguments.requiredString("itemId")) } }

    addTool(
        name = "get_work_reference_data",
        description = "获取创建和编辑作品所需的分类、渠道及标签参考数据。",
        inputSchema = schema(),
        toolAnnotations = readOnly,
    ) { result { service.getReferenceData() } }

    addTool(
        name = "get_review_feedback",
        description = "获取作品审核反馈。",
        inputSchema = schema("itemId" to stringProperty("作品 ID"), required = listOf("itemId")),
        toolAnnotations = readOnly,
    ) { request -> result { service.getReviewFeedback(request.arguments.requiredString("itemId")) } }

    addTool(
        name = "upload_work_asset",
        description = "上传 agent 可访问的本地文件，返回进程内 assetId。",
        inputSchema = schema(
            "path" to stringProperty("本地文件的绝对路径或相对于 MCP 进程工作目录的路径"),
            "purpose" to enumProperty(listOf("resource_package", "image", "video"), "文件用途"),
            "mimeType" to stringProperty("可选 MIME 类型"),
            required = listOf("path", "purpose"),
        ),
        toolAnnotations = write,
    ) { request ->
        result {
            service.uploadAsset(
                request.arguments.requiredString("path"),
                request.arguments.requiredString("purpose"),
                request.arguments?.string("mimeType")
            )
        }
    }

    addTool(
        name = "create_work",
        description = "创建 PE 作品；资源包和宣传图必须引用 upload_work_asset 返回的 assetId。",
        inputSchema = schema(
            "itemName" to stringProperty("作品名称"),
            "itemVersion" to stringProperty("版本号，默认 0.1"),
            "priType" to integerProperty("一级分类 ID"),
            "subType" to integerProperty("二级分类 ID"),
            "modSecondType" to integerProperty("次级分类 ID"),
            "info" to stringProperty("作品详情 HTML"),
            "updateSummary" to stringProperty("更新说明"),
            "labelTypeList" to arrayProperty("integer", "推荐标签 ID"),
            "tags" to arrayProperty("string", "文本标签"),
            "isOriginal" to booleanProperty("是否原创"),
            "joinShantou" to booleanProperty("是否加入山头服"),
            "priceType" to enumProperty(listOf("free", "point", "diamond"), "价格类型"),
            "priceRank" to integerProperty("价格档位"),
            "price" to integerProperty("价格"),
            "resourceAssetId" to stringProperty("资源包 assetId"),
            "channels" to objectProperty("宣传图映射：channelId -> image assetId"),
            "submitReview" to booleanProperty("创建时是否同时申请审核"),
            required = listOf("itemName"),
        ),
        toolAnnotations = write,
    ) { request -> result { service.createWork(request.arguments ?: JsonObject(emptyMap())) } }

    addTool(
        name = "update_work",
        description = "按白名单 Merge Patch 编辑最新作品详情。patch 可包含 itemName/itemVersion/info/updateSummary/activityDesc/priType/subType/modSecondType/labelTypeList/tags/isOriginal/weakOffline/weakOfflineReason/resourceAssetId/addVersion/channels/videos/corpProofAssetId。",
        inputSchema = schema(
            "itemId" to stringProperty("作品 ID"),
            "patch" to objectProperty("白名单字段补丁"),
            required = listOf("itemId", "patch"),
        ),
        toolAnnotations = write,
    ) { request -> result { service.updateWork(request.arguments ?: JsonObject(emptyMap())) } }

    registerItemAction(
        "submit_review",
        "提交作品审核。",
        write
    ) { a -> service.submitReview(a.requiredString("itemId")) }
    registerItemAction(
        "cancel_review",
        "取消作品审核。",
        write
    ) { a -> service.cancelReview(a.requiredString("itemId")) }
    registerItemAction(
        "cancel_self_test",
        "取消作品自测。",
        write
    ) { a -> service.cancelSelfTest(a.requiredString("itemId")) }

    addTool(
        name = "submit_self_test",
        description = "提交作品自测。",
        inputSchema = schema(
            "itemId" to stringProperty("作品 ID"),
            "passCheck" to booleanProperty("true 表示免机审"),
            required = listOf("itemId", "passCheck")
        ),
        toolAnnotations = write,
    ) { request ->
        result {
            service.submitSelfTest(
                request.arguments.requiredString("itemId"),
                request.arguments?.boolean("passCheck") ?: false
            )
        }
    }

    addTool(
        name = "publish_work",
        description = "立即上架作品。",
        inputSchema = schema(
            "itemId" to stringProperty("作品 ID"),
            "opPlatform" to stringProperty("上架平台，默认 all"),
            required = listOf("itemId")
        ),
        toolAnnotations = write,
    ) { request ->
        result {
            service.publish(
                request.arguments.requiredString("itemId"),
                request.arguments?.string("opPlatform") ?: "all"
            )
        }
    }

    addTool(
        name = "schedule_work_publish",
        description = "设置或取消定时上架；省略 appointOnlineTime 表示取消。",
        inputSchema = schema(
            "itemId" to stringProperty("作品 ID"),
            "appointOnlineTime" to stringProperty("yyyy-MM-dd HH:mm:ss"),
            "opPlatform" to stringProperty("上架平台，默认 all"),
            required = listOf("itemId")
        ),
        toolAnnotations = write,
    ) { request ->
        result {
            service.schedulePublish(
                request.arguments.requiredString("itemId"),
                request.arguments?.string("appointOnlineTime"),
                request.arguments?.string("opPlatform") ?: "all"
            )
        }
    }

    addTool(
        name = "change_work_price",
        description = "调整已上架非免费作品的定价。",
        inputSchema = schema(
            "itemId" to stringProperty("作品 ID"),
            "price" to integerProperty("价格"),
            "priceRank" to integerProperty("价格档位"),
            "priceType" to enumProperty(listOf("free", "point", "diamond"), "价格类型"),
            required = listOf("itemId", "price", "priceRank", "priceType"),
        ),
        toolAnnotations = write,
    ) { request ->
        result {
            service.changePrice(
                request.arguments.requiredString("itemId"),
                request.arguments.requiredInt("price"),
                request.arguments.requiredInt("priceRank"),
                request.arguments.requiredString("priceType")
            )
        }
    }

    addTool(
        name = "delete_work",
        description = "永久删除待提交审核的草稿。删除前由 MCP 客户端显示不可绕过的确认表单。",
        inputSchema = schema("itemId" to stringProperty("作品 ID"), required = listOf("itemId")),
        toolAnnotations = ToolAnnotations(
            readOnlyHint = false,
            destructiveHint = true,
            idempotentHint = true,
            openWorldHint = true
        ),
    ) { request ->
        try {
            val target = service.getDeleteTarget(request.arguments.requiredString("itemId"))
            val elicited = createElicitation(
                message = "确认永久删除作品「${target.itemName}」（ID: ${target.itemId}，状态: ${target.getStatus().label}）？此操作不可恢复。",
                requestedSchema = ElicitRequestParams.RequestedSchema(
                    properties = mapOf(
                        "confirmed" to BooleanSchema(
                            title = "确认永久删除",
                            description = "仅在明确同意永久删除时勾选",
                            default = false
                        )
                    ),
                    required = listOf("confirmed"),
                ),
            )
            val confirmed =
                elicited.action == ElicitResult.Action.Accept && elicited.content?.get("confirmed")?.jsonPrimitive?.booleanOrNull == true
            if (!confirmed) success(buildJsonObject {
                put("executed", false); put(
                "reason",
                "用户未确认删除"
            )
            }, "已取消删除")
            else result { service.deleteConfirmed(target.itemId) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: ToolFailure) {
            error(e.message ?: "删除被拒绝")
        } catch (_: Exception) {
            error("删除被拒绝或客户端不支持安全确认")
        }
    }
}

private fun Server.registerItemAction(
    name: String,
    description: String,
    annotations: ToolAnnotations,
    action: suspend (JsonObject) -> JsonObject
) {
    addTool(
        name,
        description,
        schema("itemId" to stringProperty("作品 ID"), required = listOf("itemId")),
        toolAnnotations = annotations
    ) { request ->
        result { action(request.arguments ?: JsonObject(emptyMap())) }
    }
}

private suspend fun result(block: suspend () -> JsonObject): CallToolResult = try {
    success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: ToolFailure) {
    error(e.message ?: "请求失败")
} catch (_: Exception) {
    error("请求失败，请检查参数或认证状态")
}

private fun success(data: JsonObject, text: String = "操作成功"): CallToolResult {
    val structured = buildJsonObject { put("ok", true); put("data", data) }
    return CallToolResult(content = listOf(TextContent(text)), structuredContent = structured)
}

private fun error(message: String): CallToolResult {
    val structured = buildJsonObject { put("ok", false); put("error", message) }
    return CallToolResult(
        content = listOf(TextContent(message)),
        structuredContent = structured,
        isError = true
    )
}

private fun schema(
    vararg properties: Pair<String, JsonObject>,
    required: List<String> = emptyList()
): ToolSchema = ToolSchema(
    properties = buildJsonObject { properties.forEach { (name, value) -> put(name, value) } },
    required = required,
)

private fun stringProperty(description: String) =
    buildJsonObject { put("type", "string"); put("description", description) }

private fun integerProperty(description: String) =
    buildJsonObject { put("type", "integer"); put("description", description) }

private fun booleanProperty(description: String) =
    buildJsonObject { put("type", "boolean"); put("description", description) }

private fun enumProperty(values: List<String>, description: String) = buildJsonObject {
    put("type", "string"); put("description", description); put(
    "enum",
    kotlinx.serialization.json.buildJsonArray { values.forEach { add(JsonPrimitive(it)) } })
}

private fun arrayProperty(itemType: String, description: String) = buildJsonObject {
    put("type", "array"); put("description", description); putJsonObject("items") {
    put(
        "type",
        itemType
    )
}
}

private fun objectProperty(description: String) =
    buildJsonObject { put("type", "object"); put("description", description) }

private fun JsonObject?.requiredString(name: String): String =
    this?.string(name)?.takeIf { it.isNotBlank() } ?: throw ToolFailure("缺少参数 $name")

private fun JsonObject?.requiredInt(name: String): Int =
    this?.get(name)?.jsonPrimitive?.content?.toIntOrNull()
        ?: throw ToolFailure("缺少或无效参数 $name")

private fun JsonObject.string(name: String): String? = (this[name] as? JsonPrimitive)?.content
private fun JsonObject.boolean(name: String): Boolean? =
    (this[name] as? JsonPrimitive)?.booleanOrNull
