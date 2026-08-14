package com.lemon.mcdevmanagermp.mcp

import com.lemon.mcdevmanagermp.data.common.JSONConverter
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.consts.enums.WorkItemActionEnum
import com.lemon.mcdevmanagermp.data.consts.enums.WorkItemStatusEnum
import com.lemon.mcdevmanagermp.data.dto.netease.activity.FileInfoDTO
import com.lemon.mcdevmanagermp.data.dto.netease.work.ChangePriceDTO
import com.lemon.mcdevmanagermp.data.dto.netease.work.WorkCreateChannel
import com.lemon.mcdevmanagermp.data.dto.netease.work.WorkCreateDTO
import com.lemon.mcdevmanagermp.data.dto.netease.work.WorkCreateRes
import com.lemon.mcdevmanagermp.data.repository.FileUploadRepositoryImpl
import com.lemon.mcdevmanagermp.data.repository.ResourceRepositoryImpl
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceData
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceDetailChannel
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceDetailRes
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceDetailTag
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceDetailVideoInfo
import com.lemon.mcdevmanagermp.domain.resource.GetResourceListUseCase
import com.lemon.mcdevmanagermp.domain.upload.parseUploadUrl
import com.lemon.mcdevmanagermp.domain.work.WorkDetailUseCase
import com.lemon.mcdevmanagermp.domain.work.WorkManageUseCase
import com.lemon.mcdevmanagermp.platform.platformFileFromPath
import com.lemon.mcdevmanagermp.platform.validateVideoFile
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

internal enum class AssetPurpose(val wireName: String, val remoteType: String) {
    RESOURCE_PACKAGE("resource_package", "zip_package"),
    IMAGE("image", "image"),
    VIDEO("video", "video");

    companion object {
        fun fromWireName(value: String): AssetPurpose = entries.firstOrNull { it.wireName == value }
            ?: throw ToolFailure("不支持的上传用途")
    }
}

internal data class UploadedAsset(
    val id: String,
    val purpose: AssetPurpose,
    val fileInfo: FileInfoDTO,
    val url: String,
    val sourceName: String,
    val size: Long,
)

internal class ToolFailure(message: String) : Exception(message)

internal class WorkToolService(
    private val authSession: AuthSession,
) {
    private val repository = ResourceRepositoryImpl.INSTANCE
    private val workDetail = WorkDetailUseCase(repository)
    private val workManage = WorkManageUseCase(GetResourceListUseCase(repository), repository)
    private val uploadRepository = FileUploadRepositoryImpl.INSTANCE
    private val assets = ConcurrentHashMap<String, UploadedAsset>()

    suspend fun listWorks(platform: String, status: String?, name: String?): JsonObject {
        val works = requireSuccess(authSession.withReauthRetry { workManage.getWorkList(platform) })
            .orEmpty()
            .filter { status.isNullOrBlank() || it.status == status }
            .filter { name.isNullOrBlank() || it.itemName.contains(name, ignoreCase = true) }
        return buildJsonObject {
            put("works", buildJsonArray { works.forEach { add(workSummary(it)) } })
            put("count", works.size)
        }
    }

    suspend fun getWorkDetail(itemId: String): JsonObject {
        val detail =
            requireSuccess(authSession.withReauthRetry { workDetail.getResourceDetail(itemId) })
                ?: throw ToolFailure("作品不存在")
        val item = findWork(itemId)
        return buildJsonObject {
            put("detail", JSONConverter.parseToJsonElement(JSONConverter.encodeToString(detail)))
            put("currentStatus", item.getStatus().des)
            put("allowedActions", actionsJson(item))
        }
    }

    suspend fun getReferenceData(): JsonObject {
        val consts = requireSuccess(authSession.withReauthRetry { workDetail.getMCConsts() })
        val tags = requireSuccess(authSession.withReauthRetry { workDetail.getItemTag() })
        return buildJsonObject {
            put(
                "mcConsts",
                consts?.let { JSONConverter.parseToJsonElement(JSONConverter.encodeToString(it)) }
                    ?: JsonNull)
            put(
                "itemTags",
                tags?.let { JSONConverter.parseToJsonElement(JSONConverter.encodeToString(it)) }
                    ?: JsonNull)
        }
    }

    suspend fun getReviewFeedback(itemId: String): JsonObject {
        val item = findWork(itemId)
        requireAction(item, WorkItemActionEnum.VIEW_FEEDBACK)
        val feedback =
            requireSuccess(authSession.withReauthRetry { workManage.getReviewFeedback(itemId) })
        return buildJsonObject {
            put(
                "feedback",
                feedback?.let { JSONConverter.parseToJsonElement(JSONConverter.encodeToString(it)) }
                    ?: JsonNull)
            putStatus(item)
        }
    }

    suspend fun uploadAsset(
        pathInput: String,
        purposeName: String,
        mimeTypeInput: String?
    ): JsonObject {
        val purpose = AssetPurpose.fromWireName(purposeName)
        val path = resolveUploadPath(pathInput)
        validateFileKind(path, purpose)
        val size = Files.size(path)
        val platformFile =
            platformFileFromPath(path.toString()) ?: throw ToolFailure("无法读取文件")
        if (purpose == AssetPurpose.VIDEO) {
            val validation = validateVideoFile(platformFile, size)
            if (!validation.isValid) throw ToolFailure(
                validation.errorMessage ?: "视频文件不符合要求"
            )
        }
        val mimeType = mimeTypeInput?.takeIf { it.isNotBlank() }
            ?: Files.probeContentType(path)
            ?: defaultMimeType(purpose)
        val fileInfo = requireSuccess(authSession.withReauthRetry {
            uploadRepository.uploadFile(
                fileType = purpose.remoteType,
                fileName = path.fileName.toString(),
                file = platformFile,
                mimeType = mimeType,
                secure = "true",
            )
        }) ?: throw ToolFailure("上传失败")
        val url = parseUploadUrl(fileInfo.body)
        if (url.isEmpty()) throw ToolFailure("上传成功但未能解析资源地址")
        val id = UUID.randomUUID().toString()
        assets[id] = UploadedAsset(id, purpose, fileInfo, url, path.fileName.toString(), size)
        return buildJsonObject {
            put("assetId", id)
            put("purpose", purpose.wireName)
            put("name", path.fileName.toString())
            put("size", size)
        }
    }

    suspend fun createWork(args: JsonObject): JsonObject {
        val resource =
            args.stringOrNull("resourceAssetId")?.let { asset(it, AssetPurpose.RESOURCE_PACKAGE) }
        val channels = args["channels"]?.jsonObject.orEmpty().map { (channelId, assetId) ->
            WorkCreateChannel(
                channelId.toIntOrNull() ?: throw ToolFailure("宣传图 channelId 无效"),
                asset(assetId.jsonPrimitive.content, AssetPurpose.IMAGE).fileInfo
            )
        }
        val body = WorkCreateDTO(
            itemName = args.requiredString("itemName"),
            itemVersion = args.stringOrNull("itemVersion") ?: "0.1",
            priType = args.intOrNull("priType") ?: 0,
            subType = args.intOrNull("subType") ?: 0,
            modSecondType = args.intOrNull("modSecondType") ?: 0,
            info = args.stringOrNull("info").orEmpty(),
            updateSummary = args.stringOrNull("updateSummary").orEmpty(),
            labelTypeList = args.intList("labelTypeList"),
            tag = args.stringList("tags").map { ResourceDetailTag(name = it) },
            isOriginal = args.booleanOrNull("isOriginal") ?: true,
            isDomainServerItem = if (args.booleanOrNull("joinShantou") == true) 1 else 0,
            priceType = args.stringOrNull("priceType") ?: "free",
            priceRank = args.intOrNull("priceRank") ?: 0,
            price = args.intOrNull("price") ?: 0,
            res = resource?.let { listOf(WorkCreateRes(it.fileInfo, it.sourceName)) }.orEmpty(),
            channel = channels,
            isCheckApply = args.booleanOrNull("submitReview") ?: false,
        )
        requireSuccess(authSession.withReauthRetry { workDetail.createWork(body) })
        return buildJsonObject { put("created", true) }
    }

    suspend fun updateWork(args: JsonObject): JsonObject {
        val itemId = args.requiredString("itemId")
        val item = findWork(itemId)
        requireAction(item, WorkItemActionEnum.UPDATE)
        val detail =
            requireSuccess(authSession.withReauthRetry { workDetail.getResourceDetail(itemId) })
                ?: throw ToolFailure("作品不存在")
        val patch = args["patch"]?.jsonObject ?: throw ToolFailure("缺少 patch")
        val updated = detail.copy(
            itemName = patch.stringOrNull("itemName") ?: detail.itemName,
            itemVersion = patch.stringOrNull("itemVersion") ?: detail.itemVersion,
            info = patch.stringOrNull("info") ?: detail.info,
            updateSummary = patch.stringOrNull("updateSummary") ?: detail.updateSummary,
            activityDesc = patch.stringOrNull("activityDesc") ?: detail.activityDesc,
            priType = patch.intOrNull("priType") ?: detail.priType,
            subType = patch.intOrNull("subType") ?: detail.subType,
            modSecondType = patch.intOrNull("modSecondType") ?: detail.modSecondType,
            labelTypeList = patch["labelTypeList"]?.let { patch.intList("labelTypeList") }
                ?: detail.labelTypeList,
            tags = patch["tags"]?.let {
                patch.stringList("tags").map { name ->
                    detail.tags.firstOrNull { it.name == name } ?: ResourceDetailTag(name)
                }
            } ?: detail.tags,
            isOriginal = patch.booleanOrNull("isOriginal") ?: detail.isOriginal,
            weakOffline = patch.booleanOrNull("weakOffline") ?: detail.weakOffline,
            weakOfflineReason = patch.stringOrNull("weakOfflineReason") ?: detail.weakOfflineReason,
            res = patch.stringOrNull("resourceAssetId")?.let { id ->
                val uploaded = asset(id, AssetPurpose.RESOURCE_PACKAGE)
                listOf(
                    ResourceDetailRes(
                        addVersion = patch.booleanOrNull("addVersion") ?: true,
                        resName = uploaded.sourceName,
                        resUrl = uploaded.url
                    )
                )
            } ?: detail.res,
            channel = patch["channels"]?.jsonObject?.map { (channelId, assetId) ->
                val id = channelId.toIntOrNull() ?: throw ToolFailure("宣传图 channelId 无效")
                ResourceDetailChannel(
                    id,
                    asset(assetId.jsonPrimitive.content, AssetPurpose.IMAGE).url,
                    detail.channel.firstOrNull { it.channelId == id }?.version ?: 0
                )
            } ?: detail.channel,
            videoInfoList = buildVideoList(patch, detail.videoInfoList),
            corpProofImage = patch.stringOrNull("corpProofAssetId")
                ?.let { asset(it, AssetPurpose.IMAGE).url } ?: detail.corpProofImage,
        )
        requireSuccess(authSession.withReauthRetry { workDetail.updateWork(updated, false) })
        return buildJsonObject { put("updated", true); putStatus(item) }
    }

    suspend fun submitReview(itemId: String): JsonObject =
        writeAction(itemId, WorkItemActionEnum.SUBMIT_REVIEW) {
            workManage.submitForReview(itemId)
        }

    suspend fun cancelReview(itemId: String): JsonObject =
        writeAction(itemId, WorkItemActionEnum.CANCEL_REVIEW) {
            workManage.cancelReview(itemId)
        }

    suspend fun submitSelfTest(itemId: String, passCheck: Boolean): JsonObject =
        writeAction(itemId, WorkItemActionEnum.SUBMIT_SELF_TEST) {
            workManage.applySelfTest(itemId, passCheck)
        }

    suspend fun cancelSelfTest(itemId: String): JsonObject =
        writeAction(itemId, WorkItemActionEnum.CANCEL_TEST) {
            workManage.cancelSelfTest(itemId)
        }

    suspend fun publish(itemId: String, opPlatform: String): JsonObject =
        writeAction(itemId, WorkItemActionEnum.PUBLISH) {
            workManage.publish(itemId, opPlatform)
        }

    suspend fun schedulePublish(itemId: String, time: String?, opPlatform: String): JsonObject =
        writeAction(itemId, WorkItemActionEnum.APPOINT_ONLINE) {
            if (time != null && !APPOINT_TIME.matches(time)) throw ToolFailure("上架时间格式必须为 yyyy-MM-dd HH:mm:ss")
            workManage.appointOnline(itemId, time, opPlatform)
        }

    suspend fun changePrice(itemId: String, price: Int, rank: Int, type: String): JsonObject =
        writeAction(itemId, WorkItemActionEnum.ADJUST_PRICE) {
            if (price < 0 || rank < 0 || type !in setOf(
                    "free",
                    "point",
                    "diamond"
                )
            ) throw ToolFailure("定价参数无效")
            workManage.changePrice(itemId, ChangePriceDTO(price, rank, type))
        }

    suspend fun getDeleteTarget(itemId: String): ResourceData {
        val item = findWork(itemId)
        requireAction(item, WorkItemActionEnum.DELETE)
        if (item.getStatus() != WorkItemStatusEnum.INIT) throw ToolFailure("仅待提交审核的草稿可删除")
        return item
    }

    suspend fun deleteConfirmed(itemId: String): JsonObject {
        val current = getDeleteTarget(itemId)
        requireSuccess(authSession.withReauthRetry { workManage.deleteItem(current.itemId) })
        return buildJsonObject { put("executed", true); put("itemId", current.itemId) }
    }

    private suspend fun <T> writeAction(
        itemId: String,
        action: WorkItemActionEnum,
        block: suspend () -> NetworkState<T>,
    ): JsonObject {
        val item = findWork(itemId)
        requireAction(item, action)
        requireSuccess(authSession.withReauthRetry(block))
        return buildJsonObject { put("executed", true); putStatus(item) }
    }

    private suspend fun findWork(itemId: String): ResourceData {
        if (itemId.isBlank()) throw ToolFailure("作品 ID 为空")
        return requireSuccess(authSession.withReauthRetry { workManage.getWorkList("pe") })
            ?.firstOrNull { it.itemId == itemId }
            ?: throw ToolFailure("作品不存在")
    }

    private fun requireAction(item: ResourceData, action: WorkItemActionEnum) {
        if (action !in item.getStatus().actions(item.price <= 0)) {
            throw ToolFailure("当前状态 ${item.getStatus().label} 不允许${action.label}")
        }
    }

    private fun asset(id: String, purpose: AssetPurpose): UploadedAsset {
        val uploaded = assets[id] ?: throw ToolFailure("assetId 无效或已失效")
        if (uploaded.purpose != purpose) throw ToolFailure("assetId 用途不匹配")
        return uploaded
    }

    private fun buildVideoList(
        patch: JsonObject,
        fallback: List<ResourceDetailVideoInfo>
    ): List<ResourceDetailVideoInfo> {
        val value = patch["videos"] ?: return fallback
        return value.jsonArray.map { element ->
            val video = element.jsonObject
            val uploaded = asset(video.requiredString("videoAssetId"), AssetPurpose.VIDEO)
            val cover =
                video.stringOrNull("coverAssetId")?.let { asset(it, AssetPurpose.IMAGE).url }
                    .orEmpty()
            ResourceDetailVideoInfo(
                cover = cover,
                size = uploaded.size.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
                url = uploaded.url
            )
        }
    }

    private fun resolveUploadPath(input: String): Path {
        if (input.isBlank()) throw ToolFailure("文件路径为空")
        val path = runCatching { Path.of(input).toRealPath() }
            .getOrElse { throw ToolFailure("文件不存在或无法读取") }
        if (!Files.isRegularFile(path) || !Files.isReadable(path)) {
            throw ToolFailure("路径不是可读取的普通文件")
        }
        return path
    }

    private fun validateFileKind(path: Path, purpose: AssetPurpose) {
        val extension = path.fileName.toString().substringAfterLast('.', "").lowercase()
        val allowed = when (purpose) {
            AssetPurpose.RESOURCE_PACKAGE -> setOf("zip", "mcworld", "mcpack", "mcaddon")
            AssetPurpose.IMAGE -> setOf("png", "jpg", "jpeg", "webp")
            AssetPurpose.VIDEO -> setOf("mp4")
        }
        if (extension !in allowed) throw ToolFailure("文件类型与上传用途不匹配")
    }

    private fun defaultMimeType(purpose: AssetPurpose): String = when (purpose) {
        AssetPurpose.RESOURCE_PACKAGE -> "application/zip"
        AssetPurpose.IMAGE -> "image/*"
        AssetPurpose.VIDEO -> "video/mp4"
    }

    private fun workSummary(item: ResourceData): JsonObject = buildJsonObject {
        put("itemId", item.itemId)
        put("itemName", item.itemName)
        put("status", item.getStatus().des)
        put("statusLabel", item.getStatus().label)
        put("priceType", item.priceType)
        put("price", item.price)
        put("priceRank", item.priceRank)
        put("allowedActions", actionsJson(item))
    }

    private fun actionsJson(item: ResourceData): JsonArray = buildJsonArray {
        item.getStatus().actions(item.price <= 0)
            .forEach { add(JsonPrimitive(it.name.lowercase())) }
    }

    private fun kotlinx.serialization.json.JsonObjectBuilder.putStatus(item: ResourceData) {
        put("currentStatus", item.getStatus().des)
        put("allowedActions", actionsJson(item))
    }

    private fun <T> requireSuccess(state: NetworkState<T>): T? = when (state) {
        is NetworkState.Success -> state.data
        is NetworkState.Error -> throw ToolFailure(state.msg.ifBlank { "请求失败" })
    }

    companion object {
        private val APPOINT_TIME = Regex("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")
    }
}

private fun JsonObject.requiredString(name: String): String =
    stringOrNull(name)?.takeIf { it.isNotBlank() }
        ?: throw ToolFailure("缺少参数 $name")

private fun JsonObject.stringOrNull(name: String): String? = (this[name] as? JsonPrimitive)?.content
private fun JsonObject.intOrNull(name: String): Int? = stringOrNull(name)?.toIntOrNull()
private fun JsonObject.booleanOrNull(name: String): Boolean? =
    stringOrNull(name)?.toBooleanStrictOrNull()

private fun JsonObject.stringList(name: String): List<String> =
    this[name]?.jsonArray?.map { it.jsonPrimitive.content }.orEmpty()

private fun JsonObject.intList(name: String): List<Int> =
    this[name]?.jsonArray?.mapNotNull { it.jsonPrimitive.content.toIntOrNull() }.orEmpty()
